package org.sjtu.proxy.jdk;

import org.sjtu.client.RpcReferenceWrapper;
import org.sjtu.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * @author hcstart
 * @create 2022-06-15 16:24
 *
 * 代理工厂：辅助客户端发起调用
 *
 */
public class JDKProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable {
        return (T) Proxy.newProxyInstance(rpcReferenceWrapper.getAimClass().getClassLoader(), new Class[]{rpcReferenceWrapper.getAimClass()},
                new JDKClientInvocationHandler(rpcReferenceWrapper));
    }

}
