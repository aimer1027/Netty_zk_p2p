package org.kylin.zhang.netty.client.handlers;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.kylin.zhang.beans.FileData;
import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.message.Message;
import org.kylin.zhang.message.MessageBuilder;
import org.kylin.zhang.message.MessageType;
import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.util.FilePacker;

import java.util.Date;

/**
 * Created by win-7 on 2015/10/11.
 *
 * 这个类设定了 NettyClient_2 将会执行的一系列方法
 *
 *
 * 该 handler 对应的 NettyClient_2 会在 NettyServer 接收到 zk-monitor
 * 发送过来的 ZK_ONLINE 消息对应的 处理句柄中被创建，
 *
 * 在对象被创建的一开始，便会创建发送到 zk 发送过来的对等端的
 * 消息的 REQ_FILE 的消息-----> 发送消息写入在 channelActive 这个方法中
 *
 * 对于 NettyClientHandler 划分的消息响应
 处理的消息:
 1.READY_SEND
 动作{
 创建类型为 READY_RECV 的消息，然后将通过 ctx.writeAndFlush() 方法发送出去

 }
 2.SEND_FILE
 动作{
 抽取 FileData
 通过NettyServer 代理对象， 调用文件追加方法，将文件追加到对应路径下面

 在这里因为会用到， NettyServer 的文件的追加方法，所以会使用到 NettyServer
 所以将其作为成员方法

 }
 3. END_FILE
 动作{
 关闭连接，调用 NettyServer 代理对象，resize 对应的 tempFileTable
 }
 *
 */
public class NettyClientHandler_v1 extends ChannelHandlerAdapter{
   private NettyServer_v1 nettyServerProxy ;

   private boolean closeChannel = false ;

    public NettyClientHandler_v1( NettyServer_v1 nettyServerProxy){
        this.nettyServerProxy = nettyServerProxy ;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       // 创建 REQ_FILE 消息

        Message message  = MessageBuilder.getServerInfoDataInstance(MessageType.REQ_FILE , nettyServerProxy.getServerInfo()) ;

        ctx.writeAndFlush(message) ;

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        Message  recvMessage = (Message)msg ;

        if(recvMessage.getType() == MessageType.READY_SEND){
  //          System.out.println( nettyServerProxy.getServerName() + " received READY_SEND message from peer ") ;

            // 1. 从 Message 的类型得知， 数据段是 null, 收到该消息之后，立即创建 READY_RECV 消息 作为回复
            Message message = MessageBuilder.getServerInfoDataInstance(MessageType.READY_RECV, nettyServerProxy.getServerInfo()) ;

            ctx.writeAndFlush( message) ;

   //         System.out.println( nettyServerProxy.getServerName() + " send ready to receive message as response message without data ") ;
        }

        if(recvMessage.getType() == MessageType.SENDING_FILE){

            // 抽取 Message 中的 FileData
            FileData fileData = FilePacker.getFileDataObjectFromMessage(recvMessage) ;

       //     System.out.println(nettyServerProxy.getServerName() +" receive sending file message from  " + fileData.getFileName()) ;

            // 将 fileData 中的时间更新为 当前，也就是接收到消息的时候的时间
            fileData.setSendTimer( new Date().getTime());

            // 将消息依次的追加早 nettyServer 的本地路径下面
            nettyServerProxy.localFileAppendWriter( fileData );

            // 然后再将消息中的 FileData 提取出来，更新 nettyServer 的 recvFileList
            nettyServerProxy.addFileDataToFileTable(fileData);

            // 并且在 recvFileList 添加元素的方法中，来实现这样的机制，每次添加一个新元素
            // 就会调用方法， 将 recvFileList 中的数据同步到 zk-server 端一次

        }

        if(recvMessage.getType() == MessageType.END_SEND){

          //  nettyServerProxy.resizeTempFileTable();

            closeChannel = true ;
            ctx.pipeline().close() ;
            ctx.close() ;

         //   System.out.println(nettyServerProxy.getServerName() +" receive end sending message close the channel") ;

        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if(closeChannel){
            ctx.pipeline().close() ;
            ctx.close() ;
            System.out.println("now the client of "+nettyServerProxy.getServerName()+" is close") ;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        throw new RuntimeException( cause ) ;
    }
}

































