package org.kylin.zhang.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.message.Message;
import org.kylin.zhang.netty.client.handlers.NettyClientHandler_v1;
import org.kylin.zhang.netty.codec.Decoder;
import org.kylin.zhang.netty.codec.Encoder;
import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.netty.server.handlers.NettyServerHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by win-7 on 2015/10/11.
 */
public class NettyClientSender  implements  Runnable {

    private NettyServer_v1 nettyServerProxy ;

    private ServerInfo receiverInfo ;

    private EventLoopGroup eventLoopGroup ;

    private Bootstrap b ;


    public NettyClientSender (NettyServer_v1 server , ServerInfo recvServerInfo){

        this.nettyServerProxy = server ;
        this.receiverInfo = recvServerInfo ;

    }

    /**
     * 单独创建一个线程来将 消息发送给 receiverInfo 指定的接收端
     * 该方法将会被写入到 run 方法中
     *
     * 同时由于句柄方法比较简单，所以直接在本类中进行加载即可
     * */

    public void send (){

        eventLoopGroup = new NioEventLoopGroup() ;
        try{
            b = new Bootstrap() ;

            b.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler( new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("frame decoder" , new LengthFieldBasedFrameDecoder(65535, 0 , 2 , 0 , 2)) ;
                            ch.pipeline().addLast("decoder" , new Decoder()) ;
                            ch.pipeline().addLast("encoder" , new Encoder()) ;
                            ch.pipeline().addLast( new NettyClientHandler_v1( nettyServerProxy )) ;
                        }
                    } ) ;

            ChannelFuture future = b.connect( new InetSocketAddress(receiverInfo.getIp() , receiverInfo.getPort()) ).sync() ;

            future.channel().closeFuture().sync() ;

        }catch (Exception e){
            e.printStackTrace();
        } finally{
            eventLoopGroup.shutdownGracefully() ;
        }

    }


    public void run() {
        send() ;
    }

}
