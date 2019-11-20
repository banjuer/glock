package io.banjuer.glock.server.lock;

import io.banjuer.glock.core.config.CustomProperties;
import io.banjuer.glock.core.entity.LockInfo;
import io.banjuer.glock.core.entity.LockResponse;
import io.banjuer.glock.core.rpc.api.LockService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数合法化检查放到调用层
 * @author guochengsen
 */
public class SimpleLockService implements LockService {

    private static final Integer DEFAULT_LOCK_SIZE = 16;

    private final   Map<String, LockInfo> lockInfos;

    public SimpleLockService() {
        Integer lockSize = CustomProperties.config.getInteger("glock.server.lock.size");
        lockSize = lockSize == null ? DEFAULT_LOCK_SIZE : lockSize;
        this.lockInfos = new ConcurrentHashMap<>(lockSize);
    }

    @Override
    public LockResponse tryLock(String key, String host) {
        synchronized (key) {
            if (lockInfos.get(key) == null) {
                return firstLock(key, host);
            }
            return tryReentry(lockInfos.get(key), host);
        }
    }

    private LockResponse tryReentry(LockInfo lockInfo, String host) {
        if (host.equals(lockInfo.getHost())) {
            lockInfo.increaseCount();
            return LockResponse.lockSuccess(lockInfo.getCount());
        }
        return LockResponse.lockError();
    }

    private LockResponse firstLock(String key, String host) {
        LockInfo lockInfo = new LockInfo(key, host);
        lockInfo.increaseCount();
        lockInfos.put(key, lockInfo);
        return LockResponse.lockSuccess(lockInfo.getCount());
    }

    @Override
    public LockResponse tryLock(String key, String host, long timeoutMs) {
        long current = System.currentTimeMillis(),
                future = current + timeoutMs;
        while (current <= future) {
            LockResponse response = tryLock(key, host);
            if (response.isSuccess())
                return response;
            else
                current = System.currentTimeMillis();
        }
        return LockResponse.lockError();
    }

    @Override
    public LockResponse lock(String key, String host) {
        LockResponse response = tryLock(key, host);
        while (!response.isSuccess())
            response = tryLock(key, host);
        return response;
    }

    @Override
    public LockResponse unlock(String key, String host) {
        synchronized (key) {
            LockInfo lock = lockInfos.get(key);
            if (lock == null)
                return LockResponse.unlockError("非法解锁");
            if (!host.equals(lock.getHost()))
                return LockResponse.unlockError();
            int count = lock.getCount();
            int newCount = count- 1;
            if (newCount <= 0)
                lockInfos.remove(key);
            else
                lock.setCount(newCount);
            return LockResponse.unlockSuccess(newCount);
        }
    }

}
