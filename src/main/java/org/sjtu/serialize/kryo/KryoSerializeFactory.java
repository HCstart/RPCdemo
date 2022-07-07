package org.sjtu.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.sjtu.serialize.SerializeFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author hcstart
 * @create 2022-06-18 17:13
 */
public class KryoSerializeFactory implements SerializeFactory {

    //kryo序列化方式是线程不安全的，用 ThreadLocal为线程储存一个实例
    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>(){
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            return kryo;
        }
    };

    /**
     * 序列化
     */
    @Override
    public <T> byte[] serialize(T t) {
        Output output = null;
        try {
            Kryo kryo = kryos.get();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            output = new Output(os);
            kryo.writeClassAndObject(output, t);

            return output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(output != null){
                output.clear();
            }
        }

    }

    /**
     * 反序列化
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Input input = null;
        try {
            Kryo kryo = kryos.get();

            ByteArrayInputStream is = new ByteArrayInputStream(data);
            input = new Input(is);

            return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(input != null){
                input.close();
            }
        }

    }
}
