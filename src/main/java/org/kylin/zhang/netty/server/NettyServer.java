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
    private Map<String , ServerInfo> fingerTable ;// 这个是用来存放临近节点的

    // 添加 zk-monitor 作为成员变量
    private zkMonitor zookeeperMonitor  ;


    //------------ 下面的方法封装了 将 当前的服务器 信息 注册到 zk-server 的上面
    public void registerToZkServer(){
        zookeeperMonitor.registerServerToRemote(serverInfo);
    }


    //-------------------- 下面的方法封装了 tempFileTable 的DAO 方法
    public void addFileDataToFileTable ( FileData fileData ){

        this.tempFileTable.addNewFileInfo(fileData);

        //在加入新元素之后， 立即获取 tempFileTable 中的数据，然后将信息同步到远程 zk-server 的上面

        List<String> zkDataList =   tempFileTable.getLastServerClusterInfo() ;

        // 在这里，因为 zk 句柄还没有写， 所以暂时的输出显示一下就可以了
        // 一会实验一下， client 向 server 一次性发送多个 文件； 以及多个 client 向 server 发送多个文件

        // 刚刚测试了一下， 不过， 这个追加的 文件长度方面还是有问题
 //       System.out.println(" server " + serverInfo.getServerName() + " info ") ;

        String upLoadData = new String() ;

        for (int i = 0 ; i < zkDataList.size() ; i++ ){
           String data = zkDataList.get(i) ;
          upLoadData += data  ;

            if( i != (zkDataList.size()-1) )
                upLoadData+='&' ;
        }

        // 每次都是将所有的 zkDataList 中所有的元素全部收集一下，在将其上传到 cluster 中

        this.updateServerInfoOnCluster(upLoadData);

     //   System.out.println("upload data " + upLoadData) ;
       // System.out.println("---------------------------------------- \n\n") ;
    }

    //----------------- 构造方法
    public NettyServer(){
         tempFileTable = new tempFileTable() ;
         fingerTable = new Hashtable<String, ServerInfo>() ;
    }

    public NettyServer(  String serverName , zkMonitor zkMonitor){

        this() ;

        this.zookeeperMonitor = zkMonitor ;
    }

    // 下面的构造方法仅仅用在测试， 因为正常的顺序是 1. 给定 serverName 2. 到 zk 上加载/读取配置文件 来实例化 ServerInfo 对象
    public NettyServer (ServerInfo serverInfo, zkMonitor zkMonitor){
        this() ;

        this.serverInfo = serverInfo ;
        this.zookeeperMonitor = zkMonitor ;
    }

    // ---------- 下面的方法是将当前 server 中的配置信息，同步到 zookeeper-server 对应路径下面的数据信息上
    public void updateServerInfoOnCluster(String dataContent ){
        zookeeperMonitor.upLoadServerInfoOnCluster(dataContent, serverInfo.getServerName());
    }

    // ------------ 本地文件追加方法 -------------------
    public void localFileAppender( FileData fileData ){

        // 先来获取 fileData 中的发送者 server-name
        String folderPathName = "data/"+fileData.getSenderName() ;
        System.out.println( fileData.getSenderName() ) ;
        File dir = new File(folderPathName) ;


        // server-name 在本地 data 路径的下面是否存在 以该 server-name 为名字的 文件夹
        // 存在--> 无动作
        // 不存在 --> 创建一个同名的文件夹
        if( dir.exists() && !dir.isDirectory() ) // 存在但并不是文件夹
            dir.delete() ;
        if(!dir.exists())
            dir.mkdirs() ;


        // 找该路径下面是否存在同名的文件
        String filePath = folderPathName+'/'+"received_"+fileData.getFileName() ;
        File file = new File (filePath) ;
        BufferedOutputStream bos  ;

        try{

            // 存在 --> 追加的方式打开该文件
            if( file.exists())
            {
                bos = new BufferedOutputStream( new FileOutputStream( file, true)) ;

                bos.write( fileData.getDataContent());

                bos.flush();

                bos.close();
            }
            else {
                // 不存在 ---> 直接通过 File.createNewFile 创建一个文件出来，然后将数据写入其中
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


    //-------------- netty-server 监听方法 ---------
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




    //----------------- getter setter 方法


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
