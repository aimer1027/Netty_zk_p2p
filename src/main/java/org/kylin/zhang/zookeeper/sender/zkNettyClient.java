package org.kylin.zhang.zookeeper.sender;

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

import java.net.InetSocketAddress;

/**
 * Created by win-7 on 2015/10/9.
 *
 * ������ǵ��̶߳������е��࣬
 * ������send �����Ὣ Message ���͵���Ӧ ServerInfo ��ָʾ�ķ�������
 *
 */
public class zkNettyClient implements Runnable {

    private Message message ;
    private ServerInfo receiver ;

    public zkNettyClient( Message message , ServerInfo receiver){
        this.message = message ;
        this.receiver = receiver ;
    }

    public void sendMessage( ){

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup() ;

        try{
            Bootstrap b = new Bootstrap() ;

            b.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler( new childHandlerLoader(message) ) ; // �������Ӧ���� ZkClientChildHandlersLoader

            ChannelFuture future = b.connect( new InetSocketAddress( receiver.getIp() , receiver.getPort() ) ).sync() ;

        }catch(Exception e){
            e.printStackTrace();
        } finally{
            eventLoopGroup.shutdownGracefully() ;
        }



    }

    public void run(){
        sendMessage() ;
    }

}
