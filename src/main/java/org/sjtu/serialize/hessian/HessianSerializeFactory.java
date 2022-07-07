package org.sjtu.serialize.hessian;


import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.sjtu.serialize.SerializeFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author hcstart
 * @create 2022-06-18 16:41
 *
 * Hessian序列化技术
 *
 */
public class HessianSerializeFactory implements SerializeFactory {

    /**
     * 序列化
     */
    @Override
    public <T> byte[] serialize(T t) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            Hessian2Output output = new Hessian2Output(os);

            output.writeObject(t);
            output.getBytesOutputStream().flush();
            output.completeMessage();
            output.close();
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
        if(data == null){
            return null;
        }
        Object result = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            Hessian2Input input = new Hessian2Input(is);
            result = input.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return (T)result;
    }

}
