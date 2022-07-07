package org.sjtu.common.event;

/**
 * @author hcstart
 * @create 2022-06-18 15:07
 */
public class IRpcDestroyEvent implements IRpcEvent{

    public Object data;

    public IRpcDestroyEvent(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public IRpcEvent setData(Object data) {
        this.data = data;
        return this;
    }
}
