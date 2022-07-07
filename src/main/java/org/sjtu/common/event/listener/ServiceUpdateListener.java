package org.sjtu.common.event.listener;

import io.netty.channel.ChannelFuture;
import org.sjtu.client.ConnectionHandler;
import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.common.event.IRpcUpdateEvent;
import org.sjtu.common.event.data.URLChangeWrapper;
import org.sjtu.common.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.sjtu.common.cache.CommonClientCache.CONNECT_MAP;

/**
 * @author hcstart
 * @create 2022-06-16 15:29
 *
 * zk服务提供者节点发生变更时，需要发送事件通知客户端
 *  此时客户端会更新本地的一个目标服务列表，避免向无用的服务发送请求
 *
 *
 */
public class ServiceUpdateListener implements IRpcListener<IRpcUpdateEvent> {

    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUpdateListener.class);

    /**
     * 正式根据注册中心发送过来的事件修改客户端保存的服务信息
     */
    @Override
    public void callBack(Object t) {
        //获取到子节点的数据信息
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper) t;

        //获取关注了该服务的客户端 ？？？
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(urlChangeWrapper.getServiceName());

        //判断关注该服务的客户端是否存在
        if(CommonUtils.isEmptyList(channelFutureWrappers)){
            LOGGER.error("[ServiceUpdateListener] channelFutureWrappers is empty");
            return;
        } else {
            //获取改变了的 url
            List<String> matchProviderUrl = urlChangeWrapper.getProviderUrl();

            Set<String> finalUrl = new HashSet<>();
            List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList();

            //对每个关注了该服务（变化的）的客户端进行遍历
            for(ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers){
                //根据客户端获取访问的ip接口信息（在此之前，客户端通过此ip与接口访问该变化的服务）
                String oldServerAddress = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
                //如果老的 url没有，说明已经被移除了
                if(!matchProviderUrl.contains(oldServerAddress)){
                    continue;
                } else {
                    //将找到的客户端信息记录下来
                    finalChannelFutureWrappers.add(channelFutureWrapper);
                    finalUrl.add(oldServerAddress);
                }
            }

            //此时老的 url已经被移除了，开始检查是否有新的 url
            // ChannelFutureWrapper其实是一个自定义的包装类，将 netty建立好的 ChannelFuture做了一些封装
            List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList();
            for(String newProviderUrl : matchProviderUrl){
                //finalUrl 记录的是老的 url
                if(!finalUrl.contains(newProviderUrl)){

                    ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                    String host = newProviderUrl.split(":")[0];   // [0]是IP地址，[1]是端口号
                    Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);
                    channelFutureWrapper.setPort(port);
                    channelFutureWrapper.setHost(host);

                    ChannelFuture channelFuture = null;
                    try {
                        //根据 host和端口号创建 channelFuture
                        channelFuture = ConnectionHandler.createChannelFuture(host,port);
                        //将创建的 channelFuture赋给 channelFutureWrapper
                        channelFutureWrapper.setChannelFuture(channelFuture);
                        newChannelFutureWrapper.add(channelFutureWrapper);
                        finalUrl.add(newProviderUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            finalChannelFutureWrappers.addAll(newChannelFutureWrapper);

            //最终服务更新
            CONNECT_MAP.put(urlChangeWrapper.getServiceName(), finalChannelFutureWrappers);

        }
    }
}
