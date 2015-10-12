import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.zookeeper.zkMonitor;

/**
 * Created by win-7 on 2015/10/10.
 *
 * 该类的 main 方法是整个项目的入口函数
 * 将会 启动 zkMonitor
 * 和依次启动 5 个 NettyServer
 *
 *  并不接收参数， zookeeper 以及 各个 NettyServer 的配置参数在
 *  zk-conf.xml 文件中进行设置
 *
 *  生成的 jar 文件也是以该 main 函数作为入口函数的
 *  另有一个 ZkCmdToolStartUp 作为集群状态信息展示 客户端
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
