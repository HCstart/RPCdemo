package org.sjtu.common;

import java.io.Serializable;

import static org.sjtu.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * @author hcstart
 * @create 2022-06-15 10:55
 *
 * 协议规则：
 * 魔数：2个字节
 * 字段长度：4个字节
 * 实际数据
 *
 * RpcProtocol作为数据传输的格式
 *
 */
public class RpcProtocol implements Serializable {

    private static final long serialVersionUID = 5359096060555795690L;  //实现序列化接口必须要有的参数

    private short magicNumber = MAGIC_NUMBER;  //魔数

    private int contentLength;  //字段长度

    //这个字段其实是 RpcInvocation类的字节数组，在RpcInvocation中包含了更多的调用信息
    private byte[] content;   //实际数据

    public RpcProtocol(byte[] data) {   //此构造器文中没有提供，不知是否正确
        content = data;
        contentLength = data.length;
    }

    //相应的 get/set方法
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public short getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(short magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
