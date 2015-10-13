import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.zookeeper.zkMonitor;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by win-7 on 2015/10/10.
 *
 * ����� main ������������Ŀ����ں���
 * ���� ���� zkMonitor
 * ���������� 5 �� NettyServer
 *
 *  �������ղ����� zookeeper �Լ� ���� NettyServer �����ò�����
 *  zk-conf.xml �ļ��н�������
 *
 *  ���ɵ� jar �ļ�Ҳ���Ը� main ������Ϊ��ں�����
 *  ����һ�� ZkCmdToolStartUp ��Ϊ��Ⱥ״̬��Ϣչʾ �ͻ���
 */
public class Main {

    public static void main (String [] args ) throws Exception {

        zkMonitor zkMonitor = null ;

        if( args[0].equals("with_zk")){

         zkMonitor = new zkMonitor("127.0.0.1" , (short)2181) ;

        zkMonitor.runZkMonitor();

        }
        if( args[0].equals("without_zk")){
            zkMonitor = null ;

            System.out.println("---- no zookeeper available, set up netty-server 1-5 by loading local properties -----") ;
        }

        // else running --> zkMonitor --> set it null

        NettyServer_v1 nettyServer1 = new NettyServer_v1("server1" , zkMonitor) ;
        System.out.println(nettyServer1.getServerName()+" generating local file in path data/"+nettyServer1.getServerName()) ;

        NettyServer_v1 nettyServer2 = new NettyServer_v1("server2" , zkMonitor) ;
        System.out.println(nettyServer2.getServerName()+" generating local file in path data/"+nettyServer2.getServerName()) ;

        NettyServer_v1 nettyServer3 = new NettyServer_v1("server3" , zkMonitor) ;
        System.out.println(nettyServer3.getServerName()+" generating local file in path data/"+nettyServer3.getServerName()) ;

        NettyServer_v1 nettyServer4 = new NettyServer_v1("server4" , zkMonitor) ;
        System.out.println(nettyServer4.getServerName()+" generating local file in path data/"+nettyServer4.getServerName()) ;

        NettyServer_v1 nettyServer5 = new NettyServer_v1("server5" , zkMonitor) ;
        System.out.println(nettyServer5.getServerName()+" generating local file in path data/"+nettyServer5.getServerName()) ;


        nettyServer1.runNettyServer();
        nettyServer2.runNettyServer();
        nettyServer3.runNettyServer();
        nettyServer5.runNettyServer();
        nettyServer4.runNettyServer();

    }

}
