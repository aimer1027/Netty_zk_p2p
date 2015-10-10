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


                // 首先获得路径名称
                String pathName = event.getData().getPath() ;

               System.out.println(" something happen " + event.getType()) ;

                // 然后截获对应路径上面的数据内容
                byte [] data = event.getData().getData() ;

                // 然后将数据还原成 ServerInfo 对象实例
                ServerInfo serverInfo = (ServerInfo) JsonPacker.getJsonObject( new String (data), ServerInfo.class) ;

                // 最后，根据不同的事件来驱动不同的方法来执行

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

                        // 当节点 add 的情况发生的时候，应该是 netty-server 通过 zk-monitor 向 zk-server 上面注册被监听节点的时候

                        // 这个时候 ， zk-monitor 应该
                        // 1. 将这个 serverInfo 添加到自己的 registeredServerInfoDataList 中
                        //              不过添加之前还需要进行判断
                        //              1.1 如果 registerServerInfoDataList 并不为空的话， 就要逐个比较， 创建一个 消息发送线程实例(仅仅供发送一次消息)
                         //                 向 serverName 小的服务器发送 serverName 大的 ServerInfo
                        //                  这样做的目的是为了让 serverName 小的服务器主动向 serverName 大的 服务器 发送请求文件的消息
                        monitor.addNewRegisterServer( serverInfo );
                        break ;
                    }
                }
            }
        } ;

        return listener ;
    }

}
