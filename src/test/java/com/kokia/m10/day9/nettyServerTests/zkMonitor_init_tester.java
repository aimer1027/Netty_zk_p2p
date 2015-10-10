package com.kokia.m10.day9.nettyServerTests;

import org.junit.Test;
import org.kylin.zhang.zookeeper.zkDao;
import org.kylin.zhang.zookeeper.zkMonitor;

/**
 * Created by win-7 on 2015/10/9.
 *
 * ͨ�����ԣ� ����ͨ�����������ļ�
 * Ȼ����Զ�� zk-server �����洴����Ӧ�ĳ�ʼ��·���� ��
 * ���ҵ�������һ���߳������� listen path ; ���������Ѿ�������������
 *
 */
public class zkMonitor_init_tester {



    public static void main (String [] args ){
        zkMonitor zkMonitor = new zkMonitor("127.0.0.1", (short)2181) ;

        zkMonitor.initZkServerPaths();

        zkMonitor.startListen();
    }

    @Test
    public void resetZkPath (){
        zkDao dao = new zkDao("127.0.0.1", (short)2181) ;

        dao.connectToServer();

        dao.deletePath("/Aimer");

        dao.closeConnect();

    }

}
