package com.example.geology.integration.reterraforged;

import com.example.geology.api.GeologyProvider;
import com.example.geology.core.GeologyChunkData;
import com.example.geology.core.GeologyGenerator;
import com.example.geology.core.ProvinceType;
import com.example.geology.core.RockStrata;
import com.example.geology.core.RockType;
import com.example.geology.core.ValueNoise;
import java.util.OptionalInt;
import raccoonman.reterraforged.world.worldgen.GeneratorContext;
import raccoonman.reterraforged.world.worldgen.cell.Cell;

/**
 * 集成模式地质数据提供者(读 RTF 预算的 Cell)。
 *
 * <p>本类实现"读取侧":从 RTF 的 {@link GeneratorContext#cache} 查询
 * 预算好的 {@link Cell}(已由 {@link GeologyPopulator} 填充 geology 字段),
 * 实现 {@link GeologyProvider} 接口供调用方使用。
 *
 * <p><b>查询路径</b>:
 * {@code ctx.cache.provideAtChunk(cx, cz).getChunkReader(cx, cz).getCell(x, z)}
 * —— 与 RTF 自身 feature(ErodeFeature 等)的查询范式一致,
 * tile 命中缓存后零成本。
 *
 * <p><b>职责边界</b>:
 * <ul>
 *   <li>{@link #province}:直接读 {@link Cell#geologyProvinceId}
 *       (Populator 已预算),反查 {@link ProvinceType};</li>
 *   <li>{@link #disturbedRockAt}:复用 {@link RockStrata#rockAt} +
 *       {@link RockStrata#disturb},扰动噪声用同 seed 的 ValueNoise
 *       (与独立模式行为一致,DRY);</li>
 *   <li>{@link #surfaceY}:读 RTF {@link Cell#height},经
 *       {@code ctx.levels.scale} 转换为方块 Y(与玩家看到的地形严格对齐)。</li>
 * </ul>
 *
 * <p><b>线程安全</b>:RTF {@link GeneratorContext#cache} 线程安全,
 * {@link ValueNoise} 无状态,本类可被多线程并发访问。
 *
 * <p><b>不缓存</b>:本类仅持 {@link GeneratorContext}(RTF per-world 单例)
 * 与 {@link ValueNoise}(轻量),构造成本极低;缓存会引入生命周期与并发债务。
 */
final class IntegratedGeologyProvider implements GeologyProvider {

    private final GeneratorContext ctx;
    private final ValueNoise rockNoise;

    IntegratedGeologyProvider(GeneratorContext ctx) {
        this.ctx = ctx;
        this.rockNoise = new ValueNoise(ctx.seed.root() ^ RockStrata.ROCK_NOISE_SEED_MASK);
    }

    @Override
    public ProvinceType province(int x, int z) {
        Cell cell = cellAt(x, z);
        int id = cell.geologyProvinceId;
        ProvinceType[] values = ProvinceType.values();
        if (id < 0 || id >= values.length) {
            // 防御:Cell 未被 Populator 填充(地图预览模式或异常情况),
            // fallback 到独立模式计算,保证调用方总能拿到合法 ProvinceType
            return GeologyGenerator.forSeed(ctx.seed.root()).province(x, z);
        }
        return values[id];
    }

    @Override
    public RockType disturbedRockAt(ProvinceType province, int relDepth,
                                     int x, int y, int z) {
        RockType base = RockStrata.rockAt(province, relDepth);
        return RockStrata.disturb(base, province, relDepth, x, y, z, rockNoise);
    }

    @Override
    public OptionalInt surfaceY(int x, int z) {
        Cell cell = cellAt(x, z);
        return OptionalInt.of(ctx.levels.scale(cell.height));
    }

    @Override
    public GeologyChunkData chunkData(int chunkX, int chunkZ) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        // 5 点采样多数表决,增强区块内代表性
        ProvinceType[] samples = {
            province(baseX + 4, baseZ + 4),
            province(baseX + 11, baseZ + 4),
            province(baseX + 4, baseZ + 11),
            province(baseX + 11, baseZ + 11),
            province(baseX + 8, baseZ + 8)
        };
        ProvinceType dominant = mostFrequent(samples);
        RockType surface = RockStrata.rockAt(dominant, 0);
        return new GeologyChunkData(dominant, surface, true);
    }

    /**
     * 查询 (x, z) 处的预算 Cell。
     *
     * <p>与 RTF 自身 feature 查询范式一致:
     * {@code ctx.cache.provideAtChunk(cx, cz).getChunkReader(cx, cz).getCell(x, z)}。
     * tile 命中缓存后零成本,未命中时同步阻塞至 tile 生成完成。
     *
     * @param x 方块 X
     * @param z 方块 Z
     * @return 含地质数据的 Cell
     */
    private Cell cellAt(int x, int z) {
        int cx = x >> 4;
        int cz = z >> 4;
        return ctx.cache.provideAtChunk(cx, cz)
                .getChunkReader(cx, cz)
                .getCell(x, z);
    }

    /** 多数表决:返回出现次数最多的 ProvinceType。 */
    private static ProvinceType mostFrequent(ProvinceType[] samples) {
        int[] counts = new int[ProvinceType.values().length];
        ProvinceType result = samples[0];
        int max = 0;
        for (ProvinceType p : samples) {
            int c = ++counts[p.ordinal()];
            if (c > max) {
                max = c;
                result = p;
            }
        }
        return result;
    }
}
