package com.sjtu.interfaces;

import java.util.List;

/**
 * @author hcstart
 * @create 2022-06-15 20:19
 */
public interface DataService {

    /**
     * 发送数据
     */
    String sendData(String body);

    /**
     * 获取数据
     */
    List<String> getList();

}
