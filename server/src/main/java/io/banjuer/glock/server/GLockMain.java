package io.banjuer.glock.server;

import io.banjuer.glock.core.config.CustomProperties;
import io.banjuer.glock.server.rpc.registry.RpcRegistry;

/**
 * @author guochengsen
 */
public class GLockMain {

    public static void main(String[] args) throws Exception {
        init();
        start();
    }

    private static void start() {
        // 启动服务端监听
        new RpcRegistry(CustomProperties.config.getInteger("glock.rpc.listen.port")).start();
        // 启动监控
        new MonitorMain().start();
    }

    private static void init() throws Exception {

        /*
         * zk注册流程:
         *      1). 连接zk查看并master
         *      2). master存在 -> ping 不通 -> 当前启动身份
         *      3). master -> 注册为master
         *      4). slave -> 注册slave
         *      5). watch master与slave节点, slave节点信息变更后, master更新异步slave信息
         */
        new ServerRegister().registerAndWatch();

        /*
         *  客户端启动流程:
         *  1. 从zk拉取master信息并watch
         *  2. master连不同超时并抛出异常
         * 3. 同一个客户端不能有重复的key: glock构造检查
         */

    }
    /*
     * 心跳:
     * 1. slave每隔 x 1s(可配置)ping master
     * 2. slave连续 y 3次无法ping通master, 开始争取成为master
     *
     * 如何保证数据一致: 因主从很难保证完全一致, 所以采用间隔复制, 辅以较为全面的监控服务. 每隔 x * y slave从master拉取数据(内存copy)
     * 为何难以保证完整一致: 要想实现完整一致, master每一个请求必须同步直至slave完成, 期间还要考虑网络通信, down机, 全新slave如何保证完整复制数据等等问题,
     *   这是一个非常复杂的系统问题且会导致并发处理能力降低
     *
     */

}
