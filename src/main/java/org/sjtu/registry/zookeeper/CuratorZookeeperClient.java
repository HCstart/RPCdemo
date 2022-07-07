package org.sjtu.registry.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted;


/**
 * @author hcstart
 * @create 2022-06-16 10:46
 *
 * CuratorFramework：是一款连接 zookeeper服务的框架，提供了比较全面的功能，除了基础的节点的操作，节点的监听，还有集群的连接以及重试
 *      CuratorFramework 提供了节点的监听功能，当节点数据变化，修改，会调用注册监听事件
 *
 *
 * 持久化节点：不论链接是否断开以及会话是否失效，都不会删除
 * 临时节点：链接断开 或者 会话失效（sessionTimeoutMs 配置的时间内，会话失效），那么该节点就删除了
 *
 */

/**
 * 对 CuratorFramework的具体实现进行设计，以此完成节点的增、删、改、监视等操作
 *
 */
public class CuratorZookeeperClient extends AbstractZookeeperClient {

    //这个东西很重要，后面需要重点看看
    //用于连接zookeeper，实现增、删、改、监视等功能
    private CuratorFramework client;

    public CuratorZookeeperClient(String zkAddress) {
        this(zkAddress, null, null);
    }

    public CuratorZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetryTimes) {
        super(zkAddress, baseSleepTimes, maxRetryTimes);
        //支持重连：当客户端与zookeeper 连接异常的时候，如网络波动，断开链接，支持重新连接，会话有效这个与节点的属性有关
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(super.getBaseSleepTimes(), super.getMaxRetryTimes());
        if(client == null){
            client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);   //通过工厂对client初始化
            client.start();
        }

    }

    @Override
    public CuratorFramework getClient() {
        return client;
    }

    @Override
    public void updateNodeData(String address, String data) {
        try {
            client.setData().forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNodeData(String path) {
        try {
            byte[] result = client.getData().forPath(path);
            if(result != null){
                return new String(result);
            }
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getChildrenData(String path) {
        try {
            List<String> childrenData = client.getChildren().forPath(path);
            return childrenData;
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void createPersistentData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createPersistentWithSeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void createTemporarySeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTemporaryData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(address, data.getBytes());
        } catch (KeeperException.NoChildrenForEphemeralsException e) {
            try {
                client.setData().forPath(address, data.getBytes());
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    @Override
    public void setTemporaryData(String address, String data) {
        try {
            client.setData().forPath(address, data.getBytes());
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * 服务注销
     */
    @Override
    public void destroy() {
        client.close();
    }

    @Override
    public List<String> listNode(String address) {
        try {
            return client.getChildren().forPath(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean deleteNode(String address) {
        try {
            client.delete().forPath(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existNode(String address) {
        try {
            Stat stat = client.checkExists().forPath(address);
            return stat != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 监听节点数据
     */
    @Override
    public void watchNodeData(String path, Watcher watcher) {
        try {
            client.getData().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听子节点数据
     */
    @Override
    public void watchChildNodeData(String path, Watcher watcher){
        try {
            client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 主测试方法
     */
//    public static void main(String[] args) {
//        AbstractZookeeperClient abstractZookeeperClient = new CuratorZookeeperClient("localhost:2181");
//        abstractZookeeperClient.watchNodeData("/irpc/org.idea.irpc.framework.interfaces.DataService/provider/10.1.21.11:9092",
//                new Watcher() {
//                    @Override
//                    public void process(WatchedEvent watchedEvent) {
//                        System.out.println(watchedEvent.getType());
//                        if(NodeDeleted.equals(watchedEvent.getType())){
////                            ProviderNodeInfo providerNodeInfo = URL.buildURLFromUrlStr(watchedEvent.getPath());
////                            System.out.println(providerNodeInfo);
//                        }
//                    }
//                });
//        while (true){
//
//        }
//    }

}
