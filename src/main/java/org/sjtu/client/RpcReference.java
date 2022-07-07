package org.sjtu.client;

import org.sjtu.proxy.ProxyFactory;

/**
 * @author hcstart
 * @create 2022-06-15 20:58
 */
public class RpcReference {

    //代理工厂
    public ProxyFactory proxyFactory;

    public RpcReference(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    /**
     * 根据接口类型获取代理对象
     */
    public <T> T get(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
        return proxyFactory.getProxy(rpcReferenceWrapper);
    }


}
