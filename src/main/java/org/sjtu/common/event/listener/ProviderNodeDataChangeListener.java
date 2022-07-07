package org.sjtu.common.event.listener;

import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.common.event.IRpcNodeChangeEvent;
import org.sjtu.registry.URL;
import org.sjtu.registry.zookeeper.ProviderNodeInfo;

import java.util.List;

import static org.sjtu.common.cache.CommonClientCache.CONNECT_MAP;
import static org.sjtu.common.cache.CommonClientCache.IROUTER;

/**
 * @author hcstart
 * @create 2022-06-18 9:50
 *
 * provider节点数据变化监听更新
 *
 */
public class ProviderNodeDataChangeListener implements IRpcListener<IRpcNodeChangeEvent> {
    @Override
    public void callBack(Object t) {
        ProviderNodeInfo providerNodeInfo = (ProviderNodeInfo) t;
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerNodeInfo.getServiceName());

        for(ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers){
            String address = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            if(address.equals(providerNodeInfo.getAddress())){

                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());

                URL url = new URL();
                url.setServiceName(providerNodeInfo.getServiceName());

                //更新权重
                IROUTER.updateWeight(url);
                break;

            }
        }
    }

}
