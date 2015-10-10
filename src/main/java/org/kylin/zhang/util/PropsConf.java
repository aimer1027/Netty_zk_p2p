package org.kylin.zhang.util;

import org.kylin.zhang.beans.ServerInfo;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by win-7 on 2015/9/29.
 *
 * 这个类将会实现以下的功能
 *
 * 1. 修改配置文件，并将修改的选项同步到文件中
 * 2. 将配置文件从对应的路径下面读取出来（加载配置文件）
 * 3. 创建配置文件
 * 4. 删除配置文件
 * 5. 向配置文件中添加新的 key-value 对
 *
 * 创建方法的时候，需要服务器的名称， 因为获取的配置文件名称便是
 *
 * server-name.properties
 *
 * 存放配置文件的路径便是当前工作区/项目 下面的 conf 文件夹中
 *
 */
public class PropsConf {

    private final String ServerName   ;
    private Properties properties = null ;


    public PropsConf(String serverName){
        this.ServerName = serverName ;
        this.properties = new Properties() ;

        // 然后，加载对应该 服务器名称的配置文件
        InputStream in = null ;
        try {
            // 这个配置文件是必然存在的， 因为必须要在程序运行之前，由我手动创建
            // 所以无需检查文件是否存在，还是检查了再说吧...
            File file = new File("conf/" + ServerName + ".properties") ;

          //  System.out.println(file.getAbsolutePath()) ;

            if(!file.exists() || file.isDirectory()){
                System.out.println("properties file does not exists ") ;
                System.exit(-9);
            }

            in = new BufferedInputStream(new FileInputStream(file));

            this.properties.load(in); // 应该在这里查看一下， 对应的数据值

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void addNewKVPair(String key , String value){

        // 1. 首先先查找，是否能够找到 ，如果能够找到，那么使用 update
        if(this.properties.keySet().contains(key)){
           updateKey(key,value);
        }

        // 2. 如果找不到对应的 key 在 properties 中， 就直接插入
        else{
            this.properties.put(key,value) ;
        }
        // 3. 将 本地 properties 变量信息同步到 文件中

        String comment = "key : " + key +" value : " + value ;
        writeBackToFile( comment );
    }

    public void updateKey(String key , String value ){
        // 首先，先来找一下，是否有对应的 key 对应，如果没有
        // 抛出运行时异常信息，然后退出
        if(!this.properties.keySet().contains(key)){
            throw new RuntimeException("no key " + key + " in properties :error !!") ;
        }

        // 如果，有，那么就调用 replace 的方法来替换、更新一下
        properties.replace(key , value) ;

        // 不过，到这里还远远没有结束

        // 还需要，将内存中的配置内容重写的写入到配置文件中，
        // 然后重新导入该文件到内存中的配置对象中才可以

            String comment = "key : " + key +" value : " + value ;
            writeBackToFile( comment );
    }

    public void writeBackToFile ( String comment ){
        // 将当前  properties 这个成员方法中的数值，同步到刚刚加载的文件中去

        try {
            File file = new File("conf/" + ServerName + ".properties");
            FileOutputStream outputStream = new FileOutputStream(file);

            properties.store(outputStream , comment);

     //       System.out.println("here we execute write back to file") ;
            outputStream.flush();
            outputStream.close();

        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("failed to update properties info to properties file ") ;
        }

    }

    public void reLoadProps(){
        InputStream in = null ;
        try {
            // 这个配置文件是必然存在的， 因为必须要在程序运行之前，由我手动创建
            // 所以无需检查文件是否存在，还是检查了再说吧...
            File file = new File("conf/" + ServerName + ".properties") ;

            //  System.out.println(file.getAbsolutePath()) ;

            if(!file.exists() || file.isDirectory()){
                System.out.println("properties file does not exists ") ;
                System.exit(-9);
            }

            in = new BufferedInputStream(new FileInputStream(file));

            this.properties.load(in);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 下面的这个方法专门在测试中使用，目的是为了打印出
     * 所有 properties 文件中的键值对的信息
     * */
    public void printAllProps(){
        Iterator<Object> iter = properties.keySet().iterator() ;

        while (iter.hasNext()){
            String key = (String)iter.next() ;
            String value = (String)properties.get(key) ;
            System.out.println("key----> "+ key + " value---> " + value) ;
        }

    }

    //---------------- 下面的这个方法是， 通过读取配置文件中的参数来
    // ---------------- 直接创建一个 ServerInfo 实例化的对象，并返回的操作
    public ServerInfo getServerInfo (){
        ServerInfo info = new ServerInfo ( (String)this.properties.get("serverName") , (String)properties.get("ip") ,
               Short.parseShort(properties.getProperty("port")) ) ;

        return info ;
    }

    // -------------- 下面的这个方法，是通过传入的最新的 ServerInfo 对象来更新对应的配置文件的
    public void updateByServerInfo (ServerInfo lastInfo ){

        String ip = lastInfo.getIp() ;
        String serverName = lastInfo.getServerName() ;
        short port = lastInfo.getPort() ;

        properties.replace("ip" , ip) ;
        properties.replace("port" , String.valueOf(port)) ;
        properties.replace("serverName", serverName) ;

// 更新本地/内存中的数值之后， 不要忘了将更新同步到文件中/硬盘上
        this.writeBackToFile("update by last ServerInfo instance");
    }
}
