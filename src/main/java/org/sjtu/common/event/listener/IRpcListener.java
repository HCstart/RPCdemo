package org.sjtu.common.event.listener;

/**
 * @author hcstart
 * @create 2022-06-16 15:02
 *
 * 当 Zookeeper的某个节点发生数据变动的时候，就会发送一个变更事件，然后由对应的监听器去捕获这些数据并做处理
 *
 * 监听器接口的设计如下:
 *
 */
public interface IRpcListener<T>{

    void callBack(Object t);

}
