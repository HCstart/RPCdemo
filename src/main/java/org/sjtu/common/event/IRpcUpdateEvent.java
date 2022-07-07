package org.sjtu.common.event;

/**
 * @author hcstart
 * @create 2022-06-16 15:00
 *
 * 节点更新事件规范
 *
 */
public class IRpcUpdateEvent implements IRpcEvent{

    private Object data;

    public IRpcUpdateEvent(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public IRpcEvent setData(Object data) {
        this.data = data;
        return this;   //把当前类返回，便于链式操纵
    }
}
