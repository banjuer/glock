package io.banjuer.glock.core.client;


import io.banjuer.glock.core.rpc.api.LockService;

public class SimpleTest {

    public static void main(String[] args) {

        LockService lockService = RpcProxy.create(LockService.class);
        System.out.println(lockService.lock("lock", "host"));
        System.out.println(lockService.unlock("lock", "host"));

    }

}
