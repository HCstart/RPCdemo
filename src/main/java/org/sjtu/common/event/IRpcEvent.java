package org.sjtu.common.event;

/**
 * @author hcstart
 * @create 2022-06-16 14:57
 *
 * 设计 zookeeper中事件监听机制
 *
 *
 * 定义一个抽象的事件，该事件会用于装载需要传递的数据信息
 *
 */
public interface IRpcEvent {

    Object getData();

    IRpcEvent setData(Object data);

}
