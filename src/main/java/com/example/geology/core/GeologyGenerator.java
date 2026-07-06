package com.example.geology.core;

import com.example.geology.api.GeologyProvider;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 独立模式地质生成器。
 *
 * <p>实现 {@link GeologyProvider} 接口，通过自带的值噪声划分地质省并产生岩性扰动。
 * 不依赖任何地形模组，可在原版或其他地形模组下运行。
 *
 * <p>相对深度计算统一使用实际地表高度（heightmap），由调用方传入，
 * 不再由本类估算。这样保证世界生成、地质锤、放大镜三处使用同一深度基准。
 *
 * <p>集成模式（后期阶段）将提供新的 {@link GeologyProvider} 实现
 * （读取 RTF 地形数据），通过 {@link com.example.geology.api.GeologyProviders}
 * 注册表切换，调用方代码无需修改。
 *
 * <p>实例按 seed 缓存（{@link #forSeed(long)}），避免高频调用时重复构造噪声对象。
 * 缓存使用 {@link ConcurrentHashMap}，因为区块加载事件（{@code ChunkEvent.Load}）
 * 可能在异步线程触发，与主线程的工具物品交互并发访问缓存。
 * {@link ConcurrentHashMap#computeIfAbsent} 是原子操作，且构造函数无副作用，
 * 偶发重复构造无害。世界数量有限（一个世界一个 seed），缓存条目数极少，
 * 不需要弱引用回收。
 */
public final class GeologyGenerator implements GeologyProvider {

    /** 按 seed 缓存实例。线程安全，支持区块生成异步线程与主线程并发访问。 */
    private static final ConcurrentHashMap<Long, GeologyGenerator> CACHE = new ConcurrentHashMap<>();

    private final ValueNoise provinceNoise;
    private final ValueNoise rockNoise;

    public GeologyGenerator(long seed) {
        this.provinceNoise = new ValueNoise(seed);
        this.rockNoise = new ValueNoise(seed ^ RockStrata.ROCK_NOISE_SEED_MASK);
    }

    /**
     * 按 seed 获取缓存的实例。
     *
     * <p><b>调用方应优先使用 {@link com.example.geology.api.GeologyProviders#get}
     * 而非直接调用本方法</b>，以便通过配置切换生成模式。本方法主要供
     * {@link com.example.geology.api.GeologyProviders} 内部使用，
     * 以及测试场景直接构造实例。
     */
    public static GeologyGenerator forSeed(long seed) {
        return CACHE.computeIfAbsent(seed, GeologyGenerator::new);
    }

    /** 返回 (x,z) 处的地质省类型。 */
    @Override
    public ProvinceType province(int x, int z) {
        double n = provinceNoise.fbm2D(x / 420.0, z / 420.0, 3, 0.5, 2.0);
        if (n < -0.5) return ProvinceType.SEDIMENTARY_BASIN;
        if (n < -0.1) return ProvinceType.ANCIENT_CRATON;
        if (n < 0.2) return ProvinceType.SHIELD;
        if (n < 0.55) return ProvinceType.OROGENIC_BELT;
        return ProvinceType.VOLCANIC_PROVINCE;
    }

    /**
     * 给定地质省、相对深度与坐标，返回考虑了岩性扰动的岩性。
     * <p>
     * 世界生成（{@link com.example.geology.world.RockStrataFeature}）与
     * 工具物品（{@link com.example.geology.item.GeologicalHammerItem} /
     * {@link com.example.geology.item.MagnifierItem}）的 fallback 路径
     * 都应调用本方法，保证扰动逻辑单一来源、结果一致。
     *
     * <p>实际扰动逻辑委托 {@link RockStrata#disturb}，与集成模式共用同一来源。
     *
     * @param province  地质省
     * @param relDepth  相对深度（地表=0，向下为正，由调用方用 heightmap 计算）
     * @param x         世界 X 坐标
     * @param y         世界 Y 坐标
     * @param z         世界 Z 坐标
     */
    @Override
    public RockType disturbedRockAt(ProvinceType province, int relDepth, int x, int y, int z) {
        RockType base = RockStrata.rockAt(province, relDepth);
        return RockStrata.disturb(base, province, relDepth, x, y, z, rockNoise);
    }

    /**
     * 返回 (x,z) 处的构造扰动强度 [0.0, 1.0]。
     *
     * <p>基于岩性扰动噪声（{@link #rockNoise}）在 (x,z) 平面采样并归一化,
     * 供 RTF 集成模式预算到 {@code Cell.geologyDisturbance},洞穴系统等读取。
     * 值越大表示构造活动越强烈,可驱动洞穴密度与矿脉分布。
     *
     * <p>噪声尺度复用 {@link RockStrata#DISTURB_NOISE_SCALE},与
     * {@link RockStrata#disturb} 的扰动斑块大小一致,保证扰动基底与
     * 最终岩性扰动在同一空间尺度上协调。
     *
     * @param x 世界 X 坐标
     * @param z 世界 Z 坐标
     * @return 扰动强度 [0,1]
     */
    public float disturbanceAt(int x, int z) {
        double v = this.rockNoise.noise2D(x / RockStrata.DISTURB_NOISE_SCALE, z / RockStrata.DISTURB_NOISE_SCALE);
        return (float) ((v + 1.0) / 2.0);
    }

    /** 计算区块级地质摘要（以区块中心采样）。 */
    @Override
    public GeologyChunkData chunkData(int chunkX, int chunkZ) {
        int cx = (chunkX << 4) + 8;
        int cz = (chunkZ << 4) + 8;
        ProvinceType p = province(cx, cz);
        RockType surface = RockStrata.rockAt(p, 0);
        return new GeologyChunkData(p, surface, true);
    }
}
