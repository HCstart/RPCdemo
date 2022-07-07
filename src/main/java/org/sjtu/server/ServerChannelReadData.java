package org.sjtu.server;

import io.netty.channel.ChannelHandlerContext;
import org.sjtu.common.RpcProtocol;

/**
 * @author hcstart
 * @create 2022-06-19 15:55
 *
 * 用于将服务器的业务逻辑提取出来   通过请求分发器解决高并发阻塞
 *
 * 对服务器中 handler接收的信息进行封装
 *
 */
public class ServerChannelReadData {

    private RpcProtocol rpcProtocol;

    private ChannelHandlerContext channelHandlerContext;

    public RpcProtocol getRpcProtocol() {
        return rpcProtocol;
    }

    public void setRpcProtocol(RpcProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }
}
