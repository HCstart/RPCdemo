package org.sjtu.server;

import org.sjtu.common.event.IRpcDestroyEvent;
import org.sjtu.common.event.IRpcListenerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcstart
 * @create 2022-06-18 12:47
 *
 *
 * 监听 java进程被关闭
 *
 */
public class ApplicationShutdownHook {

    //日志
    public static Logger LOGGER = LoggerFactory.getLogger(ApplicationShutdownHook.class);

    /**
     * 注册一个 shutdownHook的钩子，当jvm进程关闭时触发
     */
    public static void registryShutdownHook(){

        //创建了一个新线程
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {

                LOGGER.info("[registryShutdownHook] ==== ");

                //同步等待事件销毁
                IRpcListenerLoader.sendSyncEvent(new IRpcDestroyEvent("destroy"));

                System.out.println("destroy");
            }
        }));
    }




}
