package io.banjuer.glock.server.registry;

import io.banjuer.glock.core.entity.LockResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import io.banjuer.glock.core.rpc.protocol.InvokerProtocol;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guochengsen
 */
@Slf4j
@ChannelHandler.Sharable
public class RegistryHandler extends ChannelInboundHandlerAdapter {

    private Map<String, Object> locksMap = new ConcurrentHashMap<>();
    private List<String> classNames = new ArrayList<>();

    public RegistryHandler() {
        scannerLocks("io.banjuer.glock.server.lock");
        doRegister();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Object result = invokeMethod(msg);
        if (result != null)
            ctx.write(result);
        ctx.flush();
        ctx.close();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("grpc server :" + cause.toString());
        ctx.close();
    }

    /**
     * MethodHandler执行方法
     */
    private Object invokeMethod(Object msg) {
        Object result = null;
        InvokerProtocol request = (InvokerProtocol) msg;
        Object obj = locksMap.get(request.getClassName());
        if (obj == null) {
            log.warn("grpc server: {} has not implemented", request.getClassName());
            return null;
        }
        MethodType mt = MethodType.methodType(LockResponse.class, request.getParams());
        try {
            MethodHandle methodHandle = MethodHandles.lookup().findVirtual(obj.getClass(), request.getMethodName(), mt).bindTo(obj);
            result = methodHandle.invoke(request.getValues());
        } catch (Throwable throwable) {
            log.error("grpc server :" + throwable.toString());
        }
        return result;
    }

    /**
     * 扫描实现
     */
    private void scannerLocks(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        assert url != null;
        File dir = new File(url.getFile());
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                scannerLocks(packageName + "." + file.getName());
            } else {
                classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }
    }
    /**
     * 完成注册
     */
    private void doRegister() {
        if (classNames.size() == 0) {
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                locksMap.put(i.getName(), clazz.newInstance());
            } catch (Exception e) {
                log.error("注册" + className +"失败" + e);
            }
        }
    }

}
