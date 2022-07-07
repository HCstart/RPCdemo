package org.sjtu.router;

import org.sjtu.common.ChannelFutureWrapper;

/**
 * @author hcstart
 * @create 2022-06-17 23:00
 *
 * 对服务提供者（不同的服务器）进行封装
 *
 */
public class Selector {

    /**
     * 服务命名
     *
     * eg: com.sise.test.DataService
     */
    private String providerServiceName;

    /**
     * 经过二次筛选之后的future集合
     */
    private ChannelFutureWrapper[] channelFutureWrappers;

    public ChannelFutureWrapper[] getChannelFutureWrappers() {
        return channelFutureWrappers;
    }

    public void setChannelFutureWrappers(ChannelFutureWrapper[] channelFutureWrappers) {
        this.channelFutureWrappers = channelFutureWrappers;
    }

    public String getProviderServiceName() {
        return providerServiceName;
    }

    public void setProviderServiceName(String providerServiceName) {
        this.providerServiceName = providerServiceName;
    }




}
