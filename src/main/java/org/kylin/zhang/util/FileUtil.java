package org.kylin.zhang.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Created by win-7 on 2015/9/29.
 *
 * ��dataĿ¼������100������ı��ļ������ݳ������������10K��10M��
 * �������ΪYou are Best! ��Щ�Ǵ������ļ���
 *
 * ������еķ��� �̳��� Runnable ����ӿڣ� ֻҪ�ǵ������е� run ����
 * �����ָ����·���������� 100 ���ļ������ڴ�Сλ�� 10K - 10M ֮��
 *
 * 1. ����������ķ��� 10KB= 10*2^10 B (10240) byte
 *    10MB = 10 * 2^20 = 10 * 1024*1024  = 1 048 576 0 byte
 *
 * 2. �����ļ��ķ�����ͨ���ӿ���ָ���ļ��ĳ���
 *
 * run �� ���������ļ���ѭ�� 100 ��
 *
 * �����������ʹ�÷����� ��ʹ�õ�ʱ�򣬴���һ���ö����ʵ����
 * �ڹ��췽���д��뵱ǰ�������� name ��Ȼ����� run �����ͻ���
 * ��ǰ��·�����洴�� data/������-name/{100���ļ����ļ���С [10K,10M] ֮�䣬ͬʱ��βʹ�á�you are the best!"
 * ��Ϊ��β}
 */
public class FileUtil implements Runnable {

    private final String endLine = "You are the best!" ;
    private final int max =  10485760 ;
    private final int min = 10240 ;
    private final String ServerName ;

    public FileUtil(String serverName){
        this.ServerName = serverName ;

        // ���������ȼ��һ�£� �� data ·�������Ƿ����� serverName ͬ�����ļ��У����û��
        // �ʹ���һ��������У������Ǹ��ļ��� �ͽ�����ļ�ɾ��Ȼ�󴴽�һ��
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
        // 1. ���һ�������
        int len = getRandom();

 //       System.out.println("here we got " + len);

        // 2. ����һ�� byte [] ������д�� (len - endLine.getBytes().length ) ���ֽڴ�С������
        byte[] fileContents = new byte[len];

        // 3. Ȼ�󽫽�β�䣬��������Ӧ�� byte[] �Ľ�β��
        System.arraycopy(endLine.getBytes(), 0, fileContents, len - endLine.getBytes().length, endLine.getBytes().length);

        /*byte [] anotherB= new byte [endLine.getBytes().length] ;

        System.arraycopy(fileContents, len - endLine.getBytes().length , anotherB, 0 , endLine.getBytes().length);

       System.out.println(new String(anotherB)) ;*/

        try {
            // 4. �����ļ����ļ�������·��Ϊ : data/serverName/no_(��ֵ).txt
            File file = new File("data/" + ServerName + "/no_" + fileNo + ".txt");


            // 5. ���۴�����񣬶����´����ļ�������������ļ����ڣ�������ļ�׷�ӻ����Ʊز�ͬ
            if(file.exists()) file.delete() ;

            file.createNewFile();

       //     System.out.println("file path "+ file.getAbsolutePath()) ;

             // 6. Ȼ���ֽ��е���ֵд�뵽�ļ���ȥ
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
