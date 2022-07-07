package org.sjtu.common.config;

/**
 * @author hcstart
 * @create 2022-06-15 17:23
 *
 * 客户端配置类
 *
 *
 */
public class ClientConfig {

//    private Integer port;   //访问端口通过 properties配置文件设置

    private String registerAddr;  //访问地址（注册地址）

    private String proxyType; //客户端访问服务端使用的代理类

    public String applicationName;  //应用名称：本项目中为 irpc-provider

    /**
     * 负载均衡策略
     */
    private String routerStrategy;  //路由层添加

    /**
     * 客户端序列化方式  example:hessian2,kryo,fastJson,jdk
     */
    private String clientSerialize;

    /**
     * 客户端发数据的超时时间
     */
    private Integer timeOut;

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

    public String getClientSerialize() {
        return clientSerialize;
    }

    public void setClientSerialize(String clientSerialize) {
        this.clientSerialize = clientSerialize;
    }

    public String getRouterStrategy() {
        return routerStrategy;
    }

    public void setRouterStrategy(String routerStrategy) {
        this.routerStrategy = routerStrategy;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
