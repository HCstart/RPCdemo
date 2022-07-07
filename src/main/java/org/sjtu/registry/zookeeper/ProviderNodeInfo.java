package org.sjtu.registry.zookeeper;

/**
 * @author hcstart
 * @create 2022-06-16 17:21
 *
 * 对服务提供者的节点数据进行封装
 *
 * 包括 服务名称 与 节点ip地址
 *
 */
public class ProviderNodeInfo {

    private String serviceName;

    private String address;

    private Integer weight;

    private String registryTime;

    private String applicationName;

    private String group;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getRegistryTime() {
        return registryTime;
    }

    public void setRegistryTime(String registryTime) {
        this.registryTime = registryTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "ProviderNodeInfo{" +
                "serviceName='" + serviceName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
