package org.sjtu.common.cache;

import io.netty.util.internal.ConcurrentSet;
import org.sjtu.common.config.ServerConfig;
import org.sjtu.dispatcher.ServerChannelDispatcher;
import org.sjtu.filter.server.ServerFilterChain;
import org.sjtu.registry.RegistryService;
import org.sjtu.registry.URL;
import org.sjtu.serialize.SerializeFactory;
import org.sjtu.server.ServiceWrapper;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hcstart
 * @create 2022-06-15 17:40
 *
 * 需要注册的对象统一放在一个 Map集合中进行管理
 *
 * 存储服务端需要注册的接口
 *
 */
public class CommonServerCache {

    //储存服务端的接口与对应的服务（即可以提供的服务）：用于暴露给客户端，告诉客户端可以做的服务与相应的ip
    public static final Map<String,Object> PROVIDER_CLASS_MAP = new ConcurrentHashMap<>();

    //储存服务端的接口，服务等信息：用于提交给 zookeeper进行注册
    public static final Set<URL> PROVIDER_URL_SET = new ConcurrentSet<>();

    //用于注册服务
    public static RegistryService REGISTRY_SERVICE;

    //服务器序列化算法
    public static SerializeFactory SERVER_SERIALIZE_FACTORY;

    public static ServerConfig SERVER_CONFIG;

    //服务端过滤链
    public static ServerFilterChain SERVER_FILTER_CHAIN;

    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();


    //请求分发器对象
    public static ServerChannelDispatcher SERVER_CHANNEL_DISPATCHER = new ServerChannelDispatcher();

}
