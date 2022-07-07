package org.sjtu.registry.zookeeper;

import org.sjtu.registry.RegistryService;
import org.sjtu.registry.URL;

import java.util.List;
import java.util.Map;

import static org.sjtu.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static org.sjtu.common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * @author hcstart
 * @create 2022-06-16 9:44
 *
 * 对注册数据进行统一处理
 *
 * 对一些注册数据做统一的处理，假设日后需要考虑支持多种类型的注册中心，例如redis、etcd之类的话，所有基础的记录操作都可以统一放在抽象类里实现
 *
 */
public abstract class AbstractRegister implements RegistryService {

    /**
     * 注册 url
     */
    @Override
    public void registry(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    /**
     * 服务下线
     */
    @Override
    public void unRegister(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    /**
     * 消费方订阅服务
     */
    @Override
    public void subscribe(URL url){
        SUBSCRIBE_SERVICE_LIST.add(url);
    }

    /**
     * 执行取消订阅内部的逻辑
     */
    @Override
    public void doUnSubscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.remove(url.getServiceName());
    }

    /**
     * 获取服务的权重信息
     *
     * @param serviceName
     * @return <ip:port --> urlString>,<ip:port --> urlString>,<ip:port --> urlString>,<ip:port --> urlString>
     */
    public abstract Map<String, String> getServiceWeightMap(String serviceName);

    /**
     * 留给子类扩展：下面三个个方法
     */
    public abstract void doBeforeSubscribe(URL url);

    public abstract void doAfterSubscribe(URL url);

    public abstract List<String> getProviderIps(String serviceName);



}
