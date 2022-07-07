package org.sjtu.proxy.jdk;

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
 * @create 2022-06-15 16:34
 *
 * 各种代理工厂统一使用这个InvocationHandler
 *
 * 作用：
 * 将需要调用的方法名称、服务名称，参数统统都封装好到RpcInvocation当中，然后塞入到一个队列里，并且等待服务端的数据返回
 *
 */
public class JDKClientInvocationHandler implements InvocationHandler {

    private final static Object OBJECT = new Object();

    private RpcReferenceWrapper rpcReferenceWrapper;

    public JDKClientInvocationHandler(RpcReferenceWrapper rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
    }

    /**
     * 客户端调用接口与接收返回信息都通过代理对象完成
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //初始化一个RpcInvocation，并设置相应参数
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());

        //此处注入 uuid，对每一次的请求都做单独区分，uuid时随机生成的，也可以利用一个递增的原子类，保证每次都不一样就行
        rpcInvocation.setUuid(UUID.randomUUID().toString());

        //扩展功能
        //直连调用
//        doDirectInvoke(rpcInvocation);
        //服务分组选择
//        doGroupChoice(rpcInvocation);
        //发起调用日志记录
//        doInvokeLogRecord(rpcInvocation);


        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);  //此处value都用的同一个，在ClientHandler收到回应后会更新接收到的rpcInvocation

        //将请求的参数放入发送队列
        //这一步很重要，将rpcInvocation添加到SEND_QUEUE后，client那边就可以把 rpcInvocation拿出来执行发送请求了
        SEND_QUEUE.add(rpcInvocation);  //此 rpcInvocation还剩一个response参数未设置，在服务器执行后会设置

        long beginTime = System.currentTimeMillis();


        //客户端请求超时的一个判断依据
        //很重要
        //此处利用一个 promise对象延时接收是不是更好一些，算法上更完美
        while(System.currentTimeMillis() - beginTime < 3*1000){
            Object object = RESP_MAP.get(rpcInvocation.getUuid());  //在ClientHandler收到回应后会更新接收到的rpcInvocation
            if(object instanceof RpcInvocation){                   //此处表示服务器那边已经执行完，并将数据返回给了ClientHandler，然后在ClientHandler中更新了RESP_MAP

                //数据返回之后就没必要保留了，可以删除掉，当然必须结合 promise延时接收保证一定能拿到
//                RpcInvocation response = (RpcInvocation) RESP_MAP.remove(rpcInvocation.getUuid());
//                return response;

                return ((RpcInvocation)object).getResponse();     //通过代理将数据返回给client中的代理调用线程
            }
        }

        throw new TimeoutException("client wait server's response timeout!");

    }
}
