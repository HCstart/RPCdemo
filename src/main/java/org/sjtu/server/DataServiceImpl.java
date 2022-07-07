package org.sjtu.server;

import com.sjtu.interfaces.DataService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hcstart
 * @create 2022-06-15 20:21
 */
public class DataServiceImpl implements DataService {

    @Override
    public String sendData(String body) {
        System.out.println("这里是服务提供者，body is " + body);
        return "success";
    }

    @Override
    public List<String> getList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add("idea1");
        arrayList.add("idea2");
        arrayList.add("idea3");
        return arrayList;
    }
}
