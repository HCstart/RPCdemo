package org.sjtu.server;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.sjtu.common.RpcInvocation;
import org.sjtu.common.RpcProtocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.sjtu.common.cache.CommonServerCache.*;

/**
 * @author hcstart
 * @create 2022-06-15 14:41
 *
 * 采用阻塞队列与线程池处理业务逻辑后，变为非共享模式，不存在线程安全问题
 *
 * 可以使用 @ChannelHandler.Sharable
 */
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 重写read方法
     *
     * 数据经过编写之后，会到达此 handler   在此处执行过滤 SERVER_FILTER_CHAIN
     *
     * 此方法存在一个重大性能问题：一旦出现堵塞，将会影响其他服务的远程调用
     *
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InvocationTargetException, IllegalAccessException {

        //采用 ServerChannelReadData包装整体接收到的数据，交给请求分发器处理
        ServerChannelReadData serverChannelReadData = new ServerChannelReadData();
        serverChannelReadData.setRpcProtocol((RpcProtocol) msg);
        serverChannelReadData.setChannelHandlerContext(ctx);
        //放入channel分发器
        SERVER_CHANNEL_DISPATCHER.add(serverChannelReadData);

        /**  采用线程池与阻塞队列处理业务逻辑后，整段代码转移到类ServerChannelDispatcher（请求分发器）的线程池的逻辑中

         RpcProtocol rpcProtocol = (RpcProtocol) msg;
//        String json = new String(rpcProtocol.getContent(), 0, rpcProtocol.getContentLength());   //此处getContentLength是不是有问题，没有进行初始化
////        String json = new String(rpcProtocol.getContent(), 0, rpcProtocol.getContent().length);
//        //将发送过来的实际数据转换成 RpcInvocation类格式，RpcInvocation类主要包括请求的目标信息、uuid等
//        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);
        //序列化层中对序列化方式进行优化
        RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(),RpcInvocation.class);

        //扩展功能
        //服务端日志记录
//        doLogRecord(rpcInvocation);
        //调用 token的鉴权操作
//        doInvokeTokenVerify(rpcInvocation);

        //执行过滤链路
        SERVER_FILTER_CHAIN.doFilter(rpcInvocation);

        //这里的 PROVIDER_CLASS_MAP就是一开始预先在启动时候存储的 Bean集合
        Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());  //根据服务名称获取相应的服务（一个接口实现类）
        Method[] methods = aimObject.getClass().getDeclaredMethods();
        Object result = null;
        for(Method method : methods){
            if(method.getName().equals(rpcInvocation.getTargetMethod())){
                //通过反射找到目标对象，然后执行目标方法并返回对应值
                if(method.getReturnType().equals(Void.TYPE)){  //判断调用的方法的返回值是否为 void
                    method.invoke(aimObject, rpcInvocation.getArgs());
                } else {
                    result = method.invoke(aimObject, rpcInvocation.getArgs());
                }
            }
            break;
        }

        //将响应 response封装成 RpcProtocol返回
        rpcInvocation.setResponse(result);
//        RpcProtocol responseRpcProtocol = new RpcProtocol(JSON.toJSONString(rpcInvocation).getBytes());  //将rpcInvocation转换成RpcProtocol后发送
        //序列化层中对序列化方式进行优化
        RpcProtocol responseRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
        ctx.writeAndFlush(responseRpcProtocol);

         */
    }

    /**
     * 异常处理方法
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //输出异常
        cause.printStackTrace();

        //关闭通道
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }
}
