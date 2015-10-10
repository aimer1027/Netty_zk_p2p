package org.kylin.zhang.netty.client.handlers;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.kylin.zhang.message.Message;

import java.util.List;

/**
 * Created by win-7 on 2015/10/6.
 */
public class NettyClientSenderHandler extends ChannelHandlerAdapter {

    private List<Message> messageList ;


    public NettyClientSenderHandler(List<Message> messageList){
        this.messageList = messageList ;
    }

    // client 是主动发送消息的一方， 所以在 channel active 的时候，首先将消息发送出去


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (Message message : messageList){
         //   System.out.println("-------------------- client sending message ---------------------") ;
            ctx.writeAndFlush( message) ;
            try{
                Thread.sleep( 3000 ); // 为了让 client cmd tool 起作用，每次发送一个消息，就会等上 3 分钟
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();

        throw new RuntimeException("error happens in NettyClientSenderHandler | exceptionCaught") ;
    }
}
