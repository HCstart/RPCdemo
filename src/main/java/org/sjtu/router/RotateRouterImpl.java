package org.sjtu.router;

import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.registry.URL;

import java.util.List;

import static org.sjtu.common.cache.CommonClientCache.*;

/**
 * @author hcstart
 * @create 2022-06-18 10:07
 *
 *
 * 轮询调用核心算法
 *
 */
public class RotateRouterImpl implements IRouter{

    /**
     * 相比于随机调用，少了生成随机数组的一环
     * @param selector
     */
    @Override
    public void refreshRouterArr(Selector selector) {
        //获取提供selector服务的所有提供者
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());

        //相比于随机调用，此处少了生成随机数组的一步

        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        for (int i = 0; i < channelFutureWrappers.size(); i++) {
            arr[i] = channelFutureWrappers.get(i);  //直接按顺序
        }

        //每个服务都需要一个随机数组
        SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(), arr);
    }

    /**
     * 获取到（请求到）连接通道  --- 对外暴露的核心方法，每次外界调用服务的时候都是通过这个函数去获取下一次调用的 provider信息
     */
    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getChannelFutureWrappers());
    }

    @Override
    public void updateWeight(URL url) {

    }

}
