package org.sjtu.common.config;

/**
 * @author hcstart
 * @create 2022-06-15 17:25
 *
 * 服务端配置类
 */
public class ServerConfig {

    private Integer serverPort;  //服务对应的监听端口

    private String registerAddr;  //注册地址，zookeeper本身使用的地址

    private String applicationName;  //应用名称，本项目中统一为：irpc-provider

    /**
     * 服务端序列化方式  example:hessian2,kryo,fastJson,jdk
     */
    private String serverSerialize;

    /**
     * 情趣分发器中处理服务端业务线程数目与接收队列大小
     */
    private Integer serverBizThreadNums;
    private Integer serverQueueSize;

    public Integer getServerBizThreadNums() {
        return serverBizThreadNums;
    }

    public void setServerBizThreadNums(Integer serverBizThreadNums) {
        this.serverBizThreadNums = serverBizThreadNums;
    }

    public Integer getServerQueueSize() {
        return serverQueueSize;
    }

    public void setServerQueueSize(Integer serverQueueSize) {
        this.serverQueueSize = serverQueueSize;
    }

    public String getServerSerialize() {
        return serverSerialize;
    }

    public void setServerSerialize(String serverSerialize) {
        this.serverSerialize = serverSerialize;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
