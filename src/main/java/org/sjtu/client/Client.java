package org.sjtu.client;

import com.alibaba.fastjson.JSON;
import com.sjtu.interfaces.DataService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.sjtu.common.RpcDecoder;
import org.sjtu.common.RpcEncoder;
import org.sjtu.common.RpcInvocation;
import org.sjtu.common.RpcProtocol;
import org.sjtu.common.config.ClientConfig;
import org.sjtu.common.config.PropertiesBootstrap;
import org.sjtu.common.event.IRpcListenerLoader;
import org.sjtu.common.utils.CommonUtils;
import org.sjtu.filter.client.ClientFilterChain;
import org.sjtu.filter.client.ClientLogFilterImpl;
import org.sjtu.filter.client.DirectInvokeFilterImpl;
import org.sjtu.filter.server.GroupFilterImpl;
import org.sjtu.proxy.javassist.JavassistProxyFactory;
import org.sjtu.proxy.jdk.JDKProxyFactory;
import org.sjtu.registry.URL;
import org.sjtu.registry.zookeeper.AbstractRegister;
import org.sjtu.registry.zookeeper.ZookeeperRegister;
import org.sjtu.router.RandomRouterImpl;
import org.sjtu.router.RotateRouterImpl;
import org.sjtu.serialize.fastjson.FastJsonSerializeFactory;
import org.sjtu.serialize.hessian.HessianSerializeFactory;
import org.sjtu.serialize.jdk.JdkSerializeFactory;
import org.sjtu.serialize.kryo.KryoSerializeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.sjtu.common.cache.CommonClientCache.*;
import static org.sjtu.common.constants.RpcConstants.*;

/**
 * @author hcstart
 * @create 2022-06-15 15:22
 *
 * 客户端
 *
 */
public class Client {

    private Logger logger = LoggerFactory.getLogger(Client.class);  //日志文件

    private static EventLoopGroup clientGroup = null;  //资源的释放没有考虑？

    private ClientConfig clientConfig;   //配置类
    public ClientConfig getClientConfig() {
        return clientConfig;
    }
    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    //实现代理层与注册层得连接
    private AbstractRegister abstractRegister = null;
    private IRpcListenerLoader iRpcListenerLoader = null;

    private Bootstrap bootstrap = new Bootstrap();
    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * 初始化方法，启动客户端
     */
    public RpcReference initClientApplication() throws InterruptedException {
        clientGroup = new NioEventLoopGroup();

        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override   //初始化方法
            protected void initChannel(SocketChannel ch){
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });

        //连接 netty客户端   注册层中先不着急连接
//        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddr(), clientConfig.getPort()).sync();  //同步等待连接建立

        //初始化一个监听器
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();

        //对客户端得配置进行初始化
        // 路由层中对 ClientConfig进行了扩展，后面添加了 initClientConfig()方法对起初始化进行补充
        this.clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();

        CLIENT_CONFIG = this.clientConfig;  //保证使用的是同一个配置

        //开启发送线程   注册层中先不着急开启发送线程
//        this.startClient(channelFuture);

        //注入一个代理工厂，通过配置参数决定使用哪种代理类
        RpcReference rpcReference = null;
        if(JAVASSIST_PROXY_TYPE.equals(clientConfig.getProxyType())){
            rpcReference = new RpcReference(new JavassistProxyFactory());  //初始化代理工厂
        } else {
            rpcReference = new RpcReference(new JDKProxyFactory());
        }
        return rpcReference;
    }

    /**
     * 启动线程前需要预先订阅对应的 dubbo服务
     */
    public void doSubscribeService(Class serviceBean){
        if(abstractRegister == null){
            abstractRegister = new ZookeeperRegister(clientConfig.getRegisterAddr());
        }

        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameters("host", CommonUtils.getIpAddress());
        Map<String, String> result = abstractRegister.getServiceWeightMap(serviceBean.getName());
        URL_MAP.put(serviceBean.getName(), result);

        //订阅服务之后会在 SUBSCRIBE_SERVICE_LIST 中添加相应服务的 url.getServiceName()
        //同时，订阅服务之后也需要将自己添加到 zookeeper节点中，可以方便后期做服务治理的时候使用到
        abstractRegister.subscribe(url);
    }

    /**
     * 开始和各个 provider建立连接
     */
    public void doConnectServer(){
        for(URL providerURL : SUBSCRIBE_SERVICE_LIST){   //遍历 SUBSCRIBE_SERVICE_LIST中的 serviceName
            List<String> providerIps = abstractRegister.getProviderIps(providerURL.getServiceName());   //获取提供了该服务的所有provider
            for(String providerIp : providerIps){
                try {

                    //与各个 provider都建立连接
                    ConnectionHandler.connect(providerURL.getServiceName(), providerIp);   //客户端一初始化就把所有的服务连接都建立好，饿汉式？？

                } catch (InterruptedException e) {
                    logger.error("[doConnectServer] connect fail ", e);
                }
            }

            URL url = new URL();
            url.addParameters("servicePath", providerURL.getServiceName() + "/provider");  //下面的 doAfterSubscribe()方法只需要 serviceName就可对相应的节点进行监听
            url.addParameters("providerIps", JSON.toJSONString(providerIps));

            //客户端在此新增一个订阅的功能
            abstractRegister.doAfterSubscribe(url);   //对提供该服务的所有 provider都进行监听

        }

    }

    /**
     * 开启发送线程，专门从事将数据包发送给服务端
     * 注册层代理层连接时，删除方法参数 channelFuture，channelFuture会通过 CONNECT_MAP统一获取
     */
    private void startClient(){
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }
    /**
     * 异步发送任务信息
     */
    class AsyncSendJob implements Runnable{

        // channelFuture 通过 CONNECT_MAP 统一获取
//        private ChannelFuture channelFuture;
//        public AsyncSendJob(ChannelFuture channelFuture) {
//            this.channelFuture = channelFuture;
//        }

        @Override
        public void run() {
            while (true){
                try {
                    //阻塞模式
                    RpcInvocation rpcInvocation = SEND_QUEUE.take();  //在代理类中会将封装好的任务放入 SEND_QUEUE中，此处则从任务队列中获取任务发送

                    //将 RpcInvocation封装到 RpcProtocol对象中，然后发送给服务器，这里正好对应了服务端中的 ServerHandler
//                    String json = JSON.toJSONString(data);

                    //RpcInvocation对象中不包含魔数、字段长度等信息，此处是否还需要再包装一下 ？？？
                    //不需要了RpcEncoder类中的 handler会进行补充
//                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());

                    //通过 netty通道将数据发送给服务端
//                    channelFuture.channel().writeAndFlush(rpcProtocol);
                    //方法内部会随机获取到所有提供该服务的 provider对应的 channel 中的一个
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(rpcInvocation);

                    if(channelFuture != null){
                        //序列化层对上面序列化代码优化，可以切换序列化方式
                        RpcProtocol rpcProtocol = new RpcProtocol(CLIENT_SERIALIZE_FACTORY.serialize(rpcInvocation ));
                        channelFuture.channel().writeAndFlush(rpcProtocol);  //正式发送数据
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }
    /**
     * todo：后续可以考虑可以加入spi
     * 初始化路由策略：选择使用随机路由还是轮询路由
     */
    public void initClientConfig(){
        //初始化路由策略（负载均衡算法）
        String routerStrategy = clientConfig.getRouterStrategy();

        //初始化负载均衡算法
        switch (routerStrategy) {
            case RANDOM_ROUTER_TYPE:
                IROUTER = new RandomRouterImpl();
                break;
            case ROTATE_ROUTER_TYPE:
                IROUTER = new RotateRouterImpl();
                break;
            default:
                throw new RuntimeException("no match routerStrategy for" + routerStrategy);
        }

        //选择序列化方式
        String clientSerialize = clientConfig.getClientSerialize();
        switch (clientSerialize){
            case JDK_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new JdkSerializeFactory();
                break;
            case FAST_JSON_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new FastJsonSerializeFactory();
                break;
            case HESSIAN2_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new HessianSerializeFactory();
                break;
            case KRYO_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new KryoSerializeFactory();
                break;
            default:
                throw new RuntimeException("no match serialize type for " + clientSerialize);
        }

        //todo 初始化过滤器，指定过滤的顺序
        ClientFilterChain clientFilterChain = new ClientFilterChain();
        clientFilterChain.addClientFilter(new DirectInvokeFilterImpl());
        clientFilterChain.addClientFilter(new GroupFilterImpl());
        clientFilterChain.addClientFilter(new ClientLogFilterImpl());
        CLIENT_FILTER_CHAIN = clientFilterChain;
    }


    /**
     * main方法
     */
    public static void main(String[] args) throws Throwable {

        Client client = new Client();

        //初始化ClientConfig
//        ClientConfig clientConfig = new ClientConfig();
//        clientConfig.setPort(9090);
//        clientConfig.setServerAddr("localhost");
//        client.setClientConfig(clientConfig);

        //获取代理工厂
        RpcReference rpcReference = client.initClientApplication();

        client.initClientConfig(); //初始化配置

        RpcReferenceWrapper<DataService> rpcReferenceWrapper = new RpcReferenceWrapper<>();
        rpcReferenceWrapper.setAimClass(DataService.class);
        rpcReferenceWrapper.setGroup("dev");
        rpcReferenceWrapper.setServiceToken("tokan-a");

        //调用代理工厂，根据接口获取代理对象
        DataService dataService = rpcReference.get(rpcReferenceWrapper);

        client.doSubscribeService(DataService.class);  //订阅 DataService.class

        //将此类中使用的 Bootstrap传到 ConnectionHandler，保证使用的是同一个
        ConnectionHandler.setBootstrap(client.getBootstrap());

        //建立连接
        client.doConnectServer();

        //由于将连接建立过程重新封装到 doConnectServer方法内，而 startClient()必须在连接建立之后，所以将其从原方法内移到此处
        client.startClient();

        for (int i = 0; i < 100; i++) {
            /*
              调用sendData方法后，代理类JDKClientInvocationHandler会执行invoke方法，将任务添加到 SEND_QUEUE，
              客户端就可以拿到然后发给服务器，服务器执行响应（客户端通过代理类获取目标接口等信息）
             */
            String result = dataService.sendData("test");
            System.out.println(result);
            Thread.currentThread().sleep(1000);
        }

    }





}
