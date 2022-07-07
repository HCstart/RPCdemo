package org.sjtu.dispatcher;

import org.sjtu.common.RpcInvocation;
import org.sjtu.common.RpcProtocol;
import org.sjtu.server.ServerChannelReadData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.*;

import static org.sjtu.common.cache.CommonServerCache.*;

/**
 * @author hcstart
 * @create 2022-06-19 15:50
 *
 * 服务端 请求分发器
 *
 * 使用堵塞队列提升吞吐性能，解决 NIOEventLoopGroup处理 handler业务逻辑时可能导致的io阻塞问题
 *
 */
public class ServerChannelDispatcher {

    //阻塞队列
    private BlockingQueue<ServerChannelReadData> RPC_DATA_QUEUE;

    //线程池
    private ExecutorService executorService;

    //初始化逻辑在服务器初始化时执行，即server类中的initServerConfig方法中
    public void init(int queueSize, int bizThreadNums){
        RPC_DATA_QUEUE = new ArrayBlockingQueue<>(queueSize);

        executorService = new ThreadPoolExecutor(bizThreadNums, bizThreadNums,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(512));
    }

    public void add(ServerChannelReadData serverChannelReadData){
        RPC_DATA_QUEUE.add(serverChannelReadData);
    }

    public void startDataConsume(){
        Thread thread = new Thread(new ServerJobCoreHandle());
        thread.start();
    }

    //专门开一个线程用于将阻塞队列RPC_DATA_QUEUE中的任务提交给线程池executorService
    class ServerJobCoreHandle implements Runnable{
        @Override
        public void run() {
            while (true){
                try {
                    ServerChannelReadData serverChannelReadData = RPC_DATA_QUEUE.take();

                    //将RPC_DATA_QUEUE中的任务提交给线程池
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                //这一段是原先在ServerHandler中的逻辑，现把他移到此处处理
                                RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                                //反序列化
                                RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                                //执行过滤链路
                                SERVER_FILTER_CHAIN.doFilter(rpcInvocation);
                                Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                                Method[] methods = aimObject.getClass().getDeclaredMethods();
                                Object result = null;
                                for (Method method : methods) {
                                    if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                                        if (method.getReturnType().equals(Void.TYPE)) {
                                            method.invoke(aimObject, rpcInvocation.getArgs());
                                        } else {
                                            result = method.invoke(aimObject, rpcInvocation.getArgs());
                                        }
                                        break;
                                    }
                                }
                                rpcInvocation.setResponse(result);
                                //序列化
                                RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                                //发送回客户端
                                serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }




}
