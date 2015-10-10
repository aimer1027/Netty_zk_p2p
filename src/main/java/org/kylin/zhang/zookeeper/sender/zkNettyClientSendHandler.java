package org.kylin.zhang.zookeeper.sender;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.kylin.zhang.message.Message;

/**
 * Created by win-7 on 2015/10/9.
 *
 * сисз
 *
 */
public class zkNettyClientSendHandler extends ChannelHandlerAdapter {

    private Message message ;
    public zkNettyClientSendHandler( Message message ){
        this.message = message ;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(message) ;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        throw new RuntimeException("happen in zkNettyClientSendHandler") ;
    }
}
