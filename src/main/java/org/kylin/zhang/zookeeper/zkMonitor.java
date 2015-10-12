package org.kylin.zhang.zookeeper;

import com.sun.corba.se.spi.activation.Server;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.message.Message;
import org.kylin.zhang.message.MessageBuilder;
import org.kylin.zhang.message.MessageType;
import org.kylin.zhang.util.JsonPacker;
import org.kylin.zhang.util.MsgPacker;
import org.kylin.zhang.util.xmlLoader;
import org.kylin.zhang.zookeeper.ListenerBuilder.CacheListenBuilder;
import org.kylin.zhang.zookeeper.sender.zkNettyClient;
import org.msgpack.util.json.JSONUnpacker;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by win-7 on 2015/10/7.
 *
 * 这个类主要用来进行
 * 1. 启动 zk 路径监控线程
 * 2. 调用 封装了 zk-dao 的方法
 *
 * 功能主要分为 3 个部分:
 *  1. 监听的初始化和启动运行----> 单独线程执行
 *
 *  2. 供 netty-server 启动之前， 从 zk-server 的配置信息路径上面获取配置信息
 *
 *  3.供 netty-server 将自己需要监听的信息注册到 zk-server 的被监听路径 上面 以及， 服务器的 接收文件的状况的实时抓取
 *
 *  (是的没有错， 监听线程先启动， 然后是 被监听者的信息注册)
 *
 *  4. 修改监听路径的时候， 对应触发的回调方法：各种 handler
 */
public class zkMonitor {

    private String zkIP ; // zk-client 需要远程访问的 zk-server 的 ip：port
    private short zkPort ;

    private Map<String , ServerInfo> registerServerInfoTable ;

    private zkDao zkClientHandler  ; // 远程 zk-servre 操作代理

    private xmlLoader xmlLoader ; // 这个是用来从配置文件中导入 zk 和 要上传到 配置文件路径上面 各个节点的配置信息的加载对象


    private  String mainPath ;       // 将要在 zk-server 上面注册的主路径
    private  String confPath  ;     // /主路径/配置文件存放路径(confPath)
    private  String listenPath  ;  // /主路径/监听各个节点信息的路径

//    private List<ServerInfo> confServerInfoList ;
    // 这个变量用来存放的是，通过 zkMonitor 注册到 /主路径/listend/ 路径下面的服务器的名称和配置信息
    // 将从 zk-conf.xml  文件中获取的 服务器配置信息 写入到这个变量中是不对的；
    // 因为 zk-server 可能保存供 server 1 访问的配置信息， 但是如果 server 1 没有成功启动，那么就不会 zk-server
    // 上面注册信息 ，也就是 存放的服务器配置信息， 并不代表这些服务器一定会 被启动

//= begin ====================== 第一部分， 创建， 初始化， 和 单独监听线程的运行 ======================================

    public zkMonitor( String zkIP , short zkPort ){

        this.zkIP = zkIP ;
        this.zkPort = zkPort ;

        zkClientHandler = new zkDao(zkIP , zkPort) ;


        registerServerInfoTable = new Hashtable<String, ServerInfo>() ;


        // connect zk-handler to zk-server
        zkClientHandler.connectToServer();

        // 创建 配置文件加载器

        xmlLoader = new xmlLoader() ;
        xmlLoader.parseXML();

        // 通过配置文件加载器来获取 主路径等配置文件的消息

        mainPath     = xmlLoader.getMainPath();
        confPath     = xmlLoader.getConfPath() ;
        listenPath   = xmlLoader.getListenPath() ;

    }

    // 下面的方法是在 zk-server 上面创建初始路径的
    // 在 zk-server 上面创建的路径是需要从 zk-conf.xml 这个配置文件中读取的

    public void initZkServerPaths (){

        zkClientHandler.addPath(mainPath, null);
        zkClientHandler.addPath(mainPath+confPath,null);
        zkClientHandler.addPath(mainPath+listenPath, null );


        // 如果添加配置信息导入加载的话， 在这个地方上传配置信息

        // 好的， 在这里进行加载 , by nas
        // 下面获取的 队列是 从 zk-conf.xml 配置文件中获取的多个 server 的配置信息

        // 也就是说，需要上传到 zk-server 配置文件路径下面的数据已经从 配置文件中被加载到
        // 内存变量中

        List<ServerInfo> upLoadServerInfoDataList = xmlLoader.getConfDataList() ;

        // 将被夹在到内存变量中的数据，对应各自的 server-name 在 zk-server 上面创建路径并将
        // 对应 server-name  的 ServerInfo 上传到 zk-server 的对应路径下面

        String prefixPathName = mainPath+confPath+'/' ;

        for( ServerInfo info : upLoadServerInfoDataList ){

            // 和前面的版本不同的是，在这里进行修改， 上传的数据是将 ServerInfo 进行 json 化 成 String
            // 然后  .getByte() ---> byte [] 对象
            String pathName = prefixPathName + info.getServerName() ;
            byte [] upLoadData = JsonPacker.getJsonString( info ).getBytes() ;

            // 上传到 zk-server
            zkClientHandler.addPath(pathName , upLoadData);

        }
    }
    //======  init listen =========================
// 是 zk 用来监听 /主路径/被监听路径 下面的子路径的调用 方法 ， 这个方法被 startListen 方法所 调用， listen step1
    private void listenToPath (){

        try {
            PathChildrenCache listenerCache = new PathChildrenCache(this.zkClientHandler.getZkClientHandler(), mainPath + listenPath, true);

            listenerCache.start();

            listenerCache.getListenable().addListener(CacheListenBuilder.getInstance(this));

            System.out.println("zk listener start listen to " + mainPath + listenPath) ;

            while(true) ;

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //-------------- 此方法为单独线程运行方法， 一旦启动便会脱离主线程自己运行， -------------------------
    // 创建单独的线程来运行监听路径的方法 , 将会调用 listenToPath ， listen step2
    public void startListen(){

        new Thread("zookeeper_path_listener"){
            @Override
            public void run(){
                listenToPath();
            }
        }.start();
    }


    //=========== listener thread run ======================

//= end ====================== 第一部分， 创建， 初始化， 和 单独监听线程的运行 ========================================



//= begin ====================== 第二部分包含了供 netty-server 获取配置信息的方法的封装 ================================

    /**
     * 下面的方法是从 zk-server 的 /主路径/配置路径/{server1, server2, ... } 下面获取数据,并将数据直接打包成 ServerInfo 的方法，
     * @param :serverName ， 在这里，传入的是服务器的名称，不要传入服务器所在路径的名称， 路径名对于 client 端是不可见的
     * */
    public ServerInfo getServerConfInfoFromZkServer(String serverName ){

        byte [] data = zkClientHandler.getDataByPath(mainPath+confPath+'/'+serverName) ;

        // data 的类型检查，在MsgPacker 会执行， 如果不合法，在 MsgPacker 中会抛出异常
        ServerInfo  serverConfInfo = (ServerInfo) JsonPacker.getJsonObject(new String (data), ServerInfo.class) ;

        return serverConfInfo ;
    }

//= end ====================== 第二部分包含了供 netty-server 获取配置信息的方法的封装 ================================



    //= begin  ====================== 第三部分包含了供 netty-server 向监听路径上面注册信息的方法封装========================
// -------------- zk 为 nettty-server 提供的 将服务器节点信息 注册到 /主路径/被监听路径/{server1, server2.... }的方法
    public void registerServerToRemote ( ServerInfo serverInfo ){
        //     registerServerInfoTable.put(serverInfo.getServerName() , serverInfo) ;

        byte [] uploadData = JsonPacker.getJsonString(serverInfo).getBytes() ;

        zkClientHandler.addPath(mainPath+listenPath+'/'+serverInfo.getServerName() , uploadData );
    }

    //-------------- 下面的这个方法是用来将 netty-server 上的信息同步到 zk-server 上面的
    public void upLoadServerInfoOnCluster( String jsonInfoString , String serverName ){

        this.zkClientHandler.updateData(mainPath+listenPath+'/'+serverName , jsonInfoString.getBytes()) ;
    }

//= end  ====================== 第三部分包含了供 netty-server 向监听路径上面注册信息的方法封装=========================



    //-.-begin.-.-.-.-.-.-.-.-.- 公共方法.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.
    //-----------判断是否成功连接到服务器端--------------
    public boolean isConnect(){
        return this.zkClientHandler.isConnect() ;
    }

    // 这个方法用来移除 zk 的主路径

    public void reset(){
        zkClientHandler.deletePath(mainPath);
    }

    //-.-end   .-.-.-.-.-.-.-.-.-.-公共方法.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.



    // 在下面对上述的方法进行测试
    public static void main (String [] args ){
        zkMonitor zkMonitor = new zkMonitor("127.0.0.1" , (short)2181) ;

       /* zkMonitor.initZkServerPaths();

        zkMonitor.listenToPath(); */

        zkMonitor.reset();
    }

    //================== 下面的方法对应的是 zkMonitor 中供 CacheListenerBuilder 触发调用的函数  begin ============================
    synchronized public void addNewRegisterServer(ServerInfo newComer ){

        if( registerServerInfoTable.size() == 0  ){
            System.out.println("way 1 ") ;
            registerServerInfoTable.put(newComer.getServerName() , newComer) ;
            return ;
        }

        if(registerServerInfoTable.size() >= 1){
            System.out.println("way 2") ;
            ServerInfo smallerServerNameInfo ; //zk 发送消息的消息接收者
            ServerInfo largerServerNameInfo ;  // zk 发送消息的消息的数据封装对象

            for( String serverName : registerServerInfoTable.keySet()){

                ServerInfo value = registerServerInfoTable.get(serverName) ;

                if( value.getServerName().compareTo( newComer.getServerName()) > 0   ) {
                    smallerServerNameInfo = newComer; // receiver
                    largerServerNameInfo = value;    // data
                }
                else
                {
                    smallerServerNameInfo = value ;
                    largerServerNameInfo = newComer ;
                }


                // 创建消息对象
                Message zkOnlineMessage = MessageBuilder.getServerInfoDataInstance(MessageType.ZK_ONLINE , largerServerNameInfo) ;

                System.out.println("larger name " + largerServerNameInfo.getServerName() +" smaller name " + smallerServerNameInfo.getServerName()+"  \n zk will send message to "+smallerServerNameInfo.getServerName()) ;
                // 创建发送消息线程
                new Thread( new zkNettyClient(zkOnlineMessage, smallerServerNameInfo) , "zk's netty-client sender thread").start();


            }

            // 将消息添加到 table 中
            registerServerInfoTable.put(newComer.getServerName() , newComer ) ;
        }
    }

    synchronized public void deleteRegisterServer(String serverName ){
        if( registerServerInfoTable.size() == 1 ) //说明网络中只有自己一个节点了， 下线无需告诉任何人
        {
            registerServerInfoTable.remove(serverName) ;
        }
        else{

            System.out.println() ;
            // 首先，要把这个即将下线的节点 从 注册表中移除
            ServerInfo offLineServerInfo = registerServerInfoTable.get(serverName) ;

            registerServerInfoTable.remove(serverName) ;

            // 因为一个消息的类型和加载的数据不变， 所以创建一个 多次发送即可，无需创建多次
            Message message = MessageBuilder.getServerInfoDataInstance(MessageType.ZK_OFFLINE , offLineServerInfo)  ;

            // 然后要把消息告诉给 registerServerInfoTable 中剩余的每一个服务器
            for( String key : registerServerInfoTable.keySet() ){

                // 1. 获取 server-info 数据
                ServerInfo  receiver = registerServerInfoTable.get(key) ;

                // 2. 创建需要发送的 Message:param 1 消息类型 ， param 2 需要格式化的消息
                // 已经在上面创建好了

                // 3. 现在起一个线程将消息发送给 receiver 对应的服务器
               new Thread ( new zkNettyClient(message, receiver) , "send zk off-line message to every server on line").start();

            }
        }
    }

//================== 下面的方法对应的是 zkMonitor 中供 CacheListenerBuilder 触发调用的函数  end   ============================



//----------getter and setter -------------------

    public String getConfPath() {
        return confPath;
    }

    public void setConfPath(String confPath) {
        this.confPath = confPath;
    }

    public String getListenPath() {
        return listenPath;
    }

    public void setListenPath(String listenPath) {
        this.listenPath = listenPath;
    }

    public String getMainPath() {
        return mainPath;
    }

    public void setMainPath(String mainPath) {
        this.mainPath = mainPath;
    }

    public Map<String, ServerInfo> getRegisterServerInfoTable() {
        return registerServerInfoTable;
    }

    public void setRegisterServerInfoTable(Map<String, ServerInfo> registerServerInfoTable) {
        this.registerServerInfoTable = registerServerInfoTable;
    }


    public String getZkIP() {
        return zkIP;
    }

    public void setZkIP(String zkIP) {
        this.zkIP = zkIP;
    }

    public short getZkPort() {
        return zkPort;
    }

    public void setZkPort(short zkPort) {
        this.zkPort = zkPort;
    }

    // this is the final run method , when create an instance , run this
    // it will get everything done

    public void runZkMonitor (){
        this.initZkServerPaths();
        this.startListen();
    }

}
