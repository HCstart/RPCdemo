package org.sjtu.common.event;

/**
 * @author hcstart
 * @create 2022-06-18 9:51
 */
public class IRpcNodeChangeEvent implements IRpcEvent{

    public Object data;

    public IRpcNodeChangeEvent(Object data){
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
