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
     * ��������һ���߳����� ��Ϣ���͸� receiverInfo ָ���Ľ��ն�
     * �÷������ᱻд�뵽 run ������
     *
     * ͬʱ���ھ�������Ƚϼ򵥣�����ֱ���ڱ����н��м��ؼ���
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
                            ch.pipeline().addLast(new Encoder()) ; // ��Ϊ��һ���Է�����Ϣ�� ������ͨ����� pipeline ��������Ϣ
                                                                    // ���ԣ�������Ҫ���� Decoder �� ���� һ����Ϣ���ͳ�ȥ֮ǰ���б���� ����������
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
