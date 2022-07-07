package org.sjtu.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hcstart
 * @create 2022-06-18 19:40
 *
 * rpc远程调用包装类
 *
 */
public class RpcReferenceWrapper<T> {

    private Class<T> aimClass;

    private String group;   //这个 group是干啥的？？？

    private Map<String,Object> attachments = new ConcurrentHashMap<>();

    public Class<T> getAimClass() {
        return aimClass;
    }

    public void setAimClass(Class<T> aimClass) {
        this.aimClass = aimClass;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Object> attatchments) {
        this.attachments = attatchments;
    }

    public String getServiceToken(){
        return String.valueOf(attachments.get("serviceToken"));
    }

    public void setServiceToken(String serviceToken){
        attachments.put("serviceToken",serviceToken);
    }

    public String getUrl(){
        return String.valueOf(attachments.get("url"));
    }

    public void setUrl(String url){
        attachments.put("url",url);
    }

    public boolean isAsync(){
        return Boolean.valueOf(String.valueOf(attachments.get("async")));
    }

    public void setAsync(boolean async){
        this.attachments.put("async",async);
    }

    public void setTimeOut(int timeOut) {
        attachments.put("timeOut", timeOut);
    }

    public String getTimeOUt() {
        return String.valueOf(attachments.get("timeOut"));
    }

}
