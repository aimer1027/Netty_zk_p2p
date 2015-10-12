package m10.day11.messgeLogicTests;

import org.junit.Test;
import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.zookeeper.zkDao;
import org.kylin.zhang.zookeeper.zkMonitor;

/**
 * Created by win-7 on 2015/10/11.
 *
 * ���������������Ҫ������
 *
 * 2 �� netty-server ��server-handler ��Ϣ�׷��߼���
 * server1 ���յ���Ϣ֮�󣬻��� server2 �������� �ļ�����Ϣ
 * server2 �ڽ��յ� server 1 �������ļ���Ϣ֮��ظ� ׼��������Ϣ
 * server1 �ڽ��յ� server2 �� ׼��������Ϣ֮�󣬻�ظ� ׼��������Ϣ
 *
 *
 * �����룬����������ô����̣߳��ǲ��ǣ�
 * ʹ��һ���̳߳ض�����һЩ��
 *
 */
public class logic1Test {

    public static void main (String [] args )throws Exception {

        zkMonitor zkMonitor = new zkMonitor("127.0.0.1", (short)2181) ;
        zkMonitor.initZkServerPaths();
        zkMonitor.startListen();

        // wait for a moment
        Thread.sleep(2000);

        NettyServer_v1 ntServer1 = new NettyServer_v1("server1" , zkMonitor) ;
        NettyServer_v1 ntServer2 = new NettyServer_v1("server2" , zkMonitor) ;
        NettyServer_v1 ntServer3 = new NettyServer_v1("server3", zkMonitor) ;

      /*  NettyServer_v1 ntServer4 = new NettyServer_v1("server4" , zkMonitor) ;
        NettyServer_v1 ntServer5 = new NettyServer_v1("server5" , zkMonitor) ;*/

        System.out.println("generating data , wait .... ") ;

        ntServer1.initNettyServer();
        ntServer2.initNettyServer();
        ntServer3.initNettyServer();
    /*    ntServer4.initNettyServer();
        ntServer5.initNettyServer();*/

        new Thread(ntServer1 , "server1 listener thread").start();


        new Thread(ntServer2, "server2 listener thread").start();

        new Thread(ntServer3,"server3 listener thread").start();

      /*  new Thread(ntServer4 , "server4 listener thread").start();

        new Thread(ntServer5,"server5  listener thread").start();*/


        System.out.println("wait server1 , server2 start ") ;

        // ������ȴ� 3 ���Ŀ���ǣ��ȴ� server1 , server2 �ļ����߳���ȫ����֮��
        // Ҳ���� server �������˿�ʼ �����˿� �� ������Ϣ֮��Ҳ���� online ֮���ٽ����� online ״̬�� server ״̬ע�ᵽ
        // Զ�� zk-server �����棬���� zk-server �����ĳһ�� server �Ķ�Ӧ�˿ڷ�����Ϣ�Ļ��� ��Ϣ�ᱻ���յ�

        Thread.sleep(3000) ;

        ntServer1.registerToZkServer();

        ntServer2.registerToZkServer();
        // ����ط����ص�۲�Ķ���
        /**
         * ����ǣ�浽������ :
         * CacheListenBuilder --> ����� zk-monitor �� addNewRegisterServer
         * ---> zk-monitor ��  --> zk�ᷢ����Ϣ �� server 1 Ȼ����뵽
         *
         * server1 <---> server2 ���߼���
         *
         * */
        ntServer3.registerToZkServer();
/*

        ntServer4.registerToZkServer();

        ntServer5.registerToZkServer();
*/


    }

    @Test
    public void resetZkPath (){
        zkDao dao = new zkDao("127.0.0.1", (short)2181) ;

        dao.connectToServer();

        dao.deletePath("/Aimer");

        dao.closeConnect();

    }

    @Test
    public void testError(){
        System.out.println("well done") ;
    }

}
