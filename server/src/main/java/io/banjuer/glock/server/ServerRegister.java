package io.banjuer.glock.server;

import io.banjuer.glock.core.config.CustomProperties;
import io.banjuer.glock.core.entity.LockType;
import io.banjuer.glock.core.entity.WorkType;
import io.banjuer.glock.core.util.InetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * 服务端注册
 */
@Slf4j
public class ServerRegister {

    public static String workType;

    private static final int RPC_PORT = CustomProperties.config.getInteger("glock.rpc.listen.port");
    private static final String LOCAL_HOST = InetUtil.getHostIp();

    /**
     * zk
     */
    private static final int ZK_SESSION_TIMEOUT_DEFAULT = 3000;
    private static final String ZK_ADDRESS = CustomProperties.config.getProperty("glock.rpc.zookeeper.connect.string");
    private static final Integer ZK_SESSION_TIMEOUT = CustomProperties.config.getInteger("glock.rpc.zookeeper.connect.timeout");
    private static final String ZK_ROOT = "/glock";
    private static final String ZK_SERVER_PATH = ZK_ROOT + "/" + CustomProperties.config.getProperty("glock.server.name");

    public void registerAndWatch() throws Exception {
        int timeout = (ZK_SESSION_TIMEOUT == null || ZK_SESSION_TIMEOUT <=0 )  ? ZK_SESSION_TIMEOUT_DEFAULT : ZK_SESSION_TIMEOUT;
        ZooKeeper zk = new ZooKeeper(ZK_ADDRESS, timeout, event -> {
            String path = event.getPath();
            Watcher.Event.EventType type = event.getType();
            log.info("zookeeper 在{}上发生{}事件", path, type.name());
            if (path.contains(WorkType.master.name())) {
                try {
                    ZooKeeper z = new ZooKeeper(ZK_ADDRESS, timeout, null);
                    log.warn("glock master 变更为{}", new String(z.getData(path, true, null)));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        });
        createAllPath(zk);
        registerCluster(zk);
        zk.close();
    }

    /**
     * 主从注册
     * 1. 当为master检查zk上master是否在线, 在线则启动失败, 不在线->注册为master, 并删除无效的slave
     * 2. slave启动, 检测master连接成功 ? 启动并注册slave : 启动失败
     */
    private void registerCluster(ZooKeeper zk) throws Exception {
        // 主从节点
        String wokeType = CustomProperties.config.getProperty("glock.server.work.type");
        workType = wokeType;
        String masterPath = ZK_SERVER_PATH + "/" + WorkType.master.name();
        String slavePath = ZK_SERVER_PATH + "/" + WorkType.slave.name();
        String masterData = new String(zk.getData(masterPath, true, null));
        if (WorkType.master.name().equals(wokeType.toLowerCase())) {
            // 全新启动
            if (masterData.length() == 0) {
                masterData = LOCAL_HOST + ":" + RPC_PORT;
                zk.setData(masterPath, masterData.getBytes(), -1);
            }
            boolean masterOnline = masterOnlineCheck(masterData);
            if (masterOnline)
                throw new RuntimeException("master服务目前可用请检查");
            else {
                // 再次启动
                zk.setData(masterPath, masterData.getBytes(), -1);
            }
        } else {
            if (masterData.length() == 0)
                throw new RuntimeException("无master可用");
            String masterIp = masterData.split(":")[0];
            boolean ping = InetUtil.ping(masterIp, 1, 1000);
            if (!ping)
                throw new RuntimeException("无法连接至master, 请检查网络");
            String slaveData = LOCAL_HOST + ":" + RPC_PORT;
            zk.setData(slavePath, slaveData.getBytes(), -1);
        }
    }

    private boolean masterOnlineCheck(String masterData) {
        int totalPings = 3;
        int pings = 0;
        for (int i = 0; i < totalPings; i++) {
            boolean result = InetUtil.ping(masterData.split(":")[0], 1, 1000);
            if (result)
                pings++;
        }
        return pings > 0;
    }

    /**
     * 检查并创建目录
     * zk数据节点结构:
     * glock/servername/master
     *                                 /slave
     *                                 /locktype
     */
    private void createAllPath(ZooKeeper zk) {
        // 根目录
        checkAndCreate(zk, ZK_ROOT, "");
        // 服务节点
        checkAndCreate(zk, ZK_SERVER_PATH, "");
        // 锁类型
        String lockTypePath = ZK_SERVER_PATH + "/locktype";
        String lockType = CustomProperties.config.getProperty("glock.server.lock.type");
        lockType = lockType == null ? LockType.simple.name() : lockType;
        checkAndCreate(zk, lockTypePath, lockType);
        // cluster
        checkAndCreate(zk, ZK_SERVER_PATH + "/" + WorkType.master.name(), "");
        checkAndCreate(zk, ZK_SERVER_PATH + "/" + WorkType.slave.name(), "");
    }

    private void checkAndCreate(ZooKeeper zk, String path, String value) {
        if (!exist(zk, path))
            create(zk, path, value);
    }

    private void create(ZooKeeper zk, String path, String value) {
        try {
            zk.create(path, value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            log.error("创建zk节点{" + path + "}失败." + e.toString());
        }
    }

    private boolean exist(ZooKeeper zk, String path) {
        try {
            Stat exists = zk.exists(path, true);
            if (exists == null)
                return false;
            return true;
        } catch (Exception e) {
            log.error("检查zk节点{" + path + "}失败." + e.toString());
        }
        return false;
    }

}
