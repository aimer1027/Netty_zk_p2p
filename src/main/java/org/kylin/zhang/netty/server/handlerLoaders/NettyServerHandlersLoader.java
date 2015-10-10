package org.kylin.zhang.netty.server.handlerLoaders;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.kylin.zhang.netty.codec.Decoder;
import org.kylin.zhang.netty.codec.Encoder;
import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.netty.server.handlers.NettyServerHandler;

/**
 * Created by win-7 on 2015/10/6.
 */
public class NettyServerHandlersLoader  extends ChannelInitializer<SocketChannel> {

    private NettyServer_v1 nettyServerHandler = null ;

    public NettyServerHandlersLoader( NettyServer_v1 nettyServerHandler){
        this.nettyServerHandler = nettyServerHandler ;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ch.pipeline().addLast("frame decoder" , new LengthFieldBasedFrameDecoder(65535, 0 , 2 , 0 , 2)) ;
        ch.pipeline().addLast("decoder" , new Decoder()) ;
        ch.pipeline().addLast("encoder" , new Encoder()) ;
        ch.pipeline().addLast("server message handler" , new NettyServerHandler( nettyServerHandler )) ;


    }
}
