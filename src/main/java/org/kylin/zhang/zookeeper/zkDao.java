package org.kylin.zhang.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by win-7 on 2015/10/7.
 */
public class zkDao {

    private String zkIP ;
    private short zkPort ;
    private CuratorFramework zkClientHandler ;

    private boolean isConnect = false ;

    public zkDao(){}

    public zkDao(String zkIP , short zkPort ){
        this.zkIP = zkIP ;
        this.zkPort = zkPort;
    }

    public void connectToServer(){
        this.zkClientHandler =
                CuratorFrameworkFactory.builder().connectString(zkIP+":"+zkPort)
                        .sessionTimeoutMs(30000)
                        .connectionTimeoutMs(30000)
                        .canBeReadOnly(false)
                        .retryPolicy( new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
                        .defaultData(null).build() ;

        // create connection to the zk server
        zkClientHandler.start();

        if(zkClientHandler != null ) isConnect= true ;
    }

    public void closeConnect(){
        this.zkClientHandler.close();
    }

    // 向服务器上面添加路径

    public void addPath( String pathName , byte []  upLoadData ){
        if( !isConnect ){
            // 如果连接状态是断开的话
            // 重新调用连接方法进行远程连接

            connectToServer();
        }

        try {
            // 在添加路径之前，首先检查一下路径是否存在
            zkClientHandler.create()
                    .creatingParentsIfNeeded()
                    .forPath(pathName , upLoadData) ;


        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("failed add path to " + pathName +" in zkDao class") ;
        }

    }

    // 将路径和数据从远程 zk-server 上面进行移除
    public void deletePath(String pathName ){
        if(!isConnect){
            connectToServer();
        }

        try{
        if( zkClientHandler.checkExists().forPath(pathName) == null){
             System.out.println("path " + pathName +" does not exists , failed to delete path ") ;
            return ;
        }

            zkClientHandler.delete().deletingChildrenIfNeeded().forPath(pathName) ;

        } catch(Exception e){
            e.printStackTrace();

            throw new RuntimeException("failed delete path on "+ pathName +" in zkDao class") ;
        }
    }


    // 下面的方法用于更新 zk-server 上面对应路径上保存的数据
    public void updateData(String pathName , byte [] upLoadData){

        if( !isConnect){
            connectToServer();
        }

        try{

            if( zkClientHandler.checkExists().forPath(pathName) == null){
                System.out.println("path " + pathName+  " does not exists , failed to update data on path") ;
                return ;
            }

            zkClientHandler.setData().forPath(pathName, upLoadData) ;

            /*
            deletePath(pathName);
            addPath(pathName, upLoadData);*/

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("failed update path data on "+ pathName+" in zkDao class") ;
        }
    }

    // 查找对应路径上面的数据信息
   public byte [] getDataByPath(String pathName ){

       byte [] data ;

       if(isConnect){
           connectToServer();
       }

       try {
           // 1. 检查路径是否存在
           if (zkClientHandler.checkExists().forPath(pathName) == null) {
               System.out.println("path " + pathName+  " does not exists , failed to get data on path") ;
               return null ;
           }

            data  = zkClientHandler.getData().forPath(pathName) ;

       } catch (Exception e){
           e.printStackTrace();
           throw new RuntimeException("failed to get data on path " + pathName +" in zkDao class") ;
       }

       return data ;
   }


    //----------------------------------------

    //------------ getter and setter ----------

    public boolean isConnect() {
        return isConnect;
    }

    public void setIsConnect(boolean isConnect) {
        this.isConnect = isConnect;
    }

    public CuratorFramework getZkClientHandler() {
        return zkClientHandler;
    }

    public void setZkClientHandler(CuratorFramework zkClientHandler) {
        this.zkClientHandler = zkClientHandler;
    }

    public String getZkIP() {
        return zkIP;
    }

    public void setZkIP(String zkIP) {
        this.zkIP = zkIP;
    }

    public short getZkPort() {
        return zkPort;
    }

    public void setZkPort(short zkPort) {
        this.zkPort = zkPort;
    }
}
