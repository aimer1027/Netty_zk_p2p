package org.kylin.zhang.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Created by win-7 on 2015/9/29.
 *
 * 在data目录下生成100个随机文本文件，内容长度随机控制在10K到10M，
 * 内容最后都为You are Best! 这些是待发送文件。
 *
 * 这个类中的方法 继承了 Runnable 这个接口， 只要是调用其中的 run 方法
 * 便会在指定的路径下面生成 100 个文件，对于大小位于 10K - 10M 之间
 *
 * 1. 生成随机数的方法 10KB= 10*2^10 B (10240) byte
 *    10MB = 10 * 2^20 = 10 * 1024*1024  = 1 048 576 0 byte
 *
 * 2. 生成文件的方法，通过接口来指定文件的长度
 *
 * run ： 运行生成文件的循环 100 次
 *
 * 下面的这个类的使用方法， 在使用的时候，创建一个该对象的实例，
 * 在构造方法中传入当前服务器的 name ，然后调用 run 方法就会在
 * 当前的路径下面创建 data/服务器-name/{100个文件，文件大小 [10K,10M] 之间，同时结尾使用“you are the best!"
 * 作为结尾}
 */
public class FileUtil implements Runnable {

    private final String endLine = "You are the best!" ;
    private final int max =  10485760 ;
    private final int min = 10240 ;
    private final String ServerName ;

    public FileUtil(String serverName){
        this.ServerName = serverName ;

        // 在这里首先检查一下， 在 data 路径下面是否有与 serverName 同名的文件夹，如果没有
        // 就创建一个，如果有，但是是个文件， 就将这个文件删除然后创建一个
        File dir = new File ("data/"+serverName) ;

        if(!dir.exists() ){
          dir.mkdirs() ;
        }
        if(dir.exists() && !dir.isDirectory()){
            dir.delete() ;
            dir.mkdirs() ;
        }
    }

    private int getRandom(){

        Random random = new Random() ;

        return (random.nextInt(max)%(max-min+1) + min) ;
    }

    private void generateFile( int fileNo ) {
        // 1. 获得一个随机数
        int len = getRandom();

 //       System.out.println("here we got " + len);

        // 2. 创建一个 byte [] 向其中写入 (len - endLine.getBytes().length ) 个字节大小的内容
        byte[] fileContents = new byte[len];

        // 3. 然后将结尾句，拷贝到对应的 byte[] 的结尾处
        System.arraycopy(endLine.getBytes(), 0, fileContents, len - endLine.getBytes().length, endLine.getBytes().length);

        /*byte [] anotherB= new byte [endLine.getBytes().length] ;

        System.arraycopy(fileContents, len - endLine.getBytes().length , anotherB, 0 , endLine.getBytes().length);

       System.out.println(new String(anotherB)) ;*/

        try {
            // 4. 创建文件，文件名称与路径为 : data/serverName/no_(数值).txt
            File file = new File("data/" + ServerName + "/no_" + fileNo + ".txt");


            // 5. 无论存在与否，都重新创建文件，这个是生成文件环节，与接收文件追加环节势必不同
            if(file.exists()) file.delete() ;

            file.createNewFile();

       //     System.out.println("file path "+ file.getAbsolutePath()) ;

             // 6. 然后将字节中的数值写入到文件中去
            BufferedOutputStream bos  = new BufferedOutputStream( new FileOutputStream(file) ) ;

            bos.write(fileContents);

            bos.flush();

            bos.close();

        }catch (Exception e ){
            e.printStackTrace();
        }
    }
    public void run() {
        for( int i = 0 ; i  <100; i++ ){
            this.generateFile(i);
        }

    }


    // ------------------ getter and setters

    public String getEndLine() {
        return endLine;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public String getServerName() {
        return ServerName;
    }
}
