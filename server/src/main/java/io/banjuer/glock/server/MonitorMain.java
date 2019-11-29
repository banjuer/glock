package io.banjuer.glock.server;

public class MonitorMain {

    // private static


    /*
     *  每个server定时检查自己的身份:
     *      1). 当自己为master时, 定时ping slave, 记录当前版本号, 把可连通slave更新进slave节点 -> 检查版本号
     *      2). 当为slave时, 定时ping master. 当master ping不通, 从slave节点拉去slave数据, 包含自己开始比较各自序列id, 大的为master, 更新自己的身份
     *
     */

    public void start() {
        // 扫描所有监控
        // 启动
    }

}
