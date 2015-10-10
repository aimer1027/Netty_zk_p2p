package org.kylin.zhang.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.kylin.zhang.message.Message;


/**
 * Created by win-7 on 2015/9/21.
 */
public class Encoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {

        byte bytes [] = Message.getSerialized(msg) ;

        out.writeShort(bytes.length) ;
        out.writeBytes(bytes) ;
    }
}
