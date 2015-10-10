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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
 * 在最新的版本中， 使用日志的方式来帮助调试，
 * log4j.xml 配置文件参照的是 mycat-server 项目中的 log4j.xml配置文件
 * 该配置文件存放在 resource 文件夹下面 ； 而'日志记录者' 则作为静态成员变量存在
 *
 */
public class NettyServer_v1  implements  Runnable {

    // logger
    static Logger logger = Logger.getLogger( NettyServer_v1.class.getName() ) ;


    private String serverName ; // 这个名字是用来初始化服务器所用的
    private zkMonitor zkMonitorHandler ; // 在这里之所以叫它 handler 因为它并不是由Server 创建的， 而是通过构造方法注入的

   // private FileUtil filGenerator ;
    // 这个成员变量是以线程为单位的，用来在该服务器的 data/serverName  路径下面生成一定数目的文件的
    // 为了尽量减少 服务器类中的成员方法， 无需写成成员变量了， 专门写个调用方法即可

    private PropsConf propsConf ; // 这个成员变量是用来读取配置信息的，1. 如果检测到 成功连接到 zk , 那么就从 zk 上面获取配置信息 ， 然后更新本地文件
                                    //  如果无法成功连接到 zk , 那么就从本地的路径上(路径已经设定好了)面获取对应的配置信息并加载

    private ServerInfo serverInfo ; // 1. zk 在线--> 读取的配置信息数据存放在这个变量中， 然后通过这个变量来将信息同步到本地配置文件中
                                        // 2. zk 不在线(连接不成功，zk-server 没有启动，反正就是连接失败) 读取本地配置文件 ---> 加载到 serverInfo 中，
                                        // 最终 netty-server 的启动都是通过 读取 这个变量中 的配置数据来实现
    private Map<String, ServerInfo> fingerTable ;
                                        // 用来存放位于同一个网络中， 且 server-name 要大于自己的 节点的信息的，也就是说，如果 server1 向 server2 发送请求文件的消息
                                        // 那么， server1 会将 server2 的信息存放到自己的 finger-table 中
                                        // 这个今后应该添加一个 多线程锁 用来多线程互斥访问的

    // 下面是netty 的 server 部分运行所必须的 netty 组件
    // 目前，我还没有决定好 server 的运行需要在 线程中还是进程中运行， 如果是前者，那么在线程调用的方法中对这些变量进行初始化即可
    // 如果是后者， 那么，在构造方法中进行初始化即可


    private tempFileTable tempFileTable ; // 这个成员方法是用来存放接收到文件片段信息的，为 zk-cmd-tool 来提供访问的方法
                                            // 它的原型是一个 hash-table 封装了线程互斥访问的方法，
                                            // 每当 netty-server-listener 的对应 handler 传来一个 接收到的 Message (FILE_SENDING 类型)
                                            // 的时候，都会从中抽取出 FileData 对象，将其转换成 FileInfo 对象缓冲在 tempFielTable 中
                                            // 该变量牵涉到的方法有: 1. addFileDataToFileTable ( FileData fileData ) 供接收消息的 server-handler 来将
                                            // 提取出来的 FileData 传入到 NettyServer 中


/**
 * 这些成员变量，原本并不打算作为成员变量的，但是为了让 NettyServer 可以通过成员方法
 * 来控制 NettyServer 的监听线程中 监听端口的 关闭 ，才把这些变量提取到 成员变量的
 * 目的是为了成全 closeNettyServerListener() ;
 * */
   private  EventLoopGroup bossGroup;
   private EventLoopGroup workerGroup;
   private  ServerBootstrap b ;

    // 在这里采用 假设 2 ， 在 进程中创建netty-server ， 但是监听的部分是在 线程里面跑的 <将对应的变量放到线程里面也可以的>

    //------- begin ------ 初始化方法 --------------------
    // 构造方法， 参数 1 ， 服务器的名称； 参数 2 ， 传入的初始化好的 zkMonitor
    public NettyServer_v1(String serverName , zkMonitor zkMonitorHandler){
        this.serverName = serverName ;
        logger.debug("get server name " + serverName);

        logger.info("server starts a new thread to create local files");
        createLocalFile() ;

        this.zkMonitorHandler = zkMonitorHandler ;
        logger.debug("get zk monitor memeber variable set ");

        this.fingerTable = new Hashtable<String, ServerInfo>() ;
        logger.debug("create netty finger table");


        logger.info("init netty server components") ;

        bossGroup = new NioEventLoopGroup() ;
        workerGroup = new NioEventLoopGroup() ;
        b = new ServerBootstrap() ;

    }

    /**
     * 初始化 netty-server 方法
     * 主要功能： 根据构造方法中给出的 serverName 来从<远程 zk-server 节点上面|本地配置文件>读取该服务器名称
     * 对应的服务器的配置数据信息
     * */
    public void initNettyServer(){
        /**
         * 1. 创建配置信息缓存对象 PropsConf: 它的功能如下
         *                              a. 修改配置文件，并将修改的选项同步到文件中
         *                              b. 将配置文件从对应的路径下面读取出来（加载配置文件）
         *                              c. 创建配置文件
         *                              d. 删除配置文件
         *                              e. 向配置文件中添加新的 key-value 对
         *
         * 2. 检查 zk-handler 是否与远程的 zk-server 连接成功(这个影响后续程序的分支)
         * */

        // 1.
        this.propsConf = new PropsConf(serverName) ;

        // 2.
        if( zkMonitorHandler.isConnect()){

            logger.info("get server configuration info from remote zookeeper server ") ;

            // 从远程获取自己服务器上的配置信息
            serverInfo = zkMonitorHandler.getServerConfInfoFromZkServer(serverName) ;

            //  获取最新的配置信息之后，先别急着启动服务器，先更新本地的配置文件
            propsConf.updateByServerInfo( serverInfo  );

            logger.info("local properties file , already updated") ;

        } else{

            logger.info("can not connect to remote zookeeper server ") ;
            // 无法连接到远程获取配置信息
            // 通过 PropsConf 对象读取本地的配置文件来初始化 ServerInfo 对象

            // PropsConf 在构造方法中已经将本地 对应 'this.serverName' 的配置文件信息
            // 加载到新创建的 对象实例中

            logger.info("load server configuration info from local properties file");
            serverInfo = propsConf.getServerInfo() ;
        }

    }



    /**
     * 下面的方法用来运行 Netty-server 的server 监听线程的
     *
     * 这个方法的运行单位是线程，
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

    //------------------- 上面的单线程单独运行 netty-server-listener 已经通过测试了 --------------------
    // ---- 下面编写的是用于将自己的节点信息注册到 远程 zk-server 上面 -------------------
    public void registerToZkServer(){
        zkMonitorHandler.registerServerToRemote( serverInfo );
    }


    // 下面的方法是并发调用的方法，调用之后将会创建一个新的线程
    // 专门为 服务器 生成本地 的文件 , 这个方法在 构造方法中， 当服务器一接收到
    // ServerName 的赋值之后， 立即调用此方法来在本地对应的路径下面 生成 100 个长度位于 10K-10M 大小文件
    private void createLocalFile(){
        Thread fileCreaterFile = new Thread ( new FileUtil(serverName) ,"File generator thread") ;
        fileCreaterFile.start();
        logger.info("file generator thread start to create file");
    }



//------ end --------- 初始化方法 ------------------------------------------------


 // ================================ 供 netty-server-handler 调用的方法 ================================================

// --- 供 netty-server-handler 访问  tempFileTable 的方法 ----------------
    public void addFileDataToFileTable ( FileData fileData ){

        // 1. 将 FileData 对象传入到 tempFileTable 中
        this.tempFileTable.addNewFileInfo( fileData );


        // 2. 通过 tempFileTable 中的方法来获取当前接收到的文件的状态
            // 对应内部执行的方法的有---> FileData ---> 被转换成 FileInfo 存放到 tempFileTable 中
            // ---> 遍历 tempFileTable 中的每个元素 ，构造出 ServerInfoOnCluster 对象 ----> 调用JsonPacker
            // ----> 将 ServerInfoOnCluster 对象数据格式化成  String
        List<String> serverOnClusterInfoData = tempFileTable.getLastServerClusterInfo() ;

        String upLoadStringData = new String() ;

        for ( int i = 0 ; i < serverOnClusterInfoData.size() ; i++ ){
            // 将字符串拼接成 String
            String data = serverOnClusterInfoData.get(i) ;

            upLoadStringData += data ;

            // 为了解析的方便， 在每个 info-node 之间使用 '^' 作为分隔符号
            if( i != (serverOnClusterInfoData.size() -1) ){
                data += '^' ;
            }
        }

        // 3. 将状态数据通过 zkMonitorHandler 同步到 zk-server 的上面
        this.zkMonitorHandler.upLoadServerInfoOnCluster(upLoadStringData , serverInfo.getServerName());

    }


// -- 供 netty-server-handler 调用的方法， 将来自不同 服务器的 文件片段，追加到本地 data/不同的服务器名称/ 对应的路径下面
// 情况1 ： 如果该文件在本地没有被创建，说明这个文件段是文件的开始部分，之前没有接收到过; 这种情况只需要在对应的路径下面创建对应的文件夹和文件即可
// 情况2 ： 如果该文件在本地已经被创建了， 说明这个文件段是需要被追加在对应的路径的上面的，打开文件的写入数据流的方式，是需要以 追加的方式来实现
     public void localFileAppendWriter( FileData fileData  ){
         // 首先来，获取 fileData 的发送者的名称
         String filePrefixPath = "data/"+fileData.getSenderName() ;
         File fileDir  = new File( filePrefixPath ) ;

         // 通过检查文件夹是否存在，可以判断出当前接收文件的服务器是否曾经接收过来自该发送者发送的文件
         // 如果不存在对应文件夹---> 判断出：没有接收过， 动作: 创建该文件夹
         // 如果该文件夹存在 ------> 判断出: 接收过，     动作: 无动作
         if( !fileDir.exists()  ) // 不存在该文件夹： 创建文件夹
         {
             fileDir.mkdirs() ;
         }

         // 在对应的路径下面找 是否存在同名的文件
         String filePathName = filePrefixPath+'/'+fileData.getFileName() ;

         File file = new File( filePathName ) ;
         BufferedOutputStream bos ;

         try{

             // 如果找到同名文件， 说明，当前服务器不止一次接收到同名文件片段 ； 动作 : 追加的方式写入文件
             if(file.exists()){

                 bos = new BufferedOutputStream( new FileOutputStream(file , true )) ;

                 bos.write( fileData.getDataContent() );

                 bos.flush() ;

                 bos.close () ;
             }
             else {

                 // 找不到：首次接收文件片段，该片段是文件的头 ; 动作: 在本地创建文件，并将 FileData 中的数据写入到文件中
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
    // 下面的方法是用来调整 本类中的 tempFileTable 的size 的
    // 用于清除缓存的已经完成传输的文件块

    public void resizeTempFileTable(){
        this.tempFileTable.resizeTempFileList();
    }

// 下面的方法用来获取当前 finger-table 中的元素个数的
    public int getFingerTableLength (){
        return this.fingerTable.size() ;
    }

    //下面的方法是 finger_table 的添加、删除元素对应的方法，因为有可能涉及到 多线程同时访问
    // 所以在这里使用 synchronize 来修饰方法

    synchronized  public void addToFingerTable( ServerInfo addServerInfo ){

        if( this.fingerTable == null ){
            fingerTable = new Hashtable<String, ServerInfo>() ;
        }

        // 在这里不会出现同名 key 的元素，若是存在同名 key ，显示错误信息
        if(fingerTable.containsKey(addServerInfo.getServerName()) ){
            System.out.println("element with key " + addServerInfo.getServerName() +" already exists ! error") ;
            return ;
        }

        fingerTable.put(addServerInfo.getServerName(), addServerInfo) ;

    }

    synchronized  public void removeFromFingerTable (String deleteServerInfoName ){
        // 先来检查一下， fingerTable 是否为空或者是没有元素
        if(fingerTable == null || fingerTable.size() == 0){
            System.out.println("no elements in finger-table , failed to delete element " + deleteServerInfoName) ;
        }

        // 再来检查一下，是否存在对应的元素
        if( !fingerTable.containsKey( deleteServerInfoName)){
            System.out.println("can not find element with name " + deleteServerInfoName +" in finger table , error") ;
            return ;
        }

        // 最后排除所有情况， 删除对应元素
        fingerTable.remove(deleteServerInfoName) ;

    }

 // 下面的方法用于停止服务器的进程以及释放相关的空间
    public void shutDownServer(){
        // 1. 首先停止 listener 线程
        bossGroup.shutdownGracefully() ;
        workerGroup.shutdownGracefully() ;

        // 2. 调用退出程序
        System.exit(0);
    }


    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public String getServerName() {
        return serverName;
    }


}

