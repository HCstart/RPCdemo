package org.sjtu.common.utils;

import org.sjtu.common.ChannelFutureWrapper;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author hcstart
 * @create 2022-06-15 20:07
 *
 *
 */
public class CommonUtils {

    /**
     * 获取目标对象的所有实现接口
     */
    public static List<Class<?>> getAllInterfaces(Class targetClass){
        if(targetClass == null){
            throw new IllegalArgumentException("targetClass is null");
        }

        Class[] clazz = targetClass.getInterfaces();

        if(clazz.length == 0){
            return Collections.emptyList();
        }

        List<Class<?>> classes = new ArrayList<>(clazz.length);

        for(Class aclass : clazz){   //class是关键字，不能单独使用作为变量
            classes.add(aclass);
        }

        return classes;
    }

    /**
     * 获取 ip地址
     * @return
     */
    public static String getIpAddress(){
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while(allNetInterfaces.hasMoreElements()){
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if(netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()){
                        ip = addresses.nextElement();
                        if(ip != null && ip instanceof Inet4Address){
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("IP地址获取失败" + e.toString());
        }
        return "";
    }

    public static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }

    public static boolean isEmptyList(List list){
        if(list == null || list.size() == 0){
            return true;
        }
        return false;
    }

    public static boolean isNotEmptyList(List list){
        return !isEmptyList(list);
    }

    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }

    public static ChannelFutureWrapper[] convertFromList(List<ChannelFutureWrapper> channelFutureWrappers){
        ChannelFutureWrapper[] channelFutureWrappersArr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        for(int i=0;i<channelFutureWrappers.size();i++){
            channelFutureWrappersArr[i] = channelFutureWrappers.get(i);
        }
        return channelFutureWrappersArr;
    }


}
