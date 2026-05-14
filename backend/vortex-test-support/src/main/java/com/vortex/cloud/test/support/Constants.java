package com.vortex.cloud.test.support;

/**
 * @author zhanglei
 */
public class Constants {
    public static final String TABLE_PREFIX = "sample_";

    /**
     * xxl-job 路由策略：分片广播
     * <p>
     * FIRST（第一个）：固定选择第一个机器；
     * <p>
     * LAST（最后一个）：固定选择最后一个机器；
     * <p>
     * ROUND（轮询）：
     * <p>
     * RANDOM（随机）：随机选择在线的机器；
     * <p>
     * CONSISTENT_HASH（一致性HASH）：每个任务按照Hash算法固定选择某一台机器，且所有任务均匀散列在不同机器上。
     * <p>
     * LEAST_FREQUENTLY_USED（最不经常使用）：使用频率最低的机器优先被选举；
     * <p>
     * LEAST_RECENTLY_USED（最近最久未使用）：最久为使用的机器优先被选举；
     * <p>
     * FAILOVER（故障转移）：按照顺序依次进行心跳检测，第一个心跳检测成功的机器选定为目标执行器并发起调度；
     * <p>
     * BUSYOVER（忙碌转移）：按照顺序依次进行空闲检测，第一个空闲检测成功的机器选定为目标执行器并发起调度；
     * <p>
     * SHARDING_BROADCAST(分片广播)：广播触发对应集群中所有机器执行一次任务，同时系统自动传递分片参数；可根据分片参数开发分片任务；
     */
    public static final String EXECUTOR_ROUTE_STRATEGY = "SHARDING_BROADCAST";

    /**
     * xxl-job 阻塞处理策略：丢弃后续调度
     * <p>
     * SERIAL_EXECUTION（单机串行（默认））：调度请求进入单机执行器后，调度请求进入FIFO队列并以串行方式运行；
     * <p>
     * DISCARD_LATER（丢弃后续调度）：调度请求进入单机执行器后，发现执行器存在运行的调度任务，本次请求将会被丢弃并标记为失败；
     * <p>
     * COVER_EARLY（覆盖之前调度）：调度请求进入单机执行器后，发现执行器存在运行的调度任务，将会终止运行中的调度任务并清空队列，然后运行本地调度任务；
     */
    public static final String EXECUTOR_BLOCK_STRATEGY = "DISCARD_LATER";

    public static final String LOCK_EXAMPLE_CODE = "example:tenantId:{0}:code:{1}";

    public static final String PARAM_TYPE_EXAMPLE_TYPE = "param_sewage_plant_type";
}
