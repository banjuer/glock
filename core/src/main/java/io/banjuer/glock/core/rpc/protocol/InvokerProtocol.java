package io.banjuer.glock.core.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author guochengsen
 */
public @Data
class InvokerProtocol implements Serializable {

    private static final long serialVersionUID = -6658183935955482542L;
    private String className;
    private String methodName;
    private Class<?>[] params;
    private Object[] values;
}
