package org.sjtu.proxy.javassist;

import org.sjtu.client.RpcReferenceWrapper;
import org.sjtu.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.sjtu.common.cache.CommonClientCache.RESP_MAP;
import static org.sjtu.common.cache.CommonClientCache.SEND_QUEUE;

/**
 * @author hcstart
 * @create 2022-06-15 17:20
 */
public class JavassistInvocationHandler implements InvocationHandler {


    private final static Object OBJECT = new Object();

    private RpcReferenceWrapper rpcReferenceWrapper;

    public JavassistInvocationHandler(RpcReferenceWrapper rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttachments());
        rpcInvocation.setUuid(UUID.randomUUID().toString());

        //将封装好的数据放入 SEND_QUEUE，客户端会依次从中提取任务进行发送
        SEND_QUEUE.add(rpcInvocation);

        //扩展功能：已统一使用责任链模式进行配置
        //直连调用
//        doDirectInvoke(rpcInvocation);
        //服务分组选择
//        doGroupChoice(rpcInvocation);
        //发起调用日志记录
//        doInvokeLogRecord(rpcInvocation);

        //对客户端发送的任务中，如果不需要接收返回结果，就此处直接返回null就行了
        if(rpcReferenceWrapper.isAsync()){
            return null;
        }

        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        //代理类内部将请求放入到发送队列中，等待发送队列发送请求
        long beginTime = System.currentTimeMillis();
        //如果请求数据在指定时间内返回则返回给客户端调用方
        while (System.currentTimeMillis() - beginTime < 3*1000) {
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                return ((RpcInvocation)object).getResponse();
            }
        }
        throw new TimeoutException("client wait server's response timeout!");
    }
}
