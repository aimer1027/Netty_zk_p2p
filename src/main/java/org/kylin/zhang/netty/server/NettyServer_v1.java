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

    /**
     * ��ʼ�� netty-server ����
     * ��Ҫ���ܣ� ���ݹ��췽���и����� serverName ����<Զ�� zk-server �ڵ�����|���������ļ�>��ȡ�÷���������
     * ��Ӧ�ķ�����������������Ϣ
     * */
    public void initNettyServer(){
        /**
         * 1. ����������Ϣ������� PropsConf: ���Ĺ�������
         *                              a. �޸������ļ��������޸ĵ�ѡ��ͬ�����ļ���
         *                              b. �������ļ��Ӷ�Ӧ��·�������ȡ���������������ļ���
         *                              c. ���������ļ�
         *                              d. ɾ�������ļ�
         *                              e. �������ļ�������µ� key-value ��
         *
         * 2. ��� zk-handler �Ƿ���Զ�̵� zk-server ���ӳɹ�(���Ӱ���������ķ�֧)
         * */

        // 1.
        this.propsConf = new PropsConf(serverName) ;

        // 2.
        if( zkMonitorHandler.isConnect()){

            // ��Զ�̻�ȡ�Լ��������ϵ�������Ϣ
            serverInfo = zkMonitorHandler.getServerConfInfoFromZkServer(serverName) ;

            //  ��ȡ���µ�������Ϣ֮���ȱ����������������ȸ��±��ص������ļ�
            propsConf.updateByServerInfo( serverInfo  );


        } else{


            // �޷����ӵ�Զ�̻�ȡ������Ϣ
            // ͨ�� PropsConf �����ȡ���ص������ļ�����ʼ�� ServerInfo ����

            // PropsConf �ڹ��췽�����Ѿ������� ��Ӧ 'this.serverName' �������ļ���Ϣ
            // ���ص��´����� ����ʵ����

            serverInfo = propsConf.getServerInfo() ;
        }

    }



    /**
     * ����ķ����������� Netty-server ��server �����̵߳�
     *
     * ������������е�λ���̣߳�
     * */


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

    //------------------- ����ĵ��̵߳������� netty-server-listener �Ѿ�ͨ�������� --------------------
    // ---- �����д�������ڽ��Լ��Ľڵ���Ϣע�ᵽ Զ�� zk-server ���� -------------------
    public void registerToZkServer(){
        zkMonitorHandler.registerServerToRemote( serverInfo );
    }


    // ����ķ����ǲ������õķ���������֮�󽫻ᴴ��һ���µ��߳�
    // ר��Ϊ ������ ���ɱ��� ���ļ� , ��������� ���췽���У� ��������һ���յ�
    // ServerName �ĸ�ֵ֮�� �������ô˷������ڱ��ض�Ӧ��·������ ���� 100 ������λ�� 10K-10M ��С�ļ�
    private void createLocalFile(){
        FileUtil fileUtil =  new FileUtil(serverName)  ;
        fileUtil.run();
    }



//------ end --------- ��ʼ������ ------------------------------------------------


 // ================================ �� netty-server-handler ���õķ��� ================================================

// --- �� netty-server-handler ����  tempFileTable �ķ��� ----------------
    public void addFileDataToFileTable ( FileData fileData ){

        // 1. �� FileData �����뵽 tempFileTable ��
        this.tempFileTable.addNewFileInfo( fileData );


        // 2. ͨ�� tempFileTable �еķ�������ȡ��ǰ���յ����ļ���״̬
            // ��Ӧ�ڲ�ִ�еķ�������---> FileData ---> ��ת���� FileInfo ��ŵ� tempFileTable ��
            // ---> ���� tempFileTable �е�ÿ��Ԫ�� ������� ServerInfoOnCluster ���� ----> ����JsonPacker
            // ----> �� ServerInfoOnCluster �������ݸ�ʽ����  String
        List<String> serverOnClusterInfoData = tempFileTable.getLastServerClusterInfo() ;

        String upLoadStringData = new String() ;

        for ( int i = 0 ; i < serverOnClusterInfoData.size() ; i++ ){
            // ���ַ���ƴ�ӳ� String
            String data = serverOnClusterInfoData.get(i) ;

            upLoadStringData += data ;

            // Ϊ�˽����ķ��㣬 ��ÿ�� info-node ֮��ʹ�� '^' ��Ϊ�ָ�����
            if( i != (serverOnClusterInfoData.size() -1) ){
                upLoadStringData += '&' ;
            }
        }

        // 3. ��״̬����ͨ�� zkMonitorHandler ͬ���� zk-server ������
        this.zkMonitorHandler.upLoadServerInfoOnCluster(upLoadStringData , serverInfo.getServerName());

    }


// -- �� netty-server-handler ���õķ����� �����Բ�ͬ �������� �ļ�Ƭ�Σ�׷�ӵ����� data/��ͬ�ķ���������/ ��Ӧ��·������
// ���1 �� ������ļ��ڱ���û�б�������˵������ļ������ļ��Ŀ�ʼ���֣�֮ǰû�н��յ���; �������ֻ��Ҫ�ڶ�Ӧ��·�����洴����Ӧ���ļ��к��ļ�����
// ���2 �� ������ļ��ڱ����Ѿ��������ˣ� ˵������ļ�������Ҫ��׷���ڶ�Ӧ��·��������ģ����ļ���д���������ķ�ʽ������Ҫ�� ׷�ӵķ�ʽ��ʵ��
     public void localFileAppendWriter( FileData fileData  ){
         // ����������ȡ fileData �ķ����ߵ�����
         String filePrefixPath = "data/"+serverInfo.getServerName()+"_receivesFrom_"+fileData.getSenderName() ;
         File fileDir  = new File( filePrefixPath ) ;

         // ͨ������ļ����Ƿ���ڣ������жϳ���ǰ�����ļ��ķ������Ƿ��������չ����Ը÷����߷��͵��ļ�
         // ��������ڶ�Ӧ�ļ���---> �жϳ���û�н��չ��� ����: �������ļ���
         // ������ļ��д��� ------> �жϳ�: ���չ���     ����: �޶���
         if( !fileDir.exists()  ) // �����ڸ��ļ��У� �����ļ���
         {
             fileDir.mkdirs() ;
         }

         // �ڶ�Ӧ��·�������� �Ƿ����ͬ�����ļ�
         String filePathName = filePrefixPath+'/'+fileData.getFileName() ;

         File file = new File( filePathName ) ;
         BufferedOutputStream bos ;

         try{

             // ����ҵ�ͬ���ļ��� ˵������ǰ��������ֹһ�ν��յ�ͬ���ļ�Ƭ�� �� ���� : ׷�ӵķ�ʽд���ļ�
             if(file.exists()){

                 bos = new BufferedOutputStream( new FileOutputStream(file , true )) ;

                 bos.write( fileData.getDataContent() );

                 bos.flush() ;

                 bos.close () ;
             }
             else {

                 // �Ҳ������״ν����ļ�Ƭ�Σ���Ƭ�����ļ���ͷ ; ����: �ڱ��ش����ļ������� FileData �е�����д�뵽�ļ���
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
    // ����ķ������������� �����е� tempFileTable ��size ��
    // �������������Ѿ���ɴ�����ļ���

    public void resizeTempFileTable(){
   ///     this.tempFileTable.resizeTempFileList();
    }

// ����ķ���������ȡ��ǰ finger-table �е�Ԫ�ظ�����
    public int getFingerTableLength (){
        return this.fingerTable.size() ;
    }

    //����ķ����� finger_table ����ӡ�ɾ��Ԫ�ض�Ӧ�ķ�������Ϊ�п����漰�� ���߳�ͬʱ����
    // ����������ʹ�� synchronize �����η���

    synchronized  public void addToFingerTable( ServerInfo addServerInfo ){

        if( this.fingerTable == null ){
            fingerTable = new Hashtable<String, ServerInfo>() ;
        }

        // �����ﲻ�����ͬ�� key ��Ԫ�أ����Ǵ���ͬ�� key ����ʾ������Ϣ
        if(fingerTable.containsKey(addServerInfo.getServerName()) ){
            System.out.println("element with key " + addServerInfo.getServerName() +" already exists ! error") ;
            return ;
        }

        fingerTable.put(addServerInfo.getServerName(), addServerInfo) ;

    }

    synchronized  public void removeFromFingerTable (String deleteServerInfoName ){
        // �������һ�£� fingerTable �Ƿ�Ϊ�ջ�����û��Ԫ��
        if(fingerTable == null || fingerTable.size() == 0){
            System.out.println("no elements in finger-table , failed to delete element " + deleteServerInfoName) ;
        }

        // �������һ�£��Ƿ���ڶ�Ӧ��Ԫ��
        if( !fingerTable.containsKey( deleteServerInfoName)){
            System.out.println("can not find element with name " + deleteServerInfoName +" in finger table , error") ;
            return ;
        }

        // ����ų���������� ɾ����ӦԪ��
        fingerTable.remove(deleteServerInfoName) ;

    }

 // ����ķ�������ֹͣ�������Ľ����Լ��ͷ���صĿռ�
    public void shutDownServer(){
        // 1. ����ֹͣ listener �߳�
        bossGroup.shutdownGracefully() ;
        workerGroup.shutdownGracefully() ;

        // 2. �����˳�����
        System.exit(0);
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

     Thread.sleep(1000);

     registerToZkServer();

    }

}

