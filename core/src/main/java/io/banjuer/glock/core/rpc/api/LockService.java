package io.banjuer.glock.core.rpc.api;


import io.banjuer.glock.core.entity.LockResponse;

/**
 * @author guochengsen
 */
public interface LockService {

    LockResponse tryLock(String key, String host);

    LockResponse tryLock(String key, String host, long timeoutMs);

    LockResponse lock(String key, String host);

    LockResponse unlock(String key, String host);

}
