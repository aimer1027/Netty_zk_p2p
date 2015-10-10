package com.kokia.m10.day9.nettyServerTests;

import org.junit.Test;
import org.kylin.zhang.zookeeper.zkDao;
import org.kylin.zhang.zookeeper.zkMonitor;

/**
 * Created by win-7 on 2015/10/9.
 *
 * 通过测试， 可以通过加载配置文件
 * 然后，在远程 zk-server 的上面创建对应的初始化路径， 、
 * 并且单独开辟一个线程来监听 listen path ; 监听方法已经可以正常运行
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
