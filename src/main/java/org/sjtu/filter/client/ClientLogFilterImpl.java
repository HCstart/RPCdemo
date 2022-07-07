package org.sjtu.filter.client;

import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.common.RpcInvocation;
import org.sjtu.filter.IClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.sjtu.common.cache.CommonClientCache.CLIENT_CONFIG;

/**
 * @author hcstart
 * @create 2022-06-18 21:55
 *
 * 客户端调用日志过滤器
 *
 */
public class ClientLogFilterImpl implements IClientFilter {

    private static Logger logger = LoggerFactory.getLogger(ClientLogFilterImpl.class);


    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        rpcInvocation.getAttachments().put("c_app_name",CLIENT_CONFIG.getApplicationName());
        logger.info(rpcInvocation.getAttachments().get("c_app_name")+" do invoke -----> "+rpcInvocation.getTargetServiceName());
    }
}
