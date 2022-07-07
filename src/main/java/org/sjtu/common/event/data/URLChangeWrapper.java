package org.sjtu.common.event.data;

import java.util.List;

/**
 * @author hcstart
 * @create 2022-06-16 17:10
 *
 * 对 URL进行包装，提供 serviceName 与 providerUrl
 *
 */
public class URLChangeWrapper {

    private String serviceName;

    private List<String> providerUrl;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(List<String> providerUrl) {
        this.providerUrl = providerUrl;
    }

    @Override
    public String toString() {
        return "URLChangeWrapper{" +
                "serviceName='" + serviceName + '\'' +
                ", providerUrl=" + providerUrl +
                '}';
    }
}
