package org.sjtu.common.event;

import org.sjtu.common.event.listener.IRpcListener;
import org.sjtu.common.event.listener.ProviderNodeDataChangeListener;
import org.sjtu.common.event.listener.ServiceDestroyListener;
import org.sjtu.common.event.listener.ServiceUpdateListener;
import org.sjtu.common.utils.CommonUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hcstart
 * @create 2022-06-16 15:08
 *
 * 用于监听变化，发送事件
 *
 * 加载监听器，监听到事件后发送给相应类处理后续事务
 *
 */
public class IRpcListenerLoader {

    //定义只包含 2个线程的线程池
    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    private static List<IRpcListener> iRpcListenerList = new ArrayList<>();
    //监听 业务注册
    public static void registerListener(IRpcListener iRpcListener){
        iRpcListenerList.add(iRpcListener);
    }
    //先初始化一个
    public void init() {
        //监听服务更新
        registerListener(new ServiceUpdateListener());
        //监听数据节点（服务端）本身变化（比如权重）
        registerListener(new ProviderNodeDataChangeListener());
        //监听jvm关闭（服务端关闭）
        registerListener(new ServiceDestroyListener());

    }

    /**
     * 向客户端发送数据 （针对 服务更新（上下线等） 与 服务端本身变化（权重变化等））
     */
    public static void sendEvent(IRpcEvent iRpcEvent){
        if(CommonUtils.isEmptyList(iRpcListenerList)){  //
            return;
        }
        for(IRpcListener<?> iRpcListener : iRpcListenerList){
            Class<?> type = getInterfaceT(iRpcListener);
            if(type.equals(iRpcEvent.getClass())){     //监听器的泛型必须与发送过来的事件类型一致，区分不同类的事件？？？
                eventThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            //调用接口实现类 ServiceUpdateListener 或者 ProviderNodeDataChangeListener 里面的重写方法
                            //看发生的具体是啥事件
                            iRpcListener.callBack(iRpcEvent.getData());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

    }

    /**
     * 同步事件处理，可能会阻塞：在（jvm线程关闭）服务器关闭时调用
     */
    public static void sendSyncEvent(IRpcEvent iRpcEvent){
//        System.out.println(iRpcListenerList);
        if(CommonUtils.isEmptyList(iRpcListenerList)){
            return;
        }
        for(IRpcListener<?> iRpcListener : iRpcListenerList){
            Class<?> type = getInterfaceT(iRpcListener);
            if(type.equals(iRpcEvent.getClass())){
                try {

                    //调用接口实现类  ServiceDestroyListener  里面的重写方法
                    iRpcListener.callBack(iRpcEvent);  //直接调用而不是通过线程池

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取接口上的泛型 T
     */
    public static Class<?> getInterfaceT(Object o){
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if(type instanceof Class<?>){
            return (Class<?>) type;
        }
        return null;
    }




}
