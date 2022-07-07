package org.sjtu.common;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author hcstart
 * @create 2022-06-17 23:57
 *
 * 对 SERVICE_ROUTER_MAP中的 ChannelFutureWrapper，按顺序依次返回
 *
 * 用于路由层的负载均衡
 *
 */
public class ChannelFuturePollingRef {

    private AtomicLong referenceTimes = new AtomicLong();  //原子类

    public ChannelFutureWrapper  getChannelFutureWrapper(ChannelFutureWrapper[] arr){
        //轮询生成的随机数组
//        ChannelFutureWrapper[] arr = SERVICE_ROUTER_MAP.get(serviceName);
        long i = referenceTimes.getAndIncrement();
        int index = (int) (i % arr.length);
        return arr[index];
    }

}
