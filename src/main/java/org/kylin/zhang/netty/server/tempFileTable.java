package org.kylin.zhang.netty.server;

import org.kylin.zhang.beans.FileData;
import org.kylin.zhang.beans.FileInfo;
import org.kylin.zhang.beans.ServerInClusterInfo;
import org.kylin.zhang.util.JsonPacker;
import org.kylin.zhang.util.SortByTimer;

import java.util.*;

/**
 * Created by win-7 on 2015/10/6.
 *
 * 额， 就像说的那样， 如果将这么多的方法都添加到 NettyServer 上面的话，
 * 会让 server 显得特别的臃肿， 所以我将存放 用来缓冲存放接收的 Message 中的文件
 * 封装成一个类方法， 然后对外暴露它的接口
 */
public class tempFileTable {

    private Map<String ,FileInfo> recvFileList ;// 这个是用来存放最近接收到的文件缓存队列的
    // String:发送者-ServerName+接收到的文件名称


    public tempFileTable (){
        recvFileList = new Hashtable<String, FileInfo>() ;
    }



    // ---------------  封装的 recvFileList DAO 方法

    // 下面的这个方法，被用在 netty server 在接收到包含 FileData 为数据段的 Message 之后
    // 将 Message 中的数据段信息 FileData 提取出来之后，用来更新
    synchronized public void addNewFileInfo ( FileData fileData){
        int localFileLen = 0 ;
        String key = fileData.getSenderName()+fileData.getFileName() ;

        // 0 , 最先查找一下在 recvFileList 中是否存放了以前存放的文件块的信息
        if( recvFileList.containsKey( key )){
            // 如果存放了的话， 那么就获取已经接收到文件大小的数值出来

//            System.out.println( " new file data content length " + fileData.getFileLenght() ) ;

            localFileLen = recvFileList.get(fileData.getSenderName()+fileData.getFileName()).getReceivedTotalLength() ;
 //           System.out.println(" original file data content length " +localFileLen ) ;
            // 然后，将这个元素进行删除，因为，接下来要对其中的数值进行更新/插入操作
        }

        localFileLen += fileData.getFileLenght() ;
    //    System.out.println(" final length " + localFileLen)  ;
        // 1. 首先将 FileData 中的数据提取出来，创建成 FileInfo 对象
        FileInfo info = new FileInfo(fileData.getFileName(),fileData.getFileTotalLen(),fileData.getSendTimer(),localFileLen ,fileData.getSenderName()) ;


    //    System.out.println("file total length " + info.getFileTotalLength()) ;
        this.recvFileList.put(key , info) ;
    }

    // 下面的这个方法用来将 接收完整的文件 从缓冲队列中进行移除，判断移除条件是；文件当前的长度 = 文件的总长度
    // 同时必须满足的另外一个条件就是， 当前 list 中包含的元素个数 > 10 个
    synchronized public void resizeTempFileList (){
        for(String key : recvFileList.keySet()){
            FileInfo info = recvFileList.get(key) ;

            if( info.getReceivedTotalLength() == info.getFileTotalLength() && recvFileList.size() > 10 ){
                System.out.println( "file receiving finished , removed temp info from list ") ;
                recvFileList.remove( key ) ;
            }
        }
    }

    // ----------------------------------------------
    // 下面的这个方法专门用在 netty-server 要把当前 recvFileList 中最新的 10 个文件缓存消息上传到 zookeeper-server 上面
    // 在这个方法中， 将 recvFileList 中的 value 全部提取出来之后， 按照时间进行 新--> 旧 进行排序，将 top10 取出
    // 并组装成 ServerInClusterInfo 的格式， 存放到一个 List 中
    public List<String> getLastServerClusterInfo(){

        // 1. 首先将 recvFileList 中的 value 全部提取到一个 List<ServerClusterInfo> 中

        ArrayList<ServerInClusterInfo> clusterInfos = new ArrayList<ServerInClusterInfo>();
        List<String> jsonStringList = new ArrayList<String>() ;

        long currentTimer =   new Date().getTime() ;

        for(String key : recvFileList.keySet()){

            FileInfo info = recvFileList.get(key) ;


        //    System.out.println(" in tempFileTable 's getLastServerClusterInfo ") ;

      //      System.out.println( "current received fiel length " + info.getReceivedTotalLength() ) ;
       //     System.out.println(" file total length " + info.getFileTotalLength()) ;

            double process = (1.0* info.getReceivedTotalLength())/( info.getFileTotalLength())*100 ;

       //     System.out.println("process "+ process*100.0) ;

            ServerInClusterInfo serverInClusterInfo = new ServerInClusterInfo( info.getFileName() , info.getFileTotalLength() , process ,( currentTimer - info.getLastUpdateTimer()) , info.getSenderName() ) ;

            clusterInfos.add(serverInClusterInfo) ;
        }


        // 2. 然后将这个 List 按照时间新->旧进行排序
        // 时间差值越小
        Collections.sort(clusterInfos, new SortByTimer());

        // 3. 获取排序之后的 top 10 如果队列中的元素个数 < 10 则全部取出
        // 然后转换成返回 json 字符串

        for( int i  = 0 ; i < (clusterInfos.size() > 10 ? 10 : clusterInfos.size()) ; i++ ){
            String jsonString = JsonPacker.getJsonString(clusterInfos.get(i)) ;
            jsonStringList.add(jsonString) ;
        }

        return jsonStringList ;
    }



}
