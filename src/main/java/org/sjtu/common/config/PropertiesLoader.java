package org.sjtu.common.config;

import org.sjtu.common.utils.CommonUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author hcstart
 * @create 2022-06-17 16:54
 *
 * 配置加载器
 *
 */
public class PropertiesLoader {

    private static Properties properties;

    private static Map<String, String> propertiesMap = new HashMap<>();

    private static String DEFAULT_PROPERTIES_FILE = "D:\\JAVA-IDEA\\irpc-framework-master\\irpc-framework-core\\src\\main\\resources\\irpc.properties";

    /**
     * 将配置文件加载到配置属性 properties中去
     */
    public static void loadConfiguration() throws IOException {
        if(properties != null){
            return;
        }

        properties = new Properties();
        FileInputStream is = null;
        is = new FileInputStream(DEFAULT_PROPERTIES_FILE);
        properties.load(is);
        is.close();
    }

    /**
     * 根据键值获取配置属性（String）
     */
    public static String getPropertiesStr(String key){
        if(properties == null){
            return null;
        }
        if(CommonUtils.isEmpty(key)){
            return null;
        }
        if(!propertiesMap.containsKey(key)){
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return String.valueOf(propertiesMap.get(key));
    }

    /**
     * 根据键值获取配置属性（Integer）
     */
    public static Integer getPropertiesInteger(String key){
        if(properties == null){
            return null;
        }
        if(CommonUtils.isEmpty(key)){
            return null;
        }
        if (!propertiesMap.containsKey(key)) {
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return Integer.valueOf(propertiesMap.get(key));
    }

    public static String getPropertiesNotBlank(String key){
        String val = getPropertiesStr(key);
        if(val == null || val.equals("")){
            throw new IllegalArgumentException(key + "配置为空异常");
        }
        return val;
    }

    public static String getPropertiesStrDefault(String key, String defaultVal){
        String val = getPropertiesStr(key);
        return (val == null || val.equals("")) ? defaultVal : val;
    }

    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static Integer getPropertiesIntegerDefault(String key,Integer defaultVal) {
        if (properties == null) {
            return defaultVal;
        }
        if (CommonUtils.isEmpty(key)) {
            return defaultVal;
        }
        String value = properties.getProperty(key);
        if(value==null){
            propertiesMap.put(key, String.valueOf(defaultVal));
            return defaultVal;
        }
        if (!propertiesMap.containsKey(key)) {
            propertiesMap.put(key, value);
        }
        return Integer.valueOf(propertiesMap.get(key));
    }



}
