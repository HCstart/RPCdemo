package org.sjtu.filter;

import org.sjtu.common.RpcInvocation;

/**
 * @author hcstart
 * @create 2022-06-18 21:39
 */
public interface IServerFilter extends IFilter{

    /**
     * 执行核心过滤逻辑
     */
    void doFilter(RpcInvocation rpcInvocation);

}
