package org.sjtu.common;

import io.netty.channel.ChannelFuture;

/**
 * @author hcstart
 * @create 2022-06-16 17:06
 *
 * 对 ChannelFuture进行包装，并提供 channelFuture访问的 ip与 port
 *
 */
public class ChannelFutureWrapper {

    private ChannelFuture channelFuture;  //关键参数

    private String host;

    private Integer port;

    private Integer weight;  //对应着 提供相同服务的服务端 的权重，用于路由层随机负载均衡，堆积获取ChannelFuture

    private String group;   //对服务端服务器进行分组

    public ChannelFutureWrapper() {
    }

    public ChannelFutureWrapper(String host, Integer port, Integer weight) {
        this.host = host;
        this.port = port;
        this.weight = weight;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "ChannelFutureWrapper{" +
                "channelFuture=" + channelFuture +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", weight=" + weight +
                '}';
    }
}
