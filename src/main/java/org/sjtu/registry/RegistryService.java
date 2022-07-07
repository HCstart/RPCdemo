package org.sjtu.registry;

/**
 * @author hcstart
 * @create 2022-06-15 22:37
 *
 * RegistryService接口：服务的注册、下线，客户端订阅、取消订阅
 *
 * 远程服务信息的四个核心元操作
 *
 */
public interface RegistryService {

    /**
     * 注册url
     *
     * 将 irpc 服务写入注册中心节点
     * 当出现网络抖动时，需要进行适当的重试做法
     * 注册服务 url的时候需要写入持久化文件中
     */
    void registry(URL url);

    /**
     * 服务下线
     *
     * 持久化节点是无法进行服务下线操纵的
     * 下线的服务必须保证 url是完整匹配的
     * 移除持久化文件中的一些内容信息
     */
    void unRegister(URL url);

    /**
     * 消费方订阅服务
     */
    void subscribe(URL url);

    /**
     * 执行取消订阅内部的逻辑
     */
    void doUnSubscribe(URL url);


}
