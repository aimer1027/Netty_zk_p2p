package org.kylin.zhang.util;

import org.kylin.zhang.beans.ServerInfo;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by win-7 on 2015/9/29.
 *
 * ����ཫ��ʵ�����µĹ���
 *
 * 1. �޸������ļ��������޸ĵ�ѡ��ͬ�����ļ���
 * 2. �������ļ��Ӷ�Ӧ��·�������ȡ���������������ļ���
 * 3. ���������ļ�
 * 4. ɾ�������ļ�
 * 5. �������ļ�������µ� key-value ��
 *
 * ����������ʱ����Ҫ�����������ƣ� ��Ϊ��ȡ�������ļ����Ʊ���
 *
 * server-name.properties
 *
 * ��������ļ���·�����ǵ�ǰ������/��Ŀ ����� conf �ļ�����
 *
 */
public class PropsConf {

    private final String ServerName   ;
    private Properties properties = null ;


    public PropsConf(String serverName){
        this.ServerName = serverName ;
        this.properties = new Properties() ;

        // Ȼ�󣬼��ض�Ӧ�� ���������Ƶ������ļ�
        InputStream in = null ;
        try {
            // ��������ļ��Ǳ�Ȼ���ڵģ� ��Ϊ����Ҫ�ڳ�������֮ǰ�������ֶ�����
            // �����������ļ��Ƿ���ڣ����Ǽ������˵��...
            File file = new File("conf/" + ServerName + ".properties") ;

          //  System.out.println(file.getAbsolutePath()) ;

            if(!file.exists() || file.isDirectory()){
                System.out.println("properties file does not exists ") ;
                System.exit(-9);
            }

            in = new BufferedInputStream(new FileInputStream(file));

            this.properties.load(in); // Ӧ��������鿴һ�£� ��Ӧ������ֵ

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void addNewKVPair(String key , String value){

        // 1. �����Ȳ��ң��Ƿ��ܹ��ҵ� ������ܹ��ҵ�����ôʹ�� update
        if(this.properties.keySet().contains(key)){
           updateKey(key,value);
        }

        // 2. ����Ҳ�����Ӧ�� key �� properties �У� ��ֱ�Ӳ���
        else{
            this.properties.put(key,value) ;
        }
        // 3. �� ���� properties ������Ϣͬ���� �ļ���

        String comment = "key : " + key +" value : " + value ;
        writeBackToFile( comment );
    }

    public void updateKey(String key , String value ){
        // ���ȣ�������һ�£��Ƿ��ж�Ӧ�� key ��Ӧ�����û��
        // �׳�����ʱ�쳣��Ϣ��Ȼ���˳�
        if(!this.properties.keySet().contains(key)){
            throw new RuntimeException("no key " + key + " in properties :error !!") ;
        }

        // ������У���ô�͵��� replace �ķ������滻������һ��
        properties.replace(key , value) ;

        // �����������ﻹԶԶû�н���

        // ����Ҫ�����ڴ��е�����������д��д�뵽�����ļ��У�
        // Ȼ�����µ�����ļ����ڴ��е����ö����вſ���

            String comment = "key : " + key +" value : " + value ;
            writeBackToFile( comment );
    }

    public void writeBackToFile ( String comment ){
        // ����ǰ  properties �����Ա�����е���ֵ��ͬ�����ոռ��ص��ļ���ȥ

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
            // ��������ļ��Ǳ�Ȼ���ڵģ� ��Ϊ����Ҫ�ڳ�������֮ǰ�������ֶ�����
            // �����������ļ��Ƿ���ڣ����Ǽ������˵��...
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
     * ������������ר���ڲ�����ʹ�ã�Ŀ����Ϊ�˴�ӡ��
     * ���� properties �ļ��еļ�ֵ�Ե���Ϣ
     * */
    public void printAllProps(){
        Iterator<Object> iter = properties.keySet().iterator() ;

        while (iter.hasNext()){
            String key = (String)iter.next() ;
            String value = (String)properties.get(key) ;
            System.out.println("key----> "+ key + " value---> " + value) ;
        }

    }

    //---------------- �������������ǣ� ͨ����ȡ�����ļ��еĲ�����
    // ---------------- ֱ�Ӵ���һ�� ServerInfo ʵ�����Ķ��󣬲����صĲ���
    public ServerInfo getServerInfo (){
        ServerInfo info = new ServerInfo ( (String)this.properties.get("serverName") , (String)properties.get("ip") ,
               Short.parseShort(properties.getProperty("port")) ) ;

        return info ;
    }

    // -------------- ����������������ͨ����������µ� ServerInfo ���������¶�Ӧ�������ļ���
    public void updateByServerInfo (ServerInfo lastInfo ){

        String ip = lastInfo.getIp() ;
        String serverName = lastInfo.getServerName() ;
        short port = lastInfo.getPort() ;

        properties.replace("ip" , ip) ;
        properties.replace("port" , String.valueOf(port)) ;
        properties.replace("serverName", serverName) ;

// ���±���/�ڴ��е���ֵ֮�� ��Ҫ���˽�����ͬ�����ļ���/Ӳ����
        this.writeBackToFile("update by last ServerInfo instance");
    }
}
