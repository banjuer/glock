package io.banjuer.glock.server;

import io.banjuer.glock.core.config.CustomProperties;
import io.banjuer.glock.server.registry.RpcRegistry;

/**
 * @author guochengsen
 */
public class GLockMain {

    public static void main(String[] args) {
        init();
        backFromFile();
        start();
    }

    private static void start() {
        // 启动服务端监听
        new RpcRegistry(CustomProperties.config.getInteger("glock.rpc.listen.port")).start();
    }

    private static void backFromFile() {

    }

    private static void init() {
        // 注册
    }

}
