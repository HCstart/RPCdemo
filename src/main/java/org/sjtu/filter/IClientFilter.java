package org.sjtu.filter;

import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.common.RpcInvocation;

import java.util.List;

/**
 * @author hcstart
 * @create 2022-06-18 21:38
 */
public interface IClientFilter extends IFilter{

    /**
     * 执行过滤链
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);



}
