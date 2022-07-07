package org.sjtu.registry;

import org.sjtu.registry.zookeeper.ProviderNodeInfo;

import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * @author hcstart
 * @create 2022-06-15 22:56
 */
public class URL {

    /**
     * 服务应用名称
     */
    private String applicationName;

    /**
     * 注册到节点的服务名称，例如：com.sise.test.UserService
     */
    private String serviceName;

    /**
     * 这里可以自定义进行扩展
     * 分组
     * 权重
     * 服务提供者的地址
     * 服务提供者的端口
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * 将 URL转换为写入 zookeeper的 provider节点下的一段字符串
     */
    public static String buildProviderUrlStr(URL url){
        String host = url.getParameters().get("host");
        String port = url.getParameters().get("port");
        String group = url.getParameters().get("group");
        return new String((url.getApplicationName() + ";" + url.getServiceName()
                + ";" + host + ":" + port + ";" + System.currentTimeMillis() + ";100;" + group).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 将 URL转换为写入 zk的 consumer节点的下一段字符串
     */
    public static String buildConsumerUrlStr(URL url){
        String host = url.getParameters().get("host");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 将某个节点下的信息转换为一个 provider节点对象
     */
    public static ProviderNodeInfo buildURLFromUrlStr(String providerNodeStr){
        String[] items = providerNodeStr.split(";");
        ProviderNodeInfo providerNodeInfo = new ProviderNodeInfo();
        providerNodeInfo.setApplicationName(items[0]);
        providerNodeInfo.setServiceName(items[1]);
        providerNodeInfo.setAddress(items[2]);
        providerNodeInfo.setRegistryTime(items[3]);
        providerNodeInfo.setWeight(Integer.valueOf(items[4]));
        providerNodeInfo.setGroup(String.valueOf(items[5]));
        return providerNodeInfo;
    }


    public void addParameters(String key, String value){
        this.parameters.putIfAbsent(key, value);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * 主方法测试
     * @param args
     */
//    public static void main(String[] args) {
//        buildURLFromUrlStr("/irpc/org.idea.irpc.framework.interfaces.DataService/provider/192.168.43.227:9092");
//    }

}
