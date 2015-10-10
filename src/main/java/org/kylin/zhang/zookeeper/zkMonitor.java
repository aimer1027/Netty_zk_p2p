package org.kylin.zhang.zookeeper;

import com.sun.corba.se.spi.activation.Server;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.log4j.Logger;
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
 * �������Ҫ��������
 * 1. ���� zk ·������߳�
 * 2. ���� ��װ�� zk-dao �ķ���
 *
 * ������Ҫ��Ϊ 3 ������:
 *  1. �����ĳ�ʼ������������----> �����߳�ִ��
 *
 *  2. �� netty-server ����֮ǰ�� �� zk-server ��������Ϣ·�������ȡ������Ϣ
 *
 *  3.�� netty-server ���Լ���Ҫ��������Ϣע�ᵽ zk-server �ı�����·�� ���� �Լ��� �������� �����ļ���״����ʵʱץȡ
 *
 *  (�ǵ�û�д� �����߳��������� Ȼ���� �������ߵ���Ϣע��)
 *
 *  4. �޸ļ���·����ʱ�� ��Ӧ�����Ļص����������� handler
 */
public class zkMonitor {

    private  static Logger zkLogger = Logger.getLogger(zkMonitor.class.getName()) ;

    private String zkIP ; // zk-client ��ҪԶ�̷��ʵ� zk-server �� ip��port
    private short zkPort ;

    private Map<String , ServerInfo> registerServerInfoTable ;

    private zkDao zkClientHandler  ; // Զ�� zk-servre ��������

    private xmlLoader xmlLoader ; // ����������������ļ��е��� zk �� Ҫ�ϴ��� �����ļ�·������ �����ڵ��������Ϣ�ļ��ض���


    private  String mainPath ;       // ��Ҫ�� zk-server ����ע�����·��
    private  String confPath  ;     // /��·��/�����ļ����·��(confPath)
    private  String listenPath  ;  // /��·��/���������ڵ���Ϣ��·��

//    private List<ServerInfo> confServerInfoList ;
    // �������������ŵ��ǣ�ͨ�� zkMonitor ע�ᵽ /��·��/listend/ ·������ķ����������ƺ�������Ϣ
    // ���� zk-conf.xml  �ļ��л�ȡ�� ������������Ϣ д�뵽����������ǲ��Եģ�
    // ��Ϊ zk-server ���ܱ��湩 server 1 ���ʵ�������Ϣ�� ������� server 1 û�гɹ���������ô�Ͳ��� zk-server
    // ����ע����Ϣ ��Ҳ���� ��ŵķ�����������Ϣ�� ����������Щ������һ���� ������

//= begin ====================== ��һ���֣� ������ ��ʼ���� �� ���������̵߳����� ======================================

    public zkMonitor( String zkIP , short zkPort ){

        this.zkIP = zkIP ;
        this.zkPort = zkPort ;

        zkClientHandler = new zkDao(zkIP , zkPort) ;
        zkLogger.info("zkMonitor creates zkDao object ");

        registerServerInfoTable = new Hashtable<String, ServerInfo>() ;
        zkLogger.info("zkMonitor creates netty server registers table");

        // connect zk-handler to zk-server
        zkClientHandler.connectToServer();
        zkLogger.info("zkMonitor creates connection to remote zookeeper server");

        // ���� �����ļ�������

        xmlLoader = new xmlLoader() ;
        xmlLoader.parseXML();

        zkLogger.info("zkMonitor create xml configuration file loader") ;

        // ͨ�������ļ�����������ȡ ��·���������ļ�����Ϣ

        mainPath     = xmlLoader.getMainPath();
        confPath     = xmlLoader.getConfPath() ;
        listenPath   = xmlLoader.getListenPath() ;

    }

    // ����ķ������� zk-server ���洴����ʼ·����
    // �� zk-server ���洴����·������Ҫ�� zk-conf.xml ��������ļ��ж�ȡ��

    public void initZkServerPaths (){

        zkClientHandler.addPath(mainPath, null);
        zkClientHandler.addPath(mainPath+confPath,null);
        zkClientHandler.addPath(mainPath+listenPath, null );

        zkLogger.info("zkMonitor create main path "+ mainPath +" conf path "+
                mainPath+confPath +" listen path " + mainPath+listenPath +" on zookeeper remote server ");
        // ������������Ϣ������صĻ��� ������ط��ϴ�������Ϣ

        // �õģ� ��������м��� , by nas
        // �����ȡ�� ������ �� zk-conf.xml �����ļ��л�ȡ�Ķ�� server ��������Ϣ

        // Ҳ����˵����Ҫ�ϴ��� zk-server �����ļ�·������������Ѿ��� �����ļ��б����ص�
        // �ڴ������

        List<ServerInfo> upLoadServerInfoDataList = xmlLoader.getConfDataList() ;

        // �������ڵ��ڴ�����е����ݣ���Ӧ���Ե� server-name �� zk-server ���洴��·������
        // ��Ӧ server-name  �� ServerInfo �ϴ��� zk-server �Ķ�Ӧ·������

        String prefixPathName = mainPath+confPath+'/' ;

        for( ServerInfo info : upLoadServerInfoDataList ){

            // ��ǰ��İ汾��ͬ���ǣ�����������޸ģ� �ϴ��������ǽ� ServerInfo ���� json �� �� String
            // Ȼ��  .getByte() ---> byte [] ����
            String pathName = prefixPathName + info.getServerName() ;
            byte [] upLoadData = JsonPacker.getJsonString( info ).getBytes() ;

            // �ϴ��� zk-server
            zkClientHandler.addPath(pathName , upLoadData);

        }

        zkLogger.info("zkMonitor already create all netty-servers conf path ");
    }
    //======  init listen =========================
// �� zk �������� /��·��/������·�� �������·���ĵ��� ���� �� ��������� startListen ������ ���ã� listen step1
    private void listenToPath (){

        zkLogger.info( "zkMonitor 's server listener is running on path " + mainPath+listenPath ) ;

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
    //-------------- �˷���Ϊ�����߳����з����� һ����������������߳��Լ����У� -------------------------
   // �����������߳������м���·���ķ��� , ������� listenToPath �� listen step2
    public void startListen(){

        new Thread("zookeeper_path_listener"){
            @Override
            public void run(){
                listenToPath();
            }
        }.start();
    }


    //=========== listener thread run ======================

//= end ====================== ��һ���֣� ������ ��ʼ���� �� ���������̵߳����� ========================================



//= begin ====================== �ڶ����ְ����˹� netty-server ��ȡ������Ϣ�ķ����ķ�װ ================================

    /**
     * ����ķ����Ǵ� zk-server �� /��·��/����·��/{server1, server2, ... } �����ȡ����,��������ֱ�Ӵ���� ServerInfo �ķ�����
     * @param :serverName �� �����������Ƿ����������ƣ���Ҫ�������������·�������ƣ� ·�������� client ���ǲ��ɼ���
     * */
    public ServerInfo getServerConfInfoFromZkServer(String serverName ){

        byte [] data = zkClientHandler.getDataByPath(mainPath+confPath+'/'+serverName) ;

        // data �����ͼ�飬��MsgPacker ��ִ�У� ������Ϸ����� MsgPacker �л��׳��쳣
        ServerInfo  serverConfInfo = (ServerInfo) JsonPacker.getJsonObject(new String (data), ServerInfo.class) ;

        return serverConfInfo ;
    }

//= end ====================== �ڶ����ְ����˹� netty-server ��ȡ������Ϣ�ķ����ķ�װ ================================



//= begin  ====================== �������ְ����˹� netty-server �����·������ע����Ϣ�ķ�����װ========================
// -------------- zk Ϊ nettty-server �ṩ�� ���������ڵ���Ϣ ע�ᵽ /��·��/������·��/{server1, server2.... }�ķ���
    public void registerServerToRemote ( ServerInfo serverInfo ){
   //     registerServerInfoTable.put(serverInfo.getServerName() , serverInfo) ;

        byte [] uploadData = JsonPacker.getJsonString(serverInfo).getBytes() ;

        zkClientHandler.addPath(mainPath+listenPath+'/'+serverInfo.getServerName() , uploadData );
    }

        //-------------- �������������������� netty-server �ϵ���Ϣͬ���� zk-server �����
    public void upLoadServerInfoOnCluster( String jsonInfoString , String serverName ){

           this.zkClientHandler.updateData(mainPath+listenPath+'/'+serverName , jsonInfoString.getBytes()) ;
    }

//= end  ====================== �������ְ����˹� netty-server �����·������ע����Ϣ�ķ�����װ=========================



    //-.-begin.-.-.-.-.-.-.-.-.- ��������.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.
        //-----------�ж��Ƿ�ɹ����ӵ���������--------------
        public boolean isConnect(){
            return this.zkClientHandler.isConnect() ;
        }

        // ������������Ƴ� zk ����·��

        public void reset(){
            zkClientHandler.deletePath(mainPath);
        }

    //-.-end   .-.-.-.-.-.-.-.-.-.-��������.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.



    // ������������ķ������в���
    public static void main (String [] args ){
        zkMonitor zkMonitor = new zkMonitor("127.0.0.1" , (short)2181) ;

       /* zkMonitor.initZkServerPaths();

        zkMonitor.listenToPath(); */

    zkMonitor.reset();
    }

//================== ����ķ�����Ӧ���� zkMonitor �й� CacheListenerBuilder �������õĺ���  begin ============================
    synchronized public void addNewRegisterServer(ServerInfo newComer ){

        if( registerServerInfoTable.size() == 0  ){
            System.out.println("way 1 ") ;
            registerServerInfoTable.put(newComer.getServerName() , newComer) ;
            return ;
        }

        if(registerServerInfoTable.size() >= 1){
                System.out.println("way 2") ;
            ServerInfo smallerServerNameInfo ; //zk ������Ϣ����Ϣ������
            ServerInfo largerServerNameInfo ;  // zk ������Ϣ����Ϣ�����ݷ�װ����

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


                    // ������Ϣ����
                    Message zkOnlineMessage = MessageBuilder.getServerInfoDataInstance(MessageType.ZK_ONLINE , largerServerNameInfo) ;

                    zkLogger.info("zkMonitor create a message sender thread , send message to " + smallerServerNameInfo) ;
                    // ����������Ϣ�߳�
                    new Thread( new zkNettyClient(zkOnlineMessage, smallerServerNameInfo) , "zk's netty-client sender thread").start();

                    // ����Ϣ��ӵ� table ��
                    registerServerInfoTable.put(newComer.getServerName() , newComer ) ;

            }


        }
    }

    synchronized public void deleteRegisterServer(ServerInfo offLineServerInfo ){
            if( registerServerInfoTable.size() == 1 ) //˵��������ֻ���Լ�һ���ڵ��ˣ� ������������κ���
            {
                registerServerInfoTable.remove(offLineServerInfo.getServerName()) ;
            }
            else{
                // ���ȣ�Ҫ������������ߵĽڵ� �� ע������Ƴ�
                registerServerInfoTable.remove(offLineServerInfo.getServerName()) ;

                // ��Ϊһ����Ϣ�����ͺͼ��ص����ݲ��䣬 ���Դ���һ�� ��η��ͼ��ɣ����贴�����
                Message message = MessageBuilder.getServerInfoDataInstance(MessageType.ZK_OFFLINE , offLineServerInfo)  ;

                // Ȼ��Ҫ����Ϣ���߸� registerServerInfoTable ��ʣ���ÿһ��������
                for( String key : registerServerInfoTable.keySet() ){

                    // 1. ��ȡ server-info ����
                    ServerInfo  receiver = registerServerInfoTable.get(key) ;

                    // 2. ������Ҫ���͵� Message:param 1 ��Ϣ���� �� param 2 ��Ҫ��ʽ������Ϣ
                    // �Ѿ������洴������

                    // 3. ������һ���߳̽���Ϣ���͸� receiver ��Ӧ�ķ�����
                    new Thread ( new zkNettyClient(message, receiver) , "send zk off-line message to every server on line").start();

                }
            }
    }

//================== ����ķ�����Ӧ���� zkMonitor �й� CacheListenerBuilder �������õĺ���  end   ============================



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
}
