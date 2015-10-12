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

 //               System.out.println("here throws an exception , check the event ") ;

                // ���Ȼ��·������
          //   System.out.println(" something happen " + event.getType()) ;



                // Ȼ��ػ��Ӧ·���������������


                // Ȼ�����ݻ�ԭ�� ServerInfo ����ʵ��


                // ��󣬸��ݲ�ͬ���¼���������ͬ�ķ�����ִ��

                switch( event.getType()){
                    case CHILD_REMOVED:{



             /*     //      System.out.println("delete a node on path "+ pathName) ;
                        ServerInfo serverInfo = (ServerInfo) JsonPacker.getJsonObject( new String (data), ServerInfo.class) ;
                        在这里使用上述的这个方法是无效的， 因为对应的监听路径，存放的原本是 ServerInfo 但是，由于本身也存放
                        对应的 server-cluster 信息，所以，早就变成了 json 状态数值了，

                        不过，通过 zk-monitor 我们可以获取对应的 register-table 的访问权限

                        同时，通过 对应发生删除路径的操作，我们可以获取路径的名称

                        */
                        String delPathName = event.getData().getPath() ;

                        // 而且 path Name 对应的是 /Aimer/listen/<对应的serverName>

                        String [] infoLine = delPathName.split("/") ;

                        System.out.println("you delete " + infoLine[3]) ;

                     //   ServerInfo serverInfo = monitor.getRegisterServerInfoTable().get(infoLine[2]) ;

                        monitor.deleteRegisterServer(infoLine[3]);
                        break ;
                    }
                    case CHILD_UPDATED:{

                  //      System.out.println("update a node's data on path " + pathName) ;

                        break ;
                    }
                    case CHILD_ADDED:{
                        String pathName = event.getData().getPath() ;
                        byte [] data = event.getData().getData() ;
                        ServerInfo serverInfo = (ServerInfo) JsonPacker.getJsonObject( new String (data), ServerInfo.class) ;
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
