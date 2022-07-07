package org.sjtu.filter.server;

import org.sjtu.common.RpcInvocation;
import org.sjtu.filter.IServerFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hcstart
 * @create 2022-06-18 21:48
 */
public class ServerFilterChain {

    //服务端责任链
    public static List<IServerFilter> iServerFilters = new ArrayList<>();

    public void addServerFilter(IServerFilter iServerFilter) {
        iServerFilters.add(iServerFilter);
    }

    public void doFilter(RpcInvocation rpcInvocation){
        for (IServerFilter iServerFilter : iServerFilters) {
            iServerFilter.doFilter(rpcInvocation);
        }
    }

}
