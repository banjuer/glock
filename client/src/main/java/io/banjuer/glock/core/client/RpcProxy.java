package io.banjuer.glock.core.client;

import java.lang.reflect.Proxy;

/**
 * @author guochengsen
 */
public class RpcProxy {

    public static <T> T create(Class<?> clazz) {
        MethodProxy proxy = new MethodProxy(clazz);
        Class<?>[] interfaces = clazz.isInterface() ?
                new Class[]{clazz} :
                clazz.getInterfaces();
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, proxy);
    }

}
