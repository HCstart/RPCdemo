package org.sjtu.proxy.javassist;

import org.sjtu.client.RpcReferenceWrapper;
import org.sjtu.proxy.ProxyFactory;

/**
 * @author hcstart
 * @create 2022-06-15 17:02
 */
public class JavassistProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                rpcReferenceWrapper.getAimClass(), new JavassistInvocationHandler(rpcReferenceWrapper));
    }
}
