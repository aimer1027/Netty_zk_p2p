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
 * � ����˵�������� �������ô��ķ�������ӵ� NettyServer ����Ļ���
 * ���� server �Ե��ر��ӷ�ף� �����ҽ���� ���������Ž��յ� Message �е��ļ�
 * ��װ��һ���෽���� Ȼ����Ⱪ¶���Ľӿ�
 */
public class tempFileTable {

    private Map<String ,FileInfo> recvFileList ;// ������������������յ����ļ�������е�
    // String:������-ServerName+���յ����ļ�����


    public tempFileTable (){
        recvFileList = new Hashtable<String, FileInfo>() ;
    }



    // ---------------  ��װ�� recvFileList DAO ����

    // �������������������� netty server �ڽ��յ����� FileData Ϊ���ݶε� Message ֮��
    // �� Message �е����ݶ���Ϣ FileData ��ȡ����֮����������
    synchronized public void addNewFileInfo ( FileData fileData){
        int localFileLen = 0 ;
        String key = fileData.getSenderName()+fileData.getFileName() ;

        // 0 , ���Ȳ���һ���� recvFileList ���Ƿ�������ǰ��ŵ��ļ������Ϣ
        if( recvFileList.containsKey( key )){
            // �������˵Ļ��� ��ô�ͻ�ȡ�Ѿ����յ��ļ���С����ֵ����

//            System.out.println( " new file data content length " + fileData.getFileLenght() ) ;

            localFileLen = recvFileList.get(fileData.getSenderName()+fileData.getFileName()).getReceivedTotalLength() ;
 //           System.out.println(" original file data content length " +localFileLen ) ;
            // Ȼ�󣬽����Ԫ�ؽ���ɾ������Ϊ��������Ҫ�����е���ֵ���и���/�������
        }

        localFileLen += fileData.getFileLenght() ;
    //    System.out.println(" final length " + localFileLen)  ;
        // 1. ���Ƚ� FileData �е�������ȡ������������ FileInfo ����
        FileInfo info = new FileInfo(fileData.getFileName(),fileData.getFileTotalLen(),fileData.getSendTimer(),localFileLen ,fileData.getSenderName()) ;


    //    System.out.println("file total length " + info.getFileTotalLength()) ;
        this.recvFileList.put(key , info) ;
    }

    // ������������������ �����������ļ� �ӻ�������н����Ƴ����ж��Ƴ������ǣ��ļ���ǰ�ĳ��� = �ļ����ܳ���
    // ͬʱ�������������һ���������ǣ� ��ǰ list �а�����Ԫ�ظ��� > 10 ��
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
    // ������������ר������ netty-server Ҫ�ѵ�ǰ recvFileList �����µ� 10 ���ļ�������Ϣ�ϴ��� zookeeper-server ����
    // ����������У� �� recvFileList �е� value ȫ����ȡ����֮�� ����ʱ����� ��--> �� �������򣬽� top10 ȡ��
    // ����װ�� ServerInClusterInfo �ĸ�ʽ�� ��ŵ�һ�� List ��
    public List<String> getLastServerClusterInfo(){

        // 1. ���Ƚ� recvFileList �е� value ȫ����ȡ��һ�� List<ServerClusterInfo> ��

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


        // 2. Ȼ����� List ����ʱ����->�ɽ�������
        // ʱ���ֵԽС
        Collections.sort(clusterInfos, new SortByTimer());

        // 3. ��ȡ����֮��� top 10 ��������е�Ԫ�ظ��� < 10 ��ȫ��ȡ��
        // Ȼ��ת���ɷ��� json �ַ���

        for( int i  = 0 ; i < (clusterInfos.size() > 10 ? 10 : clusterInfos.size()) ; i++ ){
            String jsonString = JsonPacker.getJsonString(clusterInfos.get(i)) ;
            jsonStringList.add(jsonString) ;
        }

        return jsonStringList ;
    }



}
