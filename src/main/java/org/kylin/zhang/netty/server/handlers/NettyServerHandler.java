package org.kylin.zhang.netty.server.handlers;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.kylin.zhang.beans.FileData;
import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.message.Message;
import org.kylin.zhang.message.MessageBuilder;
import org.kylin.zhang.message.MessageType;

import org.kylin.zhang.netty.client.NettyClient;
import org.kylin.zhang.netty.client.NettyClientBuilder;
import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.util.FilePacker;
import org.kylin.zhang.util.RandomInteger;

import java.util.Date;
import java.util.List;

/**
 * Created by win-7 on 2015/10/6.
 */
public class NettyServerHandler extends ChannelHandlerAdapter{

    private NettyServer_v1 nettyServerHandler ;
    private boolean shutDown = false ;

    public NettyServerHandler( NettyServer_v1 nettyServer ){
        this.nettyServerHandler = nettyServer ;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
   //     System.out.println("------------------ server: new message come ---------------------") ;
        Message recvMessage  = (Message)msg ;

        if(recvMessage.getType() == MessageType.ZK_OFFLINE){
            //  根据消息类型推断出， data = ServerInfo

            // 1. 获取 Message 中的 ServerInfo 数据段
            ServerInfo dataServerInfo = (ServerInfo)recvMessage.getObjectBody(ServerInfo.class) ;

            System.out.println(nettyServerHandler.getServerName() + " receives off line message from zookeeper") ;

            // 2. 通过 nettyServerHandler 来查找 finger-table 删除对应的元素
            nettyServerHandler.removeFromFingerTable( dataServerInfo.getServerName());

            // 3. 创建 CLOSE_CONN 的消息 , 将自己的节点信息发送给堆放

            Message message = MessageBuilder.getServerInfoDataInstance(MessageType.CLOSE_CONN, nettyServerHandler.getServerInfo()) ;

            // 3. 通过 NettyClient 向 dataServerInfo 发送 CLOSE_CONN 消息
            // 接收者是从 zk 发送过来的 message 所对应的 data 部分提取出来的信息
            NettyClient nettyClientSender = new NettyClient(message , dataServerInfo) ;

            new Thread(nettyClientSender, "netty close conn msg sender thread").start();

            System.out.println( nettyServerHandler.getServerName() +" sends close connection to " + dataServerInfo.getServerName()) ;

        }

        if( recvMessage.getType() == MessageType.CLOSE_CONN){

            // 该类型的消息的数据段存放着的是 ServerInfo: dataServerInfo

            // 1. 从 Messsage 中抽取 ServerInfo 对象
            ServerInfo dataServerInfo = (ServerInfo) recvMessage.getObjectBody(ServerInfo.class) ;

            // 2. 然后，参照本地的 finger-table 将对应的元素移除
            nettyServerHandler.removeFromFingerTable( dataServerInfo.getServerName());

            // 3. 关闭连接

            System.out.println("close connection between " + nettyServerHandler.getServerName() +" and "+ dataServerInfo.getServerName()) ;
            ctx.close() ;
            ctx.pipeline().close() ;

            // 4. 接下来，查询 finger-table 中是否还有元素，如果有，没有操作
                                                    //      如果没有元素了，将 shutDown--> true
            if(nettyServerHandler.getFingerTableLength() == 0){
                shutDown = true ; // 这个变量将会在 channelReadComplete 中控制 当前 NettyServer 服务器的运行和停止
            }
        }


        // zk 上线消息
        if(recvMessage.getType() == MessageType.ZK_ONLINE){

            // 1. 获取 ServerInfo:receiverServerInfo 对象实例
            ServerInfo receiverServerInfo  = (ServerInfo) recvMessage.getObjectBody(ServerInfo.class) ;

            System.out.println( nettyServerHandler.getServerName() +" receive from zookeeper server online message" ) ;

            // 2. 将获取的 ServerInfo 对象实例添加到自己本地的 finger-table 中
            this.nettyServerHandler.addToFingerTable( receiverServerInfo );

            // 3. 创建 发送给 receiverServerInfo 的Message 对象 : 类型
            Message message = MessageBuilder.getServerInfoDataInstance(MessageType.REQ_FILE , nettyServerHandler.getServerInfo()) ;

            // 4. 创建 NettyClient 发送消息的 Netty 客户端,并发送消息
            NettyClient ntClientSender = new NettyClient(message, receiverServerInfo) ;
              new Thread(ntClientSender , "netty server file request type message sender thread").start();

            System.out.println( nettyServerHandler.getServerName() + " send request file message to " + receiverServerInfo.getServerName()) ;


        }

        // 对等端发送请求文件消息
        if(recvMessage.getType() == MessageType.REQ_FILE){

            // 1. 从 Message 中抽取 ServerInfo ：receiverServerInfo
            ServerInfo receiverServerInfo  = (ServerInfo)recvMessage.getObjectBody(ServerInfo.class) ;

            System.out.println( nettyServerHandler.getServerName() + " receive from " + receiverServerInfo.getServerName()+ " request file message ") ;


            // 2. 将其添加到本地的 finger-table 中
            nettyServerHandler.addToFingerTable( receiverServerInfo );

            // 3. 创建 READY_SEND Message
            Message message = MessageBuilder.getInstance( null , MessageType.READY_SEND) ;

            // 4. 通过 ctx.writeAndFlush() 方法来发送 Message
            ctx.writeAndFlush(message) ;

          System.out.println( nettyServerHandler.getServerName() +" send ready to send files to you message to  " + receiverServerInfo.getServerName()) ;
        }

        if( recvMessage.getType() == MessageType.READY_SEND ){

            System.out.println( nettyServerHandler.getServerName() + " received read to send message from peer ") ;

            // 1. 从 Message 的类型得知， 数据段是 null, 收到该消息之后，立即创建 READY_RECV 消息 作为回复
            Message message = MessageBuilder.getInstance(null , MessageType.READY_RECV) ;

            ctx.writeAndFlush( message) ;

            System.out.println( nettyServerHandler.getServerName() + " send ready to receive message as response message without data ") ;
        }

        if( recvMessage.getType() == MessageType.READY_RECV){

            //好了， 这个地方的逻辑对于我来说还是挺复杂的
            // 1. 首先从本地-> 对应自己的 ServerName 的路径下面， 这里对应的是 data/'自己的服务器名称'/{获取10 个 RandomInteger.get(0 , 99).txt 文件}

            /**
             * for i = 0 -> 9 对应执行的循环是用来获取本地的 10 个文件的
             * loop begin ：||
             *
             *          1. 生成随机数 0-99 {因为 ， 生成的文件名称是 0.txt --> 99.txt 100 个随机大小(10K-10M)的文件}
             *          2. 调用 FilePacker 读取文件 --> 并将文件打包成 List<Message> messageList
             *
             *          for  Message message : messageList
             *              loop being :||
             *
             *                 通过 channel 来将消息数据发送到对等端
             *                 ctx.writeAndFlush( message ) ;
             *                 Thread.sleep(3000) ; // 每次发送一个 message 之后，休息 3 s ， 目的是让这个过程尽量的慢，来排错和分析执行步骤
             *                                      // 以及调用远程工具的时候，可以显示文件正在传输的过程
             *
             *              || loop end   ;
             *
             *
             * || loop end ;
             *
             * 在将文件全部发送出去之后，等待 5s ， 然后创建一个 Message 类型为 END_SEND
             * 即便是结束发送文件， 到那时为了体现‘网络中，通讯节点保持连接通信的蛋疼设定’ 我决定不在这里断开连接
             * 而是在发送 CONN_CLOSE 消息的时候， 通过 channelReadComplete 来断开连接， 然后关闭服务器
             * */

            // 2. 然后将文件生成 List<Message>
            List<Message> messageList ;

            for( int i = 0 ; i < 10 ; i++ ){

                String fileName = RandomInteger.getRandomInteger(0, 99)+".txt" ;
                messageList = FilePacker.getMessageListFromFile(fileName , nettyServerHandler.getServerName()) ;

                for(Message m : messageList){
                    ctx.writeAndFlush( m ) ;
                    try{
                        Thread.sleep(3000);
                    }catch(Exception e){
                       e.printStackTrace();
                        }
                }

            }

            System.out.println(nettyServerHandler.getServerName() +" finish sending file to peer") ;

            try{
                Thread.sleep(1000);
            }catch(Exception e){
                e.printStackTrace();
            }

            // 在将本地的 10 个随机文件全部发送到对等端之后，
            // 等待 1s ， 然后创建 END_SEND 消息发送给对等端

            Message endSendFileMessage = MessageBuilder.getInstance(null , MessageType.END_SEND) ;


            ctx.writeAndFlush(endSendFileMessage) ;

            System.out.println( nettyServerHandler.getServerName() +" send end file sending message as response") ;
        }

        if(recvMessage.getType() == MessageType.SENDING_FILE){

                // 抽取 Message 中的 FileData
                FileData fileData = FilePacker.getFileDataObjectFromMessage(recvMessage) ;

                System.out.println(nettyServerHandler.getServerName() +" receive sending file message from  " + fileData.getFileName()) ;

                // 将 fileData 中的时间更新为 当前，也就是接收到消息的时候的时间
                fileData.setSendTimer( new Date().getTime());

                // 将消息依次的追加早 nettyServer 的本地路径下面
                nettyServerHandler.localFileAppendWriter( fileData );

                // 然后再将消息中的 FileData 提取出来，更新 nettyServer 的 recvFileList
                nettyServerHandler.addFileDataToFileTable(fileData);

                // 并且在 recvFileList 添加元素的方法中，来实现这样的机制，每次添加一个新元素
                // 就会调用方法， 将 recvFileList 中的数据同步到 zk-server 端一次

            }


         if(recvMessage.getType() == MessageType.END_SEND){
             System.out.println(nettyServerHandler.getServerName() +" received finish file sending message ") ;

             nettyServerHandler.resizeTempFileTable();

         }
        }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if(shutDown ){
            // 调用方法停止 NettyServer 的对象实例
            this.nettyServerHandler.shutDownServer();
        }
    }
}


