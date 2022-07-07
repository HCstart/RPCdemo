package org.sjtu.registry.zookeeper;

import com.alibaba.fastjson.JSON;
import com.sjtu.interfaces.DataService;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.sjtu.common.event.IRpcEvent;
import org.sjtu.common.event.IRpcListenerLoader;
import org.sjtu.common.event.IRpcNodeChangeEvent;
import org.sjtu.common.event.IRpcUpdateEvent;
import org.sjtu.common.event.data.URLChangeWrapper;
import org.sjtu.registry.RegistryService;
import org.sjtu.registry.URL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hcstart
 * @create 2022-06-16 11:17
 *
 * 实现注册服务接口:
 *
 * 此类作用：对 Zookeeper完成服务注册，服务订阅，服务下线等相关实际操作 （执行具体的业务）
 *
 */
public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    // 获取对象 AbstractZookeeperClient，创建的是其子类 CuratorZookeeperClient的对象
    private AbstractZookeeperClient zkClient;  //这个很重要，操纵中通过操纵该对象类的子类来 完成节点的增、删、改、监视等操作
    public ZookeeperRegister(String address) {
        this.zkClient = new CuratorZookeeperClient(address);
    }

    public AbstractZookeeperClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(AbstractZookeeperClient zkClient) {
        this.zkClient = zkClient;
    }

    private String ROOT = "/irpc";

    private String getProviderPath(URL url){
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters();
    }

    private String getConsumerPath(URL url){
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName();
    }

    @Override
    public List<String> getProviderIps(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        return nodeDataList;
    }

    /**
     * 将服务注册到 zookeeper上
     */
    @Override
    public void registry(URL url) {
        if(!this.zkClient.existNode(ROOT)){
            zkClient.createPersistentData(ROOT, "");
        }

        String urlStr = URL.buildProviderUrlStr(url);
        if(!zkClient.existNode(getProviderPath(url))){
            zkClient.createTemporaryData(getProviderPath(url), urlStr);   //创建的是临时节点，连接断开 或者 会话失效，该节点删除
        } else {
            //删了重新创建，相当于重新注册服务
            zkClient.deleteNode(getProviderPath(url));
            zkClient.createTemporaryData(getProviderPath(url), urlStr);
        }

        //在 Server 中已经添加到 PROVIDER_URL_SET了，实际上应该不需要了
        super.registry(url);   //相应的 PROVIDER_URL_SET也应该添加上去
    }

    /**
     * 服务下线
     */
    @Override
    public void unRegister(URL url) {
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);   //PROVIDER_URL_SET 上面对应的要删除
    }

    /**
     * 客户端取消订阅
     */
    @Override
    public void doUnSubscribe(URL url) {
        this.zkClient.deleteNode(getConsumerPath(url));
        super.doUnSubscribe(url);  //SUBSCRIBE_SERVICE_LIST 上面要对应删除
    }

    /**
     * 获取服务权重
     */
    @Override
    public Map<String, String> getServiceWeightMap(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        Map<String, String> result = new HashMap<>();
        for (String ipAndHost : nodeDataList) {
            String childData = this.zkClient.getNodeData(ROOT + "/" + serviceName + "/provider/" + ipAndHost);
            result.put(ipAndHost, childData);
        }
        return result;
    }

    @Override
    public void doBeforeSubscribe(URL url) {

    }  //订阅前动作，可自行扩展

    /**
     * 客户端订阅
     *
     * 客户端订阅服务会将自己也添加到 zookeeper节点中
     */
    @Override
    public void subscribe(URL url) {
        if(!this.zkClient.existNode(ROOT)){
            zkClient.createPersistentData(ROOT, "");  //根节点是持久的
        }

        String urlStr = URL.buildConsumerUrlStr(url);
        if(!zkClient.existNode(getConsumerPath(url))){
            zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);  //必须是有序的，服务端是根据客户端发出请求的顺序执行 FIFO模式
        } else {
            zkClient.deleteNode(getConsumerPath(url));
            zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        }
        super.subscribe(url);   //SUBSCRIBE_SERVICE_LIST 上面要添加
    }

    /**
     * 订阅后动作，订阅后必须对订阅的节点进行监听
     */
    @Override
    public void doAfterSubscribe(URL url) {
        //监听是否有新的服务注册：订阅成功后，就要对订阅的对象进行监听，看是否有变化，有变化则立刻通知客户端
//        String newServerNodePath = ROOT + "/" + url.getServiceName() + "/provider";
        String servicePath = url.getParameters().get("servicePath");
        String newServerNodePath = ROOT + "/" + servicePath;

        //调用下面的监听方法
        watchChildNodeData(newServerNodePath);  //监听服务上下线

        //监听服务节点内部数据的变化
        // 路由层添加的部分，主要用于监听服务端 权重 的变化
        String providerIpStrJson = url.getParameters().get("providerIps");
        List<String> providerIpList = JSON.parseObject(providerIpStrJson, List.class);
        for(String providerIp : providerIpList){
            //监听节点本身数据变化  比如权重
            this.watchNodeDataChange(ROOT + "/" + servicePath + "/" + providerIp);
        }
    }

    /**
     * 订阅服务节点内部的数据变化:主要用于监听服务端 权重 的变化
     */
    public void watchNodeDataChange(String newServerNodePath){
        zkClient.watchNodeData(newServerNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                String path = watchedEvent.getPath();
                String nodeData = zkClient.getNodeData(path);
                nodeData = nodeData.replace(";", "/");
                ProviderNodeInfo providerNodeInfo = URL.buildURLFromUrlStr(nodeData);

                //自定义 节点数据变化 的事件类型，
                IRpcEvent iRpcEvent = new IRpcNodeChangeEvent(providerNodeInfo);
                IRpcListenerLoader.sendEvent(iRpcEvent);

                //再调用自己，保证一直可以监听
                watchNodeDataChange(newServerNodePath);
            }
        });
    }

    /**
     * 监听节点
     *
     * 这个监听过程很关键
     * 同时监听过程中节点出现变化，会通过自定义事件组件将变化传送给客户端进行相应的改变
     */
    public void watchChildNodeData(String newServerNodePath){
        zkClient.watchChildNodeData(newServerNodePath, new Watcher() {
            //调用 process方法进行监听，当节点发生变化时，会执行下面的方法
            @Override
            public void process(WatchedEvent watchedEvent) {

//                System.out.println("watchedEvent: " + watchedEvent);

                String path = watchedEvent.getPath(); //获取产生变化的节点路径
                List<String> childrenDataList = zkClient.getChildrenData(path);   //根据路径获取新的数据（变化后的数据）
                // URLChangeWrapper 用来记录变化信息，通过事件传递给客户端
                URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
                urlChangeWrapper.setProviderUrl(childrenDataList);     //服务的url，
                urlChangeWrapper.setServiceName(path.split("/")[2]);  //服务名称

                // 这一步很重要
                //在此处调用事件监听数组，与监听器联系起来
                //自定义的一套事件监听组件   解耦（自定义事件，实现节点监听与给客户端发送响应的解耦）：设计一个事件类专门用于发送更新信息
                IRpcEvent iRpcEvent = new IRpcUpdateEvent(urlChangeWrapper);  //将新的数据根据节点更新规范重新设置
                IRpcListenerLoader.sendEvent(iRpcEvent);   //发送事件，客户端进行修改

                // zk节点的消息通知其实是只具有一次性的功效
                // 因此，收到回调之后在注册一次监听，这样能保证一直在监听，一直都收到消息
                watchChildNodeData(path);    //重要
            }
        });
    }

    /**
     * 主线程：用于启动 zookeeper
     */
    public static void main(String[] args) throws InterruptedException {

        ZookeeperRegister zookeeperRegister = new ZookeeperRegister("localhost:2181");   //指定 zookeeper本身连接的端口

        //这一步是什么用
        List<String> urls = zookeeperRegister.getProviderIps(DataService.class.getName());
        System.out.println("urls: " + urls);

        AbstractZookeeperClient abstractZookeeperClient = zookeeperRegister.getZkClient();
        String path = "/irpc/org.idea.irpc.framework.interfaces.DataService/provider/192.168.43.227:9093";
        String nodeData = abstractZookeeperClient.getNodeData(path);
        abstractZookeeperClient.watchNodeData(path, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(watchedEvent.getPath());
            }
        });

        Thread.sleep(2000000);  //等待200000秒     目的应该是保持zookeeper运行？？？

    }
}
