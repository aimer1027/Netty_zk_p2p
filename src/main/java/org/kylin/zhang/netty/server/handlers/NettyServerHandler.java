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
            //  ������Ϣ�����ƶϳ��� data = ServerInfo

            // 1. ��ȡ Message �е� ServerInfo ���ݶ�
            ServerInfo dataServerInfo = (ServerInfo)recvMessage.getObjectBody(ServerInfo.class) ;

            System.out.println(nettyServerHandler.getServerName() + " receives off line message from zookeeper") ;

            // 2. ͨ�� nettyServerHandler ������ finger-table ɾ����Ӧ��Ԫ��
            nettyServerHandler.removeFromFingerTable( dataServerInfo.getServerName());

            // 3. ���� CLOSE_CONN ����Ϣ , ���Լ��Ľڵ���Ϣ���͸��ѷ�

            Message message = MessageBuilder.getServerInfoDataInstance(MessageType.CLOSE_CONN, nettyServerHandler.getServerInfo()) ;

            // 3. ͨ�� NettyClient �� dataServerInfo ���� CLOSE_CONN ��Ϣ
            // �������Ǵ� zk ���͹����� message ����Ӧ�� data ������ȡ��������Ϣ
            NettyClient nettyClientSender = new NettyClient(message , dataServerInfo) ;

            new Thread(nettyClientSender, "netty close conn msg sender thread").start();

            System.out.println( nettyServerHandler.getServerName() +" sends close connection to " + dataServerInfo.getServerName()) ;

        }

        if( recvMessage.getType() == MessageType.CLOSE_CONN){

            // �����͵���Ϣ�����ݶδ���ŵ��� ServerInfo: dataServerInfo

            // 1. �� Messsage �г�ȡ ServerInfo ����
            ServerInfo dataServerInfo = (ServerInfo) recvMessage.getObjectBody(ServerInfo.class) ;

            // 2. Ȼ�󣬲��ձ��ص� finger-table ����Ӧ��Ԫ���Ƴ�
            nettyServerHandler.removeFromFingerTable( dataServerInfo.getServerName());

            // 3. �ر�����

            System.out.println("close connection between " + nettyServerHandler.getServerName() +" and "+ dataServerInfo.getServerName()) ;
            ctx.close() ;
            ctx.pipeline().close() ;

            // 4. ����������ѯ finger-table ���Ƿ���Ԫ�أ�����У�û�в���
                                                    //      ���û��Ԫ���ˣ��� shutDown--> true
            if(nettyServerHandler.getFingerTableLength() == 0){
                shutDown = true ; // ������������� channelReadComplete �п��� ��ǰ NettyServer �����������к�ֹͣ
            }
        }


        // zk ������Ϣ
        if(recvMessage.getType() == MessageType.ZK_ONLINE){

            // 1. ��ȡ ServerInfo:receiverServerInfo ����ʵ��
            ServerInfo receiverServerInfo  = (ServerInfo) recvMessage.getObjectBody(ServerInfo.class) ;

            System.out.println( nettyServerHandler.getServerName() +" receive from zookeeper server online message" ) ;

            // 2. ����ȡ�� ServerInfo ����ʵ����ӵ��Լ����ص� finger-table ��
            this.nettyServerHandler.addToFingerTable( receiverServerInfo );

            // 3. ���� ���͸� receiverServerInfo ��Message ���� : ����
            Message message = MessageBuilder.getServerInfoDataInstance(MessageType.REQ_FILE , nettyServerHandler.getServerInfo()) ;

            // 4. ���� NettyClient ������Ϣ�� Netty �ͻ���,��������Ϣ
            NettyClient ntClientSender = new NettyClient(message, receiverServerInfo) ;
              new Thread(ntClientSender , "netty server file request type message sender thread").start();

            System.out.println( nettyServerHandler.getServerName() + " send request file message to " + receiverServerInfo.getServerName()) ;


        }

        // �Եȶ˷��������ļ���Ϣ
        if(recvMessage.getType() == MessageType.REQ_FILE){

            // 1. �� Message �г�ȡ ServerInfo ��receiverServerInfo
            ServerInfo receiverServerInfo  = (ServerInfo)recvMessage.getObjectBody(ServerInfo.class) ;

            System.out.println( nettyServerHandler.getServerName() + " receive from " + receiverServerInfo.getServerName()+ " request file message ") ;


            // 2. ������ӵ����ص� finger-table ��
            nettyServerHandler.addToFingerTable( receiverServerInfo );

            // 3. ���� READY_SEND Message
            Message message = MessageBuilder.getInstance( null , MessageType.READY_SEND) ;

            // 4. ͨ�� ctx.writeAndFlush() ���������� Message
            ctx.writeAndFlush(message) ;

          System.out.println( nettyServerHandler.getServerName() +" send ready to send files to you message to  " + receiverServerInfo.getServerName()) ;
        }

        if( recvMessage.getType() == MessageType.READY_SEND ){

            System.out.println( nettyServerHandler.getServerName() + " received read to send message from peer ") ;

            // 1. �� Message �����͵�֪�� ���ݶ��� null, �յ�����Ϣ֮���������� READY_RECV ��Ϣ ��Ϊ�ظ�
            Message message = MessageBuilder.getInstance(null , MessageType.READY_RECV) ;

            ctx.writeAndFlush( message) ;

            System.out.println( nettyServerHandler.getServerName() + " send ready to receive message as response message without data ") ;
        }

        if( recvMessage.getType() == MessageType.READY_RECV){

            //���ˣ� ����ط����߼���������˵����ͦ���ӵ�
            // 1. ���ȴӱ���-> ��Ӧ�Լ��� ServerName ��·�����棬 �����Ӧ���� data/'�Լ��ķ���������'/{��ȡ10 �� RandomInteger.get(0 , 99).txt �ļ�}

            /**
             * for i = 0 -> 9 ��Ӧִ�е�ѭ����������ȡ���ص� 10 ���ļ���
             * loop begin ��||
             *
             *          1. ��������� 0-99 {��Ϊ �� ���ɵ��ļ������� 0.txt --> 99.txt 100 �������С(10K-10M)���ļ�}
             *          2. ���� FilePacker ��ȡ�ļ� --> �����ļ������ List<Message> messageList
             *
             *          for  Message message : messageList
             *              loop being :||
             *
             *                 ͨ�� channel ������Ϣ���ݷ��͵��Եȶ�
             *                 ctx.writeAndFlush( message ) ;
             *                 Thread.sleep(3000) ; // ÿ�η���һ�� message ֮����Ϣ 3 s �� Ŀ������������̾������������Ŵ�ͷ���ִ�в���
             *                                      // �Լ�����Զ�̹��ߵ�ʱ�򣬿�����ʾ�ļ����ڴ���Ĺ���
             *
             *              || loop end   ;
             *
             *
             * || loop end ;
             *
             * �ڽ��ļ�ȫ�����ͳ�ȥ֮�󣬵ȴ� 5s �� Ȼ�󴴽�һ�� Message ����Ϊ END_SEND
             * �����ǽ��������ļ��� ����ʱΪ�����֡������У�ͨѶ�ڵ㱣������ͨ�ŵĵ����趨�� �Ҿ�����������Ͽ�����
             * �����ڷ��� CONN_CLOSE ��Ϣ��ʱ�� ͨ�� channelReadComplete ���Ͽ����ӣ� Ȼ��رշ�����
             * */

            // 2. Ȼ���ļ����� List<Message>
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

            // �ڽ����ص� 10 ������ļ�ȫ�����͵��Եȶ�֮��
            // �ȴ� 1s �� Ȼ�󴴽� END_SEND ��Ϣ���͸��Եȶ�

            Message endSendFileMessage = MessageBuilder.getInstance(null , MessageType.END_SEND) ;


            ctx.writeAndFlush(endSendFileMessage) ;

            System.out.println( nettyServerHandler.getServerName() +" send end file sending message as response") ;
        }

        if(recvMessage.getType() == MessageType.SENDING_FILE){

                // ��ȡ Message �е� FileData
                FileData fileData = FilePacker.getFileDataObjectFromMessage(recvMessage) ;

                System.out.println(nettyServerHandler.getServerName() +" receive sending file message from  " + fileData.getFileName()) ;

                // �� fileData �е�ʱ�����Ϊ ��ǰ��Ҳ���ǽ��յ���Ϣ��ʱ���ʱ��
                fileData.setSendTimer( new Date().getTime());

                // ����Ϣ���ε�׷���� nettyServer �ı���·������
                nettyServerHandler.localFileAppendWriter( fileData );

                // Ȼ���ٽ���Ϣ�е� FileData ��ȡ���������� nettyServer �� recvFileList
                nettyServerHandler.addFileDataToFileTable(fileData);

                // ������ recvFileList ���Ԫ�صķ����У���ʵ�������Ļ��ƣ�ÿ�����һ����Ԫ��
                // �ͻ���÷����� �� recvFileList �е�����ͬ���� zk-server ��һ��

            }


         if(recvMessage.getType() == MessageType.END_SEND){
             System.out.println(nettyServerHandler.getServerName() +" received finish file sending message ") ;

             nettyServerHandler.resizeTempFileTable();

         }
        }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if(shutDown ){
            // ���÷���ֹͣ NettyServer �Ķ���ʵ��
            this.nettyServerHandler.shutDownServer();
        }
    }
}


