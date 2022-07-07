package org.sjtu.router;

import org.sjtu.common.ChannelFutureWrapper;
import org.sjtu.registry.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.sjtu.common.cache.CommonClientCache.*;

/**
 * @author hcstart
 * @create 2022-06-17 22:56
 *
 * 随机调用核心算法
 *
 * 具体使用随机调用还是轮询调用有配置文件指定
 *
 */
public class RandomRouterImpl implements IRouter{

    /**
     * 刷新路由数组
     *
     * 负载均衡算法：
     * 1：提前生成调用先后顺序的随机数组
     */
    @Override
    public void refreshRouterArr(Selector selector) {
        //获取提供selector服务的所有提供者
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];

        //提前生成调用先后顺序的随机数组
        int[] result = createRandomIndex(arr.length);

        for(int i = 0; i < arr.length; i++){
            arr[i] = channelFutureWrappers.get(result[i]);
        }

        //每个服务都需要一个随机数组
        SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(), arr);
    }

    /**
     * 获取到（请求到）连接通道  --- 对外暴露的核心方法，每次外界调用服务的时候都是通过这个函数去获取下一次调用的provider信息
     */
    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getChannelFutureWrappers());
    }

    /**
     * 更新权重信息
     */
    @Override
    public void updateWeight(URL url) {
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(url.getServiceName());
        Integer[] weightArr = createWeightArr(channelFutureWrappers);
        Integer[] finalArr = createRandomArr(weightArr);   //这个只用到了他的长度啊，为啥不只传一个长度过来，怎么回事？？？

        ChannelFutureWrapper[] finalChannelFutureWrappers = new ChannelFutureWrapper[finalArr.length];
        for(int j = 0; j < finalArr.length; j++){
//            finalChannelFutureWrappers[j] = channelFutureWrappers.get(j);
            finalChannelFutureWrappers[j] = channelFutureWrappers.get(finalArr[j]);  //此处应该是这样
        }

        SERVICE_ROUTER_MAP.put(url.getServiceName(), finalChannelFutureWrappers);
    }

    /**
     * 创建权重数组，权重数组是根据每一个提供相同服务的服务端的权重生成
     *
     * weight是指权重，权重值约定好配置是100的整数倍
     */
    private static Integer[] createWeightArr(List<ChannelFutureWrapper> channelFutureWrappers){
        List<Integer> weightArr = new ArrayList<>();
        for(int k = 0; k < channelFutureWrappers.size(); k++){
            Integer weight = channelFutureWrappers.get(k).getWeight();
            int c = weight / 100;
            for(int i = 0; i < c; i++){
                weightArr.add(k);  //添加 k（对应 1个 channelFutureWrapper） 的次数取决于本身的权重 c
            }
        }
        Integer[] arr = new Integer[weightArr.size()];
        return weightArr.toArray(arr);
    }

    /**
     * 创建随机乱序数组  ---这用的是什么算法？？？
     */
    private static Integer[] createRandomArr(Integer[] arr){
        int total = arr.length;
        Random ra = new Random();
        for(int i = 0; i < total; i++){
            int j = ra.nextInt(total);  //生成 [0,total)的随机数
            if(i == j){
                continue;
            }
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }
    private int[] createRandomIndex(int len){
        int[] arrInt = new int[len];
        for(int i = 0; i < len; i++){
            arrInt[i] = -1;
        }
        Random ra = new Random();
        int index = 0;
        while (index < len){
            int num = ra.nextInt(len); //生成 [0,len)的随机数，用的是同一个 Random随机种子
            //如果数组中不包含这个元素则赋值给数组
            if(!contains(arrInt, num)){
                arrInt[index++] = num;
            }
        }
        return arrInt;
    }

    private boolean contains(int[] arr, int key) {
        for (int i = 0; i < arr.length; i++) {
            if(arr[i] == key){
                return true;
            }
        }
        return false;
    }

    /**
     * 用来测试的主方法
     */
//    public static void main(String[] args) {
//        List<ChannelFutureWrapper> channelFutureWrappers = new ArrayList<>();
//        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 100));
//        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 200));
//        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 9300));
//        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 400));
//        Integer[] r = createWeightArr(channelFutureWrappers);
//        System.out.println(r);
//    }


}
