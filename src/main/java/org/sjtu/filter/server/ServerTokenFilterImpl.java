package org.sjtu.filter.server;

import org.sjtu.common.RpcInvocation;
import org.sjtu.common.utils.CommonUtils;
import org.sjtu.filter.IServerFilter;
import org.sjtu.server.ServiceWrapper;

import static org.sjtu.common.cache.CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP;

/**
 * @author hcstart
 * @create 2022-06-18 22:09
 *
 * 简单版本的 token校验
 *
 * 某些服务私密性强，调用时需要做安全防范
 *
 */
public class ServerTokenFilterImpl implements IServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String token = String.valueOf(rpcInvocation.getAttachments().get("serviceToken"));
        ServiceWrapper serviceWrapper = PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());
        String matchToken = String.valueOf(serviceWrapper.getServiceToken());
        if (CommonUtils.isEmpty(matchToken)) {
            return;
        }
        if (!CommonUtils.isEmpty(token) && token.equals(matchToken)) {
            return;
        }
        throw new RuntimeException("token is " + token + " , verify result is false!");
    }
}
