package org.kylin.zhang.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.kylin.zhang.message.Message;

import java.util.List;

/**
 * Created by win-7 on 2015/9/21.
 */
public class Decoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

//        System.out.println("readable length " + in.readableBytes()) ;

        byte [] bytes = new byte[in.readableBytes()] ;

        in.readBytes(bytes) ;

        Message msg = Message.getDeSerialized(bytes) ;


        out.add(msg) ;
    }
}
