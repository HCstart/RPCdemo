package org.sjtu.common.cache;

import org.sjtu.common.ChannelFuturePollingRef;
import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.common.RpcInvocation;
import org.sjtu.common.config.ClientConfig;
import org.sjtu.filter.client.ClientFilterChain;
import org.sjtu.registry.URL;
import org.sjtu.router.IRouter;
import org.sjtu.serialize.SerializeFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hcstart
 * @create 2022-06-15 17:28
 *
 * 客户端公用缓存  存储请求队列等公共信息
 *
 */
public class CommonClientCache {

    //储存客户端即将发送的信息
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue(100);

    //储存响应信息
    public static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();

    // provider名称 --> 该服务有哪些集群 URL
    public static List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();

    // com.sise.test.service -> <<ip:host,urlString>,<ip:host,urlString>,<ip:host,urlString>>
    public static Map<String, Map<String,String>> URL_MAP = new ConcurrentHashMap<>(); //这个设置成了线程安全的 HashMap
    public static Set<String> SERVER_ADDRESS  = new HashSet<>();

    //每次远程调用时，都是从这里面去选择服务提供者
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();  //这个设置成了线程安全的 HashMap


    //路由层中有关随机请求的 Map
    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();  //保存每个服务对应的已经设定好的负载均衡轮询（随机）数组
    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();  //保证按顺序调用已经设定好的负载均衡数组的元素
    public static IRouter IROUTER;  //通过此类对路由层采用的负载均衡算法进行初始化

    //客户端序列化算法
    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;

    public static ClientConfig CLIENT_CONFIG;
    //客户端过滤链
    public static ClientFilterChain CLIENT_FILTER_CHAIN;

}
