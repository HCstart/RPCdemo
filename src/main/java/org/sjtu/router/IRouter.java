package org.sjtu.router;

import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.registry.URL;

/**
 * @author hcstart
 * @create 2022-06-17 22:49
 *
 * 路由层主代码：实现负载均衡的策略
 *
 *
 */
public interface IRouter {

    /**
     * 刷新路由数组
     */
    void refreshRouterArr(Selector selector);

    /**
     * 获取到（请求到）连接通道
     */
    ChannelFutureWrapper  select(Selector selector);

    /**
     * 更新权重信息
     */
    void updateWeight(URL url);



}
