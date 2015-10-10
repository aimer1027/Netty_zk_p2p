package org.kylin.zhang.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.message.Message;
import org.kylin.zhang.netty.client.handlers.NettyClientSenderHandler;
import org.kylin.zhang.netty.codec.Encoder;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by win-7 on 2015/10/6.
 */
public class NettyClient implements Runnable {

    private final List<Message> messageList ;

    private   ServerInfo receiverInfo ;

    private    EventLoopGroup eventLoopGroup ;

    private    Bootstrap b ;

    public NettyClient(Message message , ServerInfo recvInfo ){

        this.messageList = new ArrayList<Message>() ;
        this.messageList.add( message ) ;
        this.receiverInfo = recvInfo ;
    }

    public NettyClient ( List<Message> messageList , ServerInfo receiverInfo){

        this.receiverInfo = receiverInfo ;
        this.messageList = messageList ;
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
                            ch.pipeline().addLast(new Encoder()) ; // 因为是一次性发送消息， 并不会通过这个 pipeline 来接受消息
                                                                    // 所以，并不需要安置 Decoder ， 仅仅 一个消息发送出去之前进行编码的 编码器即可
                            ch.pipeline().addLast( new NettyClientSenderHandler( messageList )) ;
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
