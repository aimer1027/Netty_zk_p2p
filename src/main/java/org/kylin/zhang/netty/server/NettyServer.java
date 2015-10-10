/*
package org.kylin.zhang.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.kylin.zhang.beans.FileData;
import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.netty.server.handlerLoaders.NettyServerHandlersLoader;
import org.kylin.zhang.zookeeper.zkMonitor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

*/
/**
 * Created by win-7 on 2015/10/6.
 *//*

public class NettyServer  {
    private String serverName ;
    private ServerInfo serverInfo ;


    private tempFileTable tempFileTable ;
    private Map<String , ServerInfo> fingerTable ;// �������������ٽ��ڵ��

    // ��� zk-monitor ��Ϊ��Ա����
    private zkMonitor zookeeperMonitor  ;


    //------------ ����ķ�����װ�� �� ��ǰ�ķ����� ��Ϣ ע�ᵽ zk-server ������
    public void registerToZkServer(){
        zookeeperMonitor.registerServerToRemote(serverInfo);
    }


    //-------------------- ����ķ�����װ�� tempFileTable ��DAO ����
    public void addFileDataToFileTable ( FileData fileData ){

        this.tempFileTable.addNewFileInfo(fileData);

        //�ڼ�����Ԫ��֮�� ������ȡ tempFileTable �е����ݣ�Ȼ����Ϣͬ����Զ�� zk-server ������

        List<String> zkDataList =   tempFileTable.getLastServerClusterInfo() ;

        // �������Ϊ zk �����û��д�� ������ʱ�������ʾһ�¾Ϳ�����
        // һ��ʵ��һ�£� client �� server һ���Է��Ͷ�� �ļ��� �Լ���� client �� server ���Ͷ���ļ�

        // �ող�����һ�£� ������ ���׷�ӵ� �ļ����ȷ��滹��������
 //       System.out.println(" server " + serverInfo.getServerName() + " info ") ;

        String upLoadData = new String() ;

        for (int i = 0 ; i < zkDataList.size() ; i++ ){
           String data = zkDataList.get(i) ;
          upLoadData += data  ;

            if( i != (zkDataList.size()-1) )
                upLoadData+='&' ;
        }

        // ÿ�ζ��ǽ����е� zkDataList �����е�Ԫ��ȫ���ռ�һ�£��ڽ����ϴ��� cluster ��

        this.updateServerInfoOnCluster(upLoadData);

     //   System.out.println("upload data " + upLoadData) ;
       // System.out.println("---------------------------------------- \n\n") ;
    }

    //----------------- ���췽��
    public NettyServer(){
         tempFileTable = new tempFileTable() ;
         fingerTable = new Hashtable<String, ServerInfo>() ;
    }

    public NettyServer(  String serverName , zkMonitor zkMonitor){

        this() ;

        this.zookeeperMonitor = zkMonitor ;
    }

    // ����Ĺ��췽���������ڲ��ԣ� ��Ϊ������˳���� 1. ���� serverName 2. �� zk �ϼ���/��ȡ�����ļ� ��ʵ���� ServerInfo ����
    public NettyServer (ServerInfo serverInfo, zkMonitor zkMonitor){
        this() ;

        this.serverInfo = serverInfo ;
        this.zookeeperMonitor = zkMonitor ;
    }

    // ---------- ����ķ����ǽ���ǰ server �е�������Ϣ��ͬ���� zookeeper-server ��Ӧ·�������������Ϣ��
    public void updateServerInfoOnCluster(String dataContent ){
        zookeeperMonitor.upLoadServerInfoOnCluster(dataContent, serverInfo.getServerName());
    }

    // ------------ �����ļ�׷�ӷ��� -------------------
    public void localFileAppender( FileData fileData ){

        // ������ȡ fileData �еķ����� server-name
        String folderPathName = "data/"+fileData.getSenderName() ;
        System.out.println( fileData.getSenderName() ) ;
        File dir = new File(folderPathName) ;


        // server-name �ڱ��� data ·���������Ƿ���� �Ը� server-name Ϊ���ֵ� �ļ���
        // ����--> �޶���
        // ������ --> ����һ��ͬ�����ļ���
        if( dir.exists() && !dir.isDirectory() ) // ���ڵ��������ļ���
            dir.delete() ;
        if(!dir.exists())
            dir.mkdirs() ;


        // �Ҹ�·�������Ƿ����ͬ�����ļ�
        String filePath = folderPathName+'/'+"received_"+fileData.getFileName() ;
        File file = new File (filePath) ;
        BufferedOutputStream bos  ;

        try{

            // ���� --> ׷�ӵķ�ʽ�򿪸��ļ�
            if( file.exists())
            {
                bos = new BufferedOutputStream( new FileOutputStream( file, true)) ;

                bos.write( fileData.getDataContent());

                bos.flush();

                bos.close();
            }
            else {
                // ������ ---> ֱ��ͨ�� File.createNewFile ����һ���ļ�������Ȼ������д������
                file.createNewFile() ;

                bos = new BufferedOutputStream( new FileOutputStream( file , false )) ;

                bos.write( fileData.getDataContent());

                bos.flush();

                bos.close( ) ;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    //-------------- netty-server �������� ---------
    public void startServer( final NettyServer nettyServer  ){

        new Thread(){

            final EventLoopGroup bossGroup  = new NioEventLoopGroup();
            final EventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            ChannelFuture future ;

            public void run(){
                try{
                    b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG , 100)
                            .handler(new LoggingHandler(LogLevel.INFO))
                            .childHandler( new NettyServerHandlersLoader( nettyServer )) ;

                    future = b.bind( serverInfo.getIp() , serverInfo.getPort() ).sync() ;
                    System.out.println(" server is running listening on port " + serverInfo.getPort()) ;
                    future.channel().closeFuture().sync() ;
                }catch(Exception e){
                    e.printStackTrace();
                    throw new RuntimeException("fail to startServer in NettyServer") ;
                } finally{
                    bossGroup.shutdownGracefully() ;
                    workerGroup.shutdownGracefully() ;
                }
            }
        }.start();

    }




    //----------------- getter setter ����


    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public Map<String, ServerInfo> getFingerTable() {
        return fingerTable;
    }

    public void setFingerTable(Map<String, ServerInfo> fingerTable) {
        this.fingerTable = fingerTable;
    }
}
*/
