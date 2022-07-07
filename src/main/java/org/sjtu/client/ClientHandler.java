package org.sjtu.client;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.sjtu.common.RpcInvocation;
import org.sjtu.common.RpcProtocol;

import static org.sjtu.common.cache.CommonClientCache.CLIENT_SERIALIZE_FACTORY;
import static org.sjtu.common.cache.CommonClientCache.RESP_MAP;

/**
 * @author hcstart
 * @create 2022-06-15 16:08
 *
 * 客户端接收服务端的响应数据
 *
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读操纵时执行
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //客户端和服务端之间的数据都是以 RpcProtocol作为基本协议进行交互的
        RpcProtocol rpcProtocol = (RpcProtocol) msg;

        //从 rpcProtocol中获取传输参数更为详细的RpcInvocation对象字节数组
        byte[] reqContent = rpcProtocol.getContent();

//        String json = new String(reqContent, 0, reqContent.length);
//        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);
        //序列化层对上两句进行优化
        RpcInvocation rpcInvocation = CLIENT_SERIALIZE_FACTORY.deserialize(reqContent, RpcInvocation.class);

        //通过之前发送的 uuid来注入匹配的响应数值
        if(!RESP_MAP.containsKey(rpcInvocation.getUuid())){
            throw new IllegalArgumentException("server response is error");   //发送过去与回收回来的uuid必须一致，才能保证收到的是正确的
        }

        //将请求的响应结构放入一个 Map集合中，集合的 key就是 uuid，这个 uuid在发送请求之前就已经初始化好了，所以只需要起一个线程在后台遍历这个map，查看对应的key是否有响应即可
        //uuid放入map的操纵封装到代理类中进行实现
        RESP_MAP.put(rpcInvocation.getUuid(), rpcInvocation);

        ReferenceCountUtil.release(msg);
    }

    /**
     * 处理异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();

        //channel未关闭的话需要将其关闭
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }
}
