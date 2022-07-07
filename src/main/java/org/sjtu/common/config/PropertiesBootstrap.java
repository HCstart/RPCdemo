package org.sjtu.common.config;

import java.io.IOException;

import static com.sun.deploy.panel.JreLocator.DEFAULT_TIMEOUT;
import static org.sjtu.common.constants.RpcConstants.*;

/**
 * @author hcstart
 * @create 2022-06-17 16:49
 */
public class PropertiesBootstrap {

    public volatile boolean configIsReady;

    public static final String SERVER_PORT = "irpc.serverPort";
    public static final String REGISTER_ADDRESS = "irpc.registerAddr";
    public static final String APPLICATION_NAME = "irpc.applicationName";
    public static final String PROXY_TYPE = "irpc.proxyType";
    public static final String ROUTER_TYPE = "irpc.router";
    public static final String SERVER_SERIALIZE_TYPE = "irpc.serverSerialize";
    public static final String CLIENT_SERIALIZE_TYPE = "irpc.clientSerialize";

    public static final String CLIENT_DEFAULT_TIME_OUT = "irpc.client.default.timeout";
    public static final String SERVER_BIZ_THREAD_NUMS = "irpc.server.biz.thread.nums";
    public static final String SERVER_QUEUE_SIZE = "irpc.server.queue.size";

    /**
     * 初始化 ServerConfig类
     */
    public static ServerConfig loadServerConfigFromLocal(){
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadServerConfigFromLocal fail,e is {}", e);
        }

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setServerPort(PropertiesLoader.getPropertiesInteger(SERVER_PORT));
        serverConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        serverConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        serverConfig.setServerSerialize(PropertiesLoader.getPropertiesStrDefault(SERVER_SERIALIZE_TYPE,JDK_SERIALIZE_TYPE));
        serverConfig.setServerBizThreadNums(PropertiesLoader.getPropertiesIntegerDefault(SERVER_BIZ_THREAD_NUMS,DEFAULT_THREAD_NUMS));
        serverConfig.setServerQueueSize(PropertiesLoader.getPropertiesIntegerDefault(SERVER_QUEUE_SIZE,DEFAULT_QUEUE_SIZE));
        return serverConfig;
    }

    /**
     * 初始化 clientConfig
     */
    public static ClientConfig loadClientConfigFromLocal(){
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadClientConfigFromLocal fail, e is {}", e);
        }

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        clientConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        clientConfig.setProxyType(PropertiesLoader.getPropertiesStr(PROXY_TYPE));
        clientConfig.setRouterStrategy(PropertiesLoader.getPropertiesStrDefault(ROUTER_TYPE, RANDOM_ROUTER_TYPE));
        clientConfig.setClientSerialize(PropertiesLoader.getPropertiesStrDefault(CLIENT_SERIALIZE_TYPE, JDK_SERIALIZE_TYPE));
        clientConfig.setTimeOut(PropertiesLoader.getPropertiesIntegerDefault(CLIENT_DEFAULT_TIME_OUT,DEFAULT_TIMEOUT));
        return clientConfig;
    }





}
