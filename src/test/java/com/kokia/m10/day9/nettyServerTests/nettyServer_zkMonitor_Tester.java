package com.kokia.m10.day9.nettyServerTests;

import org.junit.Test;
import org.kylin.zhang.netty.server.NettyServer_v1;
import org.kylin.zhang.zookeeper.zkDao;
import org.kylin.zhang.zookeeper.zkMonitor;

/**
 * Created by win-7 on 2015/10/9.
 */
public class nettyServer_zkMonitor_Tester {

    public static void main (String [] args ){

        // ���� zkMonitor ����ʵ��
        zkMonitor zkMonitorServer = new zkMonitor("127.0.0.1", (short)2181) ;

        // ���÷������ڷ������˴�����Ӧ�ĳ�ʼ��·��
        zkMonitorServer.initZkServerPaths();

        // ���ü��������� ���������߳���
        zkMonitorServer.startListen();

        NettyServer_v1 nettyServer_v1 = new NettyServer_v1("server1", zkMonitorServer) ;

        nettyServer_v1.initNettyServer();

       // nettyServer_v1.runNettyListenServer( nettyServer_v1 );

        // ���� netty-server �ļ���
        nettyServer_v1.registerToZkServer();
    }

    @Test
    public void resetZkPath (){
        zkDao dao = new zkDao("127.0.0.1", (short)2181) ;

        dao.connectToServer();

        dao.deletePath("/Aimer");

        dao.closeConnect();

    }
}
