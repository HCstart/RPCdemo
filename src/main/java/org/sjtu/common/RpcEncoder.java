package org.sjtu.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author hcstart
 * @create 2022-06-15 11:04
 *
 * RPC 编码器
 *
 * 由于是继承的 MessageToByteEncoder，所以每个channel使用中涉及到私有信息的转换，不能设置成共享，即不能添加 @sharable
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ch, RpcProtocol msg, ByteBuf out) throws Exception {
        out.writeShort(msg.getMagicNumber());  //魔数
        out.writeInt(msg.getContentLength());  //字段长度
        out.writeBytes(msg.getContent());   //需要发送的数据：RpcInvocation对象序列化后的 byte数组
    }
}
