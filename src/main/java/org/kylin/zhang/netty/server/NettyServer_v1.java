package org.kylin.zhang.netty.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.kylin.zhang.beans.FileData;
import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.netty.codec.Decoder;
import org.kylin.zhang.netty.codec.Encoder;
import org.kylin.zhang.netty.server.handlerLoaders.NettyServerHandlersLoader;
import org.kylin.zhang.netty.server.handlers.NettyServerHandler;
import org.kylin.zhang.util.FileUtil;
import org.kylin.zhang.util.PropsConf;
import org.kylin.zhang.zookeeper.zkMonitor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by win-7 on 2015/10/9.
 *
 * �����µİ汾�У� ʹ����־�ķ�ʽ���������ԣ�
 * log4j.xml �����ļ����յ��� mycat-server ��Ŀ�е� log4j.xml�����ļ�
 * �������ļ������ resource �ļ������� �� ��'��־��¼��' ����Ϊ��̬��Ա��������
 *
 */
public class NettyServer_v1  implements  Runnable {

    private String serverName ; // ���������������ʼ�����������õ�
    private zkMonitor zkMonitorHandler ; // ������֮���Խ��� handler ��Ϊ����������Server �����ģ� ����ͨ�����췽��ע���

   // private FileUtil filGenerator ;
    // �����Ա���������߳�Ϊ��λ�ģ������ڸ÷������� data/serverName  ·����������һ����Ŀ���ļ���
    // Ϊ�˾������� ���������еĳ�Ա������ ����д�ɳ�Ա�����ˣ� ר��д�����÷�������

    private PropsConf propsConf ; // �����Ա������������ȡ������Ϣ�ģ�1. �����⵽ �ɹ����ӵ� zk , ��ô�ʹ� zk �����ȡ������Ϣ �� Ȼ����±����ļ�
                                    //  ����޷��ɹ����ӵ� zk , ��ô�ʹӱ��ص�·����(·���Ѿ��趨����)���ȡ��Ӧ��������Ϣ������

    private ServerInfo serverInfo ; // 1. zk ����--> ��ȡ��������Ϣ���ݴ������������У� Ȼ��ͨ���������������Ϣͬ�������������ļ���
                                        // 2. zk ������(���Ӳ��ɹ���zk-server û��������������������ʧ��) ��ȡ���������ļ� ---> ���ص� serverInfo �У�
                                        // ���� netty-server ����������ͨ�� ��ȡ ��������� ������������ʵ��
    private Map<String, ServerInfo> fingerTable ;
                                        // �������λ��ͬһ�������У� �� server-name Ҫ�����Լ��� �ڵ����Ϣ�ģ�Ҳ����˵����� server1 �� server2 ���������ļ�����Ϣ
                                        // ��ô�� server1 �Ὣ server2 ����Ϣ��ŵ��Լ��� finger-table ��
                                        // ������Ӧ�����һ�� ���߳��� �������̻߳�����ʵ�

    // ������netty �� server ��������������� netty ���
    // Ŀǰ���һ�û�о����� server ��������Ҫ�� �߳��л��ǽ��������У� �����ǰ�ߣ���ô���̵߳��õķ����ж���Щ�������г�ʼ������
    // ����Ǻ��ߣ� ��ô���ڹ��췽���н��г�ʼ������


    private tempFileTable tempFileTable ; // �����Ա������������Ž��յ��ļ�Ƭ����Ϣ�ģ�Ϊ zk-cmd-tool ���ṩ���ʵķ���
                                            // ����ԭ����һ�� hash-table ��װ���̻߳�����ʵķ�����
                                            // ÿ�� netty-server-listener �Ķ�Ӧ handler ����һ�� ���յ��� Message (FILE_SENDING ����)
                                            // ��ʱ�򣬶�����г�ȡ�� FileData ���󣬽���ת���� FileInfo ���󻺳��� tempFielTable ��
                                            // �ñ���ǣ�浽�ķ�����: 1. addFileDataToFileTable ( FileData fileData ) ��������Ϣ�� server-handler ����
                                            // ��ȡ������ FileData ���뵽 NettyServer ��


/**
 * ��Щ��Ա������ԭ������������Ϊ��Ա�����ģ�����Ϊ���� NettyServer ����ͨ����Ա����
 * ������ NettyServer �ļ����߳��� �����˿ڵ� �ر� ���Ű���Щ������ȡ�� ��Ա������
 * Ŀ����Ϊ�˳�ȫ closeNettyServerListener() ;
 * */
   private  EventLoopGroup bossGroup;
   private EventLoopGroup workerGroup;
   private  ServerBootstrap b ;

    // ��������� ���� 2 �� �� �����д���netty-server �� ���Ǽ����Ĳ������� �߳������ܵ� <����Ӧ�ı����ŵ��߳�����Ҳ���Ե�>

    //------- begin ------ ��ʼ������ --------------------
    // ���췽���� ���� 1 �� �����������ƣ� ���� 2 �� ����ĳ�ʼ���õ� zkMonitor
    public NettyServer_v1(String serverName , zkMonitor zkMonitorHandler){
        this.serverName = serverName ;

        createLocalFile() ;

        this.zkMonitorHandler = zkMonitorHandler ;


        this.fingerTable = new Hashtable<String, ServerInfo>() ;

        bossGroup = new NioEventLoopGroup() ;
        workerGroup = new NioEventLoopGroup() ;
        b = new ServerBootstrap() ;

        this.tempFileTable = new tempFileTable() ;

    }


    public void initNettyServer(){

        // 1.
        this.propsConf = new PropsConf(serverName) ;

        // 2.
        if( zkMonitorHandler!=null){


            serverInfo = zkMonitorHandler.getServerConfInfoFromZkServer(serverName) ;


            propsConf.updateByServerInfo( serverInfo  );


        } else{


            serverInfo = propsConf.getServerInfo() ;
        }

    }

   public void runNettyListenServer( ){

       ChannelFuture future ;

       try{
           b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                   .option(ChannelOption.SO_BACKLOG , 100)
                   .handler(new LoggingHandler(LogLevel.INFO))
                   .childHandler( new NettyServerHandlersLoader( this )) ;

           future = b.bind( serverInfo.getIp() , serverInfo.getPort() ).sync() ;
           System.out.println(" server is running listening on port " + serverInfo.getPort()) ;
           future.channel().closeFuture().sync() ;

       }catch(Exception e){
           e.printStackTrace();
           System.out.println("failed to start netty server listener ") ;
       } finally{
            bossGroup.shutdownGracefully() ;
           workerGroup.shutdownGracefully() ;
       }

    }

    public void run() {
        runNettyListenServer();
    }


    public void registerToZkServer(){
        zkMonitorHandler.registerServerToRemote( serverInfo );
    }



    private void createLocalFile(){
        FileUtil fileUtil =  new FileUtil(serverName)  ;
        fileUtil.run();
    }


    public void addFileDataToFileTable ( FileData fileData ){

        // 1.
        this.tempFileTable.addNewFileInfo( fileData );


        // 2.
        List<String> serverOnClusterInfoData = tempFileTable.getLastServerClusterInfo() ;

        String upLoadStringData = new String() ;

        for ( int i = 0 ; i < serverOnClusterInfoData.size() ; i++ ){

            String data = serverOnClusterInfoData.get(i) ;

            upLoadStringData += data ;

            if( i != (serverOnClusterInfoData.size() -1) ){
                upLoadStringData += '&' ;
            }
        }

        // 3.
        this.zkMonitorHandler.upLoadServerInfoOnCluster(upLoadStringData , serverInfo.getServerName());

    }



     public void localFileAppendWriter( FileData fileData  ){

         String filePrefixPath = "data/"+serverInfo.getServerName()+"_receivesFrom_"+fileData.getSenderName() ;
         File fileDir  = new File( filePrefixPath ) ;

         if( !fileDir.exists()  )
         {
             fileDir.mkdirs() ;
         }


         String filePathName = filePrefixPath+'/'+fileData.getFileName() ;

         File file = new File( filePathName ) ;
         BufferedOutputStream bos ;

         try{

             if(file.exists()){

                 bos = new BufferedOutputStream( new FileOutputStream(file , true )) ;

                 bos.write( fileData.getDataContent() );

                 bos.flush() ;

                 bos.close () ;
             }
             else {

                file.createNewFile() ;

                 bos = new BufferedOutputStream( new FileOutputStream( file , false )) ;

                 bos.write(fileData.getDataContent());

                 bos.flush();

                 bos.close();

             }

         } catch(Exception e){
             e.printStackTrace();
         }

     }
//=======================================================================================================================


    public void resizeTempFileTable(){
   ///     this.tempFileTable.resizeTempFileList();
    }


    public int getFingerTableLength (){
        return this.fingerTable.size() ;
    }

    synchronized  public void addToFingerTable( ServerInfo addServerInfo ){

        if( this.fingerTable == null ){
            fingerTable = new Hashtable<String, ServerInfo>() ;
        }

        if(fingerTable.containsKey(addServerInfo.getServerName()) ){
            System.out.println("element with key " + addServerInfo.getServerName() +" already exists ! error") ;
            return ;
        }

        fingerTable.put(addServerInfo.getServerName(), addServerInfo) ;

    }

    synchronized  public void removeFromFingerTable (String deleteServerInfoName ){

        if(fingerTable == null || fingerTable.size() == 0){
            System.out.println("no elements in finger-table , failed to delete element " + deleteServerInfoName) ;
            return;
        }

        if( !fingerTable.containsKey( deleteServerInfoName)){
            System.out.println("can not find element with name " + deleteServerInfoName +" in finger table , error") ;
            return ;
        }

        fingerTable.remove(deleteServerInfoName) ;

    }


    public void shutDownServer(){
        // 1. ����ֹͣ listener �߳�
        bossGroup.shutdownGracefully() ;
        workerGroup.shutdownGracefully() ;

        // 2.
       return ;
    }


    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public String getServerName() {
        return serverName;
    }


    // this is the final starter , create an instance
    // and then call it , it will get every thing done
    public void runNettyServer() throws Exception {
     this.initNettyServer();

     new Thread( this, serverName+"thread" ).start(); ;

    // here justify whether the zkMonitorHandler is null , if null it means zk-server can not access ,
        // so no register
    if( zkMonitorHandler !=null )
     registerToZkServer();

    }

    public void shutDownZkMonitor(){
        this.zkMonitorHandler.shutDown();
    }
}

