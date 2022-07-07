package org.sjtu.common.constants;

/**
 * @author hcstart
 * @create 2022-06-15 13:45
 *
 * 保存协议的魔数
 *
 */

public class RpcConstants {

    //自定义协议使用的魔数：客户端收到的数据的魔数必须是这个，否则认位时非法数据
    public final static short MAGIC_NUMBER = 19812;

    //使用的动态代理类型（代理层）
    public static final String JDK_PROXY_TYPE = "jdk";
    public static final String JAVASSIST_PROXY_TYPE = "javassist";

    //使用的负载均衡方法（路由层）
    public static final String RANDOM_ROUTER_TYPE = "random";
    public static final String ROTATE_ROUTER_TYPE = "rotate";

    //序列化方法（序列化层）
    public static final String JDK_SERIALIZE_TYPE = "jdk";
    public static final String FAST_JSON_SERIALIZE_TYPE = "fastJson";
    public static final String HESSIAN2_SERIALIZE_TYPE = "hessian2";
    public static final String KRYO_SERIALIZE_TYPE = "kryo";

    //客户端访问超时时间
    public static final Integer DEFAULT_TIMEOUT = 3000;

    //默认请求分发器最大线程数与阻塞队列长度
    public static final Integer DEFAULT_THREAD_NUMS = 5;
    public static final Integer DEFAULT_QUEUE_SIZE = 10;   //此处只作测试，实际远比这个大--512，上面那个是256

}