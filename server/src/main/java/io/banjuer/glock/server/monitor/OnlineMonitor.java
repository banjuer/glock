package io.banjuer.glock.server.monitor;

import io.banjuer.glock.core.entity.WorkType;
import io.banjuer.glock.server.ServerRegister;

/**
 * 节点是否存活检查
 * 1. master每隔1s更新zk节点上的值+1
 * 2. slave每隔1s检查该节点是否增加1
 */
public class OnlineMonitor implements Monitor {

    @Override
    public void start() {
        String workType = ServerRegister.workType;
        if (WorkType.master.name().equals(workType)) {

        } else {

        }
    }
}
