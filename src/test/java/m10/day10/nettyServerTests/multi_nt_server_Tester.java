package m10.day10.nettyServerTests;

import org.junit.Test;
import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.zookeeper.zkDao;
import org.kylin.zhang.zookeeper.zkMonitor;

/**
 * Created by win-7 on 2015/10/10.
 */
public class multi_nt_server_Tester {

    public static void main (String [] args )throws Exception {

        zkMonitor zkMonitor = new zkMonitor("127.0.0.1", (short)2181) ;
        zkMonitor.initZkServerPaths();
        zkMonitor.startListen();

        // wait for a moment
        Thread.sleep(2000);

        NettyServer_v1 ntServer1 = new NettyServer_v1("server1" , zkMonitor) ;
        NettyServer_v1 ntServer2 = new NettyServer_v1("server2" , zkMonitor) ;

        ntServer1.initNettyServer();
        ntServer2.initNettyServer();

        new Thread(ntServer1 , "server1 listener thread").start();

        new Thread(ntServer2, "server2 listener thread").start();

        System.out.println("wait server1 , server2 start ") ;
        Thread.sleep(3000) ;

        ntServer1.registerToZkServer();


        ntServer2.registerToZkServer();


    }

    @Test
    public void resetZkPath (){
        zkDao dao = new zkDao("127.0.0.1", (short)2181) ;

        dao.connectToServer();

        dao.deletePath("/Aimer");

        dao.closeConnect();

    }

}
