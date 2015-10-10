package org.kylin.zhang.zookeeper.sender;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.kylin.zhang.message.Message;
import org.kylin.zhang.netty.codec.Encoder;

/**
 * Created by win-7 on 2015/10/9.
 *
 * 此类用于为 zk-client-message-sender 加载一系列编码器
 * 和相关的 channel
 *
 */
public class childHandlerLoader  extends ChannelInitializer<SocketChannel>{

    private Message message ;

    public childHandlerLoader(Message message){
        this.message = message ;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("frameDecoder " , new LengthFieldBasedFrameDecoder(65535 , 0 , 2 , 0 , 2 )) ;
        ch.pipeline().addLast("encoder used before sending message", new Encoder()) ;
        ch.pipeline().addLast("zookeeper client sender's message handler" , new zkNettyClientSendHandler( message )) ;

    }
}
