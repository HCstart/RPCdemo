package org.sjtu.common.event.listener;

import org.sjtu.common.event.IRpcDestroyEvent;
import org.sjtu.registry.URL;

import static org.sjtu.common.cache.CommonServerCache.PROVIDER_URL_SET;
import static org.sjtu.common.cache.CommonServerCache.REGISTRY_SERVICE;

/**
 * @author hcstart
 * @create 2022-06-18 15:11
 */
public class ServiceDestroyListener implements IRpcListener<IRpcDestroyEvent>{
    @Override
    public void callBack(Object t) {
        for(URL url : PROVIDER_URL_SET){
            REGISTRY_SERVICE.unRegister(url);   //注册服务取消注册
        }
    }
}
