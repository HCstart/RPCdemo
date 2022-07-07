package org.sjtu.serialize.jdk;

import org.sjtu.serialize.SerializeFactory;

import java.io.*;

/**
 * @author hcstart
 * @create 2022-06-18 16:11
 *
 * 利用 jdk自带的方法实现序列化与反序列化
 *
 */
public class JdkSerializeFactory implements SerializeFactory {

    /**
     * 序列化
     *
     * 为何不对 os与 output等进行关闭？？？
     */
    @Override
    public <T> byte[] serialize(T t) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            ObjectOutputStream output = new ObjectOutputStream(os);

            output.writeObject(t);
            output.flush();
            data = os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    /**
     * 反序列化
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        try {
            ObjectInputStream input = new ObjectInputStream(is);
            Object result = input.readObject();

            return ((T) result);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
