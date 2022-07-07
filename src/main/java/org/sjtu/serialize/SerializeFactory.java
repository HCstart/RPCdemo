package org.sjtu.serialize;

/**
 * @author hcstart
 * @create 2022-06-18 16:08
 *
 * 序列化层
 *
 * 基础接口
 *
 */
public interface SerializeFactory {

    /**
     * 序列化
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     */
    <T> T deserialize(byte[] data, Class<T> clazz);


}
