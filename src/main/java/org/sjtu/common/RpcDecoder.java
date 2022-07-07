package org.sjtu.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static org.sjtu.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * @author hcstart
 * @create 2022-06-15 13:44
 *
 * RPC解码器
 *
 * 解决粘包半包问题？
 *
 */
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * 协议开头部分的标准长度
     */
    public final int BASE_LENGTH = 2 + 4;   // 1个short，1个int

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        //收到的数据包至少要包含协议头
        if(byteBuf.readableBytes() >= BASE_LENGTH){

            //防止收到一些体积过大的数据包
            if(byteBuf.readableBytes() > 4056){
                byteBuf.skipBytes(byteBuf.readableBytes());  //跳越过不需要读取的字节
            }

            int beginReader;
            while (true) {    //此处为什么要用 while(true)，好像不需要
                beginReader = byteBuf.readerIndex();   //返回读指针所在坐标
                byteBuf.markReaderIndex();         //在读指针上做标记
                //读取协议中的魔术 ，short类型，2个字节
                if(byteBuf.readShort() == MAGIC_NUMBER){
                    break;
                }
//                else {
//                    //不是魔数的开头，说明是非法数据
//                    ctx.close();
////                    throw new RuntimeException("收到非法数据");  //可以尝试抛出异常
//                    return;
//                }

                //序列化框架适配
                byteBuf.resetReaderIndex();
                byteBuf.readByte();

                if (byteBuf.readInt() < BASE_LENGTH) {
                    return;
                }

            }

            //这里对应了 RpcProtocol对象的 contentLength
            int length = byteBuf.readInt();

            if(byteBuf.readableBytes() < length){  //说明剩余的数据包不完整，需要重置一下读索引
                byteBuf.readerIndex(beginReader);
                return;
            }

            //这里就是实际的RpcProtocol对象的content
            byte[] data = new byte[length];
            byteBuf.readBytes(data);
            RpcProtocol rpcProtocol = new RpcProtocol(data);   //先将数据保存成 RpcProtocol，而不是直接传输

            out.add(rpcProtocol);

        }

    }

}
