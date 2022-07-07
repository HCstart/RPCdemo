package org.sjtu.filter.client;

import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.common.RpcInvocation;
import org.sjtu.filter.IClientFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hcstart
 * @create 2022-06-18 21:50
 */
public class ClientFilterChain {

    //客户端责任链
    private static List<IClientFilter> iClientFilterList = new ArrayList<>();

    public void addClientFilter(IClientFilter iClientFilter) {
        iClientFilterList.add(iClientFilter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        for (IClientFilter iClientFilter : iClientFilterList) {
            iClientFilter.doFilter(src, rpcInvocation);
        }
    }

}
