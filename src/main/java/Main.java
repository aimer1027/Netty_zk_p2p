import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.zookeeper.zkMonitor;

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

        // 1. create zk-monitor
        zkMonitor zkMonitor = new zkMonitor("127.0.0.1" , (short)2181) ;
        zkMonitor.runZkMonitor();

        NettyServer_v1 nettyServer1 = new NettyServer_v1("server1" , zkMonitor) ;
        nettyServer1.runNettyServer();

        NettyServer_v1 nettyServer2 = new NettyServer_v1("server2" , zkMonitor) ;
        nettyServer2.runNettyServer();


    }
}
