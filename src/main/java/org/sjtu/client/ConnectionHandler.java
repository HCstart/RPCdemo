package org.sjtu.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.common.RpcInvocation;
import org.sjtu.common.utils.CommonUtils;
import org.sjtu.router.Selector;

import java.util.*;

import static org.sjtu.common.cache.CommonClientCache.*;

/**
 * @author hcstart
 * @create 2022-06-16 16:01
 *
 * 职责：当注册中心的节点 新增 或者移除 或者权重变化的时候，这个类主要负责对内存中的 url做变更
 *
 * 将连接的建立，断开，按照服务名筛选等功能都封装在了一起，按照单一职责的设计原则，将与连接有关的功能都统一封装在了一起。
 *
 * 将对于netty连接的管理操作统一封装在了ConnectionHandler类中
 */
public class ConnectionHandler {

    /**
     * 核心的连接处理器
     * 专门用于负责和服务器端构建连接通信
     *
     * 与 Client类中使用的是同一个
     */
    private static Bootstrap bootstrap;
    public static void setBootstrap(Bootstrap bootstrap){
        ConnectionHandler.bootstrap = bootstrap;
    }

    /**
     * 构建单个连接通道元操作：
     * 既要处理连接，还要统一将连接进行内存存储管理
     */
    public static void connect(String providerServiceName, String providerIp) throws InterruptedException {
        if(bootstrap == null){
            throw new RuntimeException("bootstrap cannot be null!");
        }

        //格式错误类型信息
        if(!providerIp.contains(":")){
            return;
        }

        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];
        Integer port = Integer.valueOf(providerAddress[1]);

        //到底这个 ChannelFuture里面是什么
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();  //同步等待连接建立

        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);

        SERVER_ADDRESS.add(providerIp);

        //获取服务提供者的地址信息，并将其更新到 CONNECT_MAP中
        // 注意 channelFutureWrappers使用的是 ArrayList，意味着可以重复，所以 CONNECT_MAP也会对 每次客户端访问服务端 进行记录
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if(CommonUtils.isEmptyList(channelFutureWrappers)){
            channelFutureWrappers = new ArrayList();
        }
        channelFutureWrappers.add(channelFutureWrapper);
        CONNECT_MAP.put(providerServiceName, channelFutureWrappers);   //将建立好的连接都包装进 CONNECT_MAP
    }

    /**
     * 构建 ChannelFuture
     */
    public static ChannelFuture createChannelFuture(String ip, Integer port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        return channelFuture;
    }

    /**
     * 断开连接
     */
    public static void disConnect(String providerServiceName, String providerIp){
        SERVER_ADDRESS.remove(providerIp);

        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isNotEmptyList(channelFutureWrappers)) {
            Iterator<ChannelFutureWrapper> iterator = channelFutureWrappers.listIterator();
            while (iterator.hasNext()){
                ChannelFutureWrapper channelFutureWrapper = iterator.next();
                if(providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort())) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 默认走随机策略获取 ChannelFuture（负载均衡）
     *
     * 但是直接使用 Random()有很多缺陷
     * 1.假设目标机器的性能不一，如何对机器进行权重分配？
     * 2.每次都要执行Random函数，在高并发情况下对CPU的消耗会比较高。
     * 3.如何基于路由策略做ABTest？
     * 4.......
     *
     * ====>
     * 路由层对此处进行了更新：定义了随机算法与轮询算法两种方式进行负载均衡
     *
     * 过滤链在此处，先执行过滤，在执行负载均衡
     *
     */
    public static ChannelFuture getChannelFuture(RpcInvocation rpcInvocation){
        String providerServiceName = rpcInvocation.getTargetServiceName();
        ChannelFutureWrapper[] channelFutureWrappers = SERVICE_ROUTER_MAP.get(providerServiceName);
        if(channelFutureWrappers == null || channelFutureWrappers.length == 0) {
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }

        //路由层添加之后，在 Client类中initConfig()方法中对负载均衡采取的协议进行了定义
//        //随机获取一个 channelFuture （负载均衡）
//        ChannelFuture channelFuture = channelFutureWrappers.get(new Random().nextInt(channelFutureWrappers.size())).getChannelFuture();

        //在此处调用过滤链进行过滤
        //此时在 writeAndFlush()方法之前，也在客户端的 handler之前
        CLIENT_FILTER_CHAIN.doFilter(Arrays.asList(channelFutureWrappers),rpcInvocation);

        Selector selector = new Selector();  //路由层中用于轮询选择 channelfuture
        selector.setProviderServiceName(providerServiceName);
        selector.setChannelFutureWrappers(channelFutureWrappers);  //经过二次筛选后的channelfuture集合
        ChannelFuture channelFuture = IROUTER.select(selector).getChannelFuture();

        return channelFuture;
    }






}
