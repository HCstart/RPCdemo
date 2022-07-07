package org.sjtu.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hcstart
 * @create 2022-06-15 10:59
 *
 * 必须要实现序列化接口才能进行序列化
 */
public class RpcInvocation implements Serializable {

    private static final long serialVersionUID = -3611379458492006176L;

    //请求的目标方法
    private String targetMethod;

    //请求的目标服务名称
    private String targetServiceName;

    //请求的参数信息
    private Object[] args;

    private String uuid;  //用于匹配请求和响应的一个关键值，判断获得的响应得否与请求相匹配

    //接口响应的数据塞入这个字段中（如果是异步调用或者 viod类型，这里就为空）
    private Object response;

    //序列化层引进来的是干啥呢？？？
    // 应该是添加过滤链时引入的，此参数在代理类的 invoke方法中被初始化
    // 应该时保存 目标服务 所对应的 可以提供该项服务的 所有节点
    private Map<String, Object> attachments = new HashMap<>();

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Object> attachments) {
        this.attachments = attachments;
    }

    //相应的 get/set方法
    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public void setTargetServiceName(String targetServiceName) {
        this.targetServiceName = targetServiceName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

}
