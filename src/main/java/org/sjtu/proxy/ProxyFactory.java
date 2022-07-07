package org.sjtu.proxy;

import org.sjtu.client.RpcReferenceWrapper;

/**
 * @author hcstart
 * @create 2022-06-15 16:26
 */
public interface ProxyFactory {

    <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable;

}
