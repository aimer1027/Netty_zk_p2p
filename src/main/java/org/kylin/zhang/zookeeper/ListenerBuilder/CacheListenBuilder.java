package org.kylin.zhang.zookeeper.ListenerBuilder;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.util.JsonPacker;
import org.kylin.zhang.util.MsgPacker;
import org.kylin.zhang.zookeeper.zkMonitor;

/**
 * Created by win-7 on 2015/10/7.
 */
public class CacheListenBuilder {

    public static PathChildrenCacheListener getInstance( final zkMonitor monitor){

        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {


                // ���Ȼ��·������
                String pathName = event.getData().getPath() ;

               System.out.println(" something happen " + event.getType()) ;

                // Ȼ��ػ��Ӧ·���������������
                byte [] data = event.getData().getData() ;

                // Ȼ�����ݻ�ԭ�� ServerInfo ����ʵ��
                ServerInfo serverInfo = (ServerInfo) JsonPacker.getJsonObject( new String (data), ServerInfo.class) ;

                // ��󣬸��ݲ�ͬ���¼���������ͬ�ķ�����ִ��

                switch( event.getType()){
                    case CHILD_REMOVED:{

                  //      System.out.println("delete a node on path "+ pathName) ;

                        monitor.deleteRegisterServer(serverInfo);
                        break ;
                    }
                    case CHILD_UPDATED:{

                  //      System.out.println("update a node's data on path " + pathName) ;

                        break ;
                    }
                    case CHILD_ADDED:{

                      System.out.println("add a new node" + pathName) ;

                        // ���ڵ� add �����������ʱ��Ӧ���� netty-server ͨ�� zk-monitor �� zk-server ����ע�ᱻ�����ڵ��ʱ��

                        // ���ʱ�� �� zk-monitor Ӧ��
                        // 1. ����� serverInfo ��ӵ��Լ��� registeredServerInfoDataList ��
                        //              �������֮ǰ����Ҫ�����ж�
                        //              1.1 ��� registerServerInfoDataList ����Ϊ�յĻ��� ��Ҫ����Ƚϣ� ����һ�� ��Ϣ�����߳�ʵ��(����������һ����Ϣ)
                         //                 �� serverName С�ķ��������� serverName ��� ServerInfo
                        //                  ��������Ŀ����Ϊ���� serverName С�ķ����������� serverName ��� ������ ���������ļ�����Ϣ
                        monitor.addNewRegisterServer( serverInfo );
                        break ;
                    }
                }
            }
        } ;

        return listener ;
    }

}
