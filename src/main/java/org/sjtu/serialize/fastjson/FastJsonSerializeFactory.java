package org.sjtu.serialize.fastjson;

import com.alibaba.fastjson.JSON;
import org.sjtu.serialize.SerializeFactory;

/**
 * @author hcstart
 * @create 2022-06-18 17:26
 */
public class FastJsonSerializeFactory implements SerializeFactory {

    /**
     * 序列化
     */
    @Override
    public <T> byte[] serialize(T t) {
        String jsonStr = JSON.toJSONString(t);
        return jsonStr.getBytes();
    }

    /**
     * 反序列化
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return JSON.parseObject(new String(data), clazz);
    }
}
