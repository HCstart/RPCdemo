package org.sjtu.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.sjtu.common.RpcDecoder;
import org.sjtu.common.RpcEncoder;
import org.sjtu.common.config.PropertiesBootstrap;
import org.sjtu.common.config.ServerConfig;
import org.sjtu.common.event.IRpcListenerLoader;
import org.sjtu.common.utils.CommonUtils;
import org.sjtu.filter.server.ServerFilterChain;
import org.sjtu.filter.server.ServerLogFilterImpl;
import org.sjtu.filter.server.ServerTokenFilterImpl;
import org.sjtu.registry.RegistryService;
import org.sjtu.registry.URL;
import org.sjtu.registry.zookeeper.ZookeeperRegister;
import org.sjtu.serialize.fastjson.FastJsonSerializeFactory;
import org.sjtu.serialize.hessian.HessianSerializeFactory;
import org.sjtu.serialize.jdk.JdkSerializeFactory;
import org.sjtu.serialize.kryo.KryoSerializeFactory;

import static org.sjtu.common.cache.CommonServerCache.*;
import static org.sjtu.common.constants.RpcConstants.*;

/**
 * @author hcstart
 * @create 2022-06-15 10:06
 */
public class Server {

    private static EventLoopGroup bossGroup = null;   //资源的释放没有考虑？
    private static EventLoopGroup WorkerGroup = null;

    private ServerConfig serverConfig;   //配置类，设置接口等信息
    public ServerConfig getServerConfig() {
        return serverConfig;
    }
    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    private static IRpcListenerLoader iRpcListenerLoader;

    /**
     * 初始化方法，服务端启动
     */
    public void startApplication() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();   //初始化处理accept方法的EventLoopGroup
        WorkerGroup = new NioEventLoopGroup();  //初始化处理read/write方法的EventLoopGroup

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, WorkerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);   //与客户端一起设置，可禁用 nagle算法
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);  //设定接收缓冲区大小
        bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)   //调整数据发送时滑动窗口大小
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)    //调整数据发送时滑动窗口大小
                .option(ChannelOption.SO_KEEPALIVE, true);  //设置 TCP连接

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override   //初始化方法
            protected void initChannel(SocketChannel ch) throws Exception {
                System.out.println("初始化provider过程");

                //添加编解码器
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());

                //添加对数据进行处理的 handler
                ch.pipeline().addLast(new ServerHandler());
            }
        });

        //服务端一启动就调用方法将服务注册上去：异步方式，开启了一个新线程
        this.batchExportUrl();

        bootstrap.bind(serverConfig.getServerPort()).sync();   //绑定监听端口（由serverConfig设定），并同步等待连接
    }

    /**
     * 将服务注册到 Zookeeper上，正式暴露服务信息
     */
    public void exportService(ServiceWrapper serviceWrapper){
        Object serviceBean = serviceWrapper.getServiceObj();
        if(serviceBean.getClass().getInterfaces().length == 0){
            throw new RuntimeException("service must have interfaces!");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();
        if(classes.length > 1){
            throw new RuntimeException("service must only have one interfaces!");
        }

        //代理层与注册层连接：
        if(REGISTRY_SERVICE  == null){
            REGISTRY_SERVICE  = new ZookeeperRegister(serverConfig.getRegisterAddr());   //指定 zookeeper本身连接的端口（连接 zookeeper的端口）
        }
        //默认选择对象的第一个实现接口
        Class interfaceClass = classes[0];
        //将需要注册的对象统一放在一个 Map集合中进行管理，用以暴露给客户端，使客户端知晓可以提供的服务与相应的ip
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);

        //创建 服务端可以提供的服务service对应的 URL，便于提交给 zookeeper进行注册
        URL url = new URL();
        url.setServiceName(interfaceClass.getName());
        url.setApplicationName(serverConfig.getApplicationName());
        url.addParameters("host", CommonUtils.getIpAddress());
        url.addParameters("port", String.valueOf(serverConfig.getServerPort()));
        url.addParameters("group", String.valueOf(serviceWrapper.getGroup()));
        url.addParameters("limit", String.valueOf(serviceWrapper.getLimit()));
        PROVIDER_URL_SET.add(url);   //PROVIDER_URL_SET用于保存需要注册的服务
        if(CommonUtils.isNotEmpty(serviceWrapper.getServiceToken())){
            PROVIDER_SERVICE_WRAPPER_MAP.put(interfaceClass.getName(), serviceWrapper);
        }
//        LockSupport.unpark(task);
    }

    /**
     * 开启一个新线程，用于注册服务
     */
    public void batchExportUrl(){
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //需要等待 exportService 方法中生成服务对应的 URL后才能注册，所以线程停止了2.5s
                    //但此方法是不是太粗暴了，能否用其他方法简化一下 wait()/notify() 或者pack()/unpack()
                    Thread.sleep(2500);

//                    LockSupport.park();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //正式对服务进行注册
                for(URL url : PROVIDER_URL_SET){
                    REGISTRY_SERVICE.registry(url);
                }

            }
        });
        task.start();
    }

    /**
     * 初始化 ServerConfig
     */
    public void initServerConfig(){
        //加载基本配置信息，并进行初始化
        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);

        //选择序列化方式
        String serverSerialize = serverConfig.getServerSerialize();
        switch (serverSerialize) {
            case JDK_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new JdkSerializeFactory();
                break;
            case FAST_JSON_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new FastJsonSerializeFactory();
                break;
            case HESSIAN2_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new HessianSerializeFactory();
                break;
            case KRYO_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new KryoSerializeFactory();
                break;
            default:
                throw new RuntimeException("no match serialize type for" + serverSerialize);
        }
        SERVER_CONFIG = serverConfig;  //保证使用的是同一组配置

        //初始化请求分发器中线程池与队列配置
        SERVER_CHANNEL_DISPATCHER.init(SERVER_CONFIG.getServerQueueSize(), SERVER_CONFIG.getServerBizThreadNums());

        //初始化过滤链
        ServerFilterChain serverFilterChain = new ServerFilterChain();
        serverFilterChain.addServerFilter(new ServerLogFilterImpl());
        serverFilterChain.addServerFilter(new ServerTokenFilterImpl());
        SERVER_FILTER_CHAIN = serverFilterChain;
    }

    /**
     * 主方法
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();

        //初始化 serverConfig
//        ServerConfig serverConfig = new ServerConfig();
//        serverConfig.setPort(9090);
//        server.setServerConfig(serverConfig);
        //将上面的代码提取到 initServerConfig方法中，同时用 PropertiesBootstrap类管理配置
        server.initServerConfig();

        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();

        //针对过滤链配置相应的过滤条件
        ServiceWrapper dataServiceServiceWrapper = new ServiceWrapper(new DataServiceImpl(), "dev");
        dataServiceServiceWrapper.setServiceToken("token-a");
        dataServiceServiceWrapper.setLimit(2);
        ServiceWrapper userServiceServiceWrapper = new ServiceWrapper(new UserServiceImpl(), "dev");
        userServiceServiceWrapper.setServiceToken("token-b");
        userServiceServiceWrapper.setLimit(2);

        //服务注册
        server.exportService(new ServiceWrapper(new DataServiceImpl()));   //DataService是一个自定义的服务类，用来测试
        server.exportService(new ServiceWrapper(new UserServiceImpl()));   //UserService也是一个自定义的服务类，用来测试

        //先把销毁过程跑起来，此
        ApplicationShutdownHook.registryShutdownHook();

        //服务端启动
        server.startApplication();

    }

}
