package org.sjtu.filter.client;

import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.common.RpcInvocation;
import org.sjtu.common.utils.CommonUtils;
import org.sjtu.filter.IClientFilter;

import java.util.Iterator;
import java.util.List;

/**
 * @author hcstart
 * @create 2022-06-18 22:06
 *
 *
 * ip直连过滤器
 *
 */
public class DirectInvokeFilterImpl implements IClientFilter {

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String url = (String) rpcInvocation.getAttachments().get("url");
        if(CommonUtils.isEmpty(url)){
            return;
        }
        Iterator<ChannelFutureWrapper> channelFutureWrapperIterator = src.iterator();
        while (channelFutureWrapperIterator.hasNext()){
            ChannelFutureWrapper channelFutureWrapper = channelFutureWrapperIterator.next();
            if(!(channelFutureWrapper.getHost()+":"+channelFutureWrapper.getPort()).equals(url)){
                channelFutureWrapperIterator.remove();
            }
        }
        if(CommonUtils.isEmptyList(src)){
            throw new RuntimeException("no match provider url for "+ url);
        }
    }
}
