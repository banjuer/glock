package io.banjuer.glock.client.api;

import com.alibaba.fastjson.JSON;
import io.banjuer.glock.client.exception.GLockInitException;
import io.banjuer.glock.client.proxy.RpcProxy;
import io.banjuer.glock.core.entity.LockResponse;
import io.banjuer.glock.core.rpc.api.LockService;
import io.banjuer.glock.core.util.InetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author guochengsen
 * 分布式锁服务: <b>不支持单机并发<b/>, 单机默认为同一个线程
 */
@Slf4j
public final class GLock {

    private static final LockService LOCK = RpcProxy.create(LockService.class);
    private static final String HOST = InetUtil.getHostName() + "/" + InetUtil.getHostIp();
    private static final Set<String> ALL_CLIENT_KEYS = new TreeSet<>();

    private String key;

    public GLock(String key) {
        synchronized (ALL_CLIENT_KEYS) {
            if (ALL_CLIENT_KEYS.contains(key))
                throw new GLockInitException("同一客户端服务器不允许出现相同的key:{" + key + "}. 提示:GLock以静态变量使用或者尝试更换不同的加锁关键字.");
            ALL_CLIENT_KEYS.add(key);
            this.key = key;
        }
    }

    public boolean tryLock() {
        LockResponse lockResponse = LOCK.tryLock(key, HOST);
        if (lockResponse.isSuccess())
            return true;
        log.warn("{}在{}上加锁失败", HOST, key);
        return false;
    }

    public boolean tryLock(long timeoutMs) {
        if (timeoutMs <= 0) return false;
        LockResponse lockResponse = LOCK.tryLock(key, HOST, timeoutMs);
        if (lockResponse.isSuccess())
            return true;
        log.warn("{}在{}上加锁失败", HOST, key);
        return false;
    }

    public void lock() {
        LockResponse response = LOCK.lock(key, HOST);
        log.info("{}在{}上加锁结果:{}", HOST, key, JSON.toJSON(response));
    }

    public void unlock() {
        LockResponse response = LOCK.unlock(key, HOST);
        log.info("{}在{}上解锁结果:{}", HOST, key, JSON.toJSON(response));
    }

}
