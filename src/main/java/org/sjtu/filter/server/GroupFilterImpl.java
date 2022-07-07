package org.sjtu.filter.server;

import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.common.RpcInvocation;
import org.sjtu.common.utils.CommonUtils;
import org.sjtu.filter.IClientFilter;

import java.util.List;

/**
 * @author hcstart
 * @create 2022-06-18 21:58
 *
 * 服务端分组过滤器
 *
 */
public class GroupFilterImpl implements IClientFilter {

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String group = String.valueOf(rpcInvocation.getAttachments().get("group"));
        for (ChannelFutureWrapper channelFutureWrapper : src) {
            if (!channelFutureWrapper.getGroup().equals(group)) {
                src.remove(channelFutureWrapper);
            }
        }
        if (CommonUtils.isEmptyList(src)) {
            throw new RuntimeException("no provider match for group " + group);
        }
    }
}
