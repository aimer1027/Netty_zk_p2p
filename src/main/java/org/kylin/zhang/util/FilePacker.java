package org.kylin.zhang.util;

import org.kylin.zhang.beans.FileData;
import org.kylin.zhang.message.Message;
import org.kylin.zhang.message.MessageBuilder;
import org.kylin.zhang.message.MessageType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by win-7 on 2015/10/6.
 *
 * ��������ṩ�ľ�̬����
 *
 * public static  List<Message> getMessageListFromFile(String filePath ) ;
 *
 * ��������һ�� �ļ� ת����һ���� ��� Message  Message �б��/list
 *
 */

/**
 * ˵���� ���� message �п���Я�����������ݴ�С��Ҳ����һ�� message �������Է�װ���ٸ��ֽ� �����ļ��е�����
 *  Message --->
 *  ���ͣ� 1 ���ֽ�
 *  ����: 2 ���ֽ�
 *          �������ݾ��� byte [] data
 *
 *  ���ڷ��� �ļ����ݵ� ��Ϣ��װ����
 *
 *  1. ������ Message ���󣬴����� FileData <�� FileData �еĳ�Ա�������� byte [] ��Ա�������������ݾ���ʼ����>
 *  2. Ȼ����� Message �Ľ��ж������л��ķ��������� FileData ���� Message �����У�
 *  3. ͨ�� Message �е� getRemainBytes �ķ�������ȡ ���е��ֽ���Ŀ remainedBytes
 *  4. ���ļ��ж�ȡ remainedBytes �� byte[] data ���ݣ� ����д�뵽 FileData ������
 *  5. Ȼ������ͨ�� Message ���л���ֵ�ķ��������� FileData �������·��õ� Message ��
 *  ����һ�� Message �ͷ�װ���ˣ� ������Ҫ�����Ĳ���һ�£� ���Է���
 *
 *
 *  6. ������ѭ���Ƿ�����ı��ǣ����ǣ��ļ��Ƿ���û�ж�ȡ���ֽ�
 *
 *   FileDataMessagePacker_Tester
 * */
public class FilePacker {


    public static List<Message> getMessageListForTest ( String fileName , String senderName ){
        String filePath ="data/"+senderName+'/'+fileName ;

        List<Message> messageList = new ArrayList<Message>() ;

        BufferedInputStream bis ;

        File file =  new File ( filePath ) ;

        if( !file.exists()){
            // �ļ������ڣ�û�����ˣ� �׳��쳣
            throw new RuntimeException("can not find file "+ filePath +" does exists") ;
        }

        try {

            // ���Ȼ�ø��ļ������������
            bis = new BufferedInputStream( new FileInputStream( file)) ;

            // Ȼ����뵽ѭ����

            do{

                // ���ȴ���һ�� FileData ����
                // (byte[] dataContent, int fileLenght, String fileName, int fileTotalLen, String senderName, int sendTimer)
                FileData fileData = new FileData( null , 0 , fileName ,(int)file.length() , senderName , new Date().getTime()) ;

                // Ȼ����� FileData ����������л�
                byte tempData [] = MsgPacker.Packer(fileData , FileData.class) ;

                // Ȼ�󴴽� Message ���� ���л��ŵ� FileData ���뵽 Message ��
                Message message = MessageBuilder.getInstance( tempData , MessageType.SENDING_FILE) ;


                //   Ȼ�󣬵��� Message �ķ�����ȡʣ��� ���е��ֽڸ���
                short remainByte = message.getRemainedBytes() ;


                // ���������Ӵ򿪵��ļ��ж����� ��Ӧ�Ŀ����ֽڸ���<������ط���Ҫ��һ�����ԣ�
                //  �����ǰ�ļ����ֽڸ����� 100 ���� �Ҷ�ȡ 108 ���Ļ����Ƿ�ᱨ�����Ƕ����ļ���β �ͻ����>

                // ���������һ��С�ж�: �жϵ�ǰ�ļ��пɶ�ȡ���ֽڵĸ��� �� Message ��ʣ��ռ�ĸ��� �Ǹ�С
                // ȥ��С����ֵ������ byte[] �� Ȼ����ļ��ж�ȡ��Ӧ���ֽڸ�������������� �ֽ�������

                byte [] fileBytes = new byte[(remainByte > bis.available()?bis.available():remainByte)] ;


                // ����ȡ������ �ֽ�д�뵽 FileData ��ȥ�� Ȼ���������л��� �����л�֮�������д�뵽 Message ��
                bis.read( fileBytes) ;
                fileData.setDataContent(fileBytes); // ���ֽ����ݷ��õ� FileData ����ʵ����
                fileData.setFileLenght( fileBytes.length); // �����˸����ļ����д�ŵ��ļ����ݵ��ֽڸ���


                // һ�� Message ��������ˣ� ����� Message �ŵ� messageList �м���
                // Message �е� setObjectBody ������������л���������Ӧ����� fileData ����������л�
                message.setObjectBody(fileData, FileData.class);

                messageList.add(message) ;

            } while ( bis.available()  !=0   ) ;
            // ���� �ļ���β֮���Զ����˳�ѭ������

        } catch (Exception e ){
            e.printStackTrace();
        }


        return messageList ;

    }

    public static List<Message> getMessageListFromFile (String fileName , String senderName ){

        String filePath = "data/"+senderName+"/no_"+fileName ;

       List<Message> messageList = new ArrayList<Message>() ;

        BufferedInputStream bis ;

        File file =  new File ( filePath ) ;

        if( !file.exists()){
            // �ļ������ڣ�û�����ˣ� �׳��쳣
            throw new RuntimeException("can not find file "+ filePath +" does exists") ;
        }

        try {

            // ���Ȼ�ø��ļ������������
            bis = new BufferedInputStream( new FileInputStream( file)) ;

            // Ȼ����뵽ѭ����

           //do
            while (bis.available() != 0 ){

               // ���ȴ���һ�� FileData ����
                // (byte[] dataContent, int fileLenght, String fileName, int fileTotalLen, String senderName, int sendTimer)
                FileData fileData = new FileData( null , 0 , fileName ,(int)file.length() , senderName , new Date().getTime()) ;

              // Ȼ����� FileData ����������л�
                byte tempData [] = MsgPacker.Packer(fileData , FileData.class) ;

              // Ȼ�󴴽� Message ���� ���л��ŵ� FileData ���뵽 Message ��
                Message message = MessageBuilder.getInstance( tempData , MessageType.SENDING_FILE) ;


              //   Ȼ�󣬵��� Message �ķ�����ȡʣ��� ���е��ֽڸ���
                short remainByte = message.getRemainedBytes() ;


              // ���������Ӵ򿪵��ļ��ж����� ��Ӧ�Ŀ����ֽڸ���<������ط���Ҫ��һ�����ԣ�
                //  �����ǰ�ļ����ֽڸ����� 100 ���� �Ҷ�ȡ 108 ���Ļ����Ƿ�ᱨ�����Ƕ����ļ���β �ͻ����>

                // ���������һ��С�ж�: �жϵ�ǰ�ļ��пɶ�ȡ���ֽڵĸ��� �� Message ��ʣ��ռ�ĸ��� �Ǹ�С
                // ȥ��С����ֵ������ byte[] �� Ȼ����ļ��ж�ȡ��Ӧ���ֽڸ�������������� �ֽ�������

                byte [] fileBytes = new byte[(remainByte > bis.available()?bis.available():remainByte)] ;


                // ����ȡ������ �ֽ�д�뵽 FileData ��ȥ�� Ȼ���������л��� �����л�֮�������д�뵽 Message ��
                bis.read( fileBytes) ;

                FileData fileData1 = new FileData( fileBytes , fileBytes.length , fileName ,(int)file.length() , senderName , new Date().getTime()) ;

            // һ�� Message ��������ˣ� ����� Message �ŵ� messageList �м���
                // Message �е� setObjectBody ������������л���������Ӧ����� fileData ����������л�

                byte[] msgObject = MsgPacker.Packer(fileData1 , FileData.class) ;

                message.setBody(msgObject);
              //  message.setObjectBody(fileData1, FileData.class);

                messageList.add(message) ;

                }// while ( bis.available()  !=0   ) ;
            // ���� �ļ���β֮���Զ����˳�ѭ������


    } catch (Exception e ){
        e.printStackTrace();
    }


        return messageList ;

    }

    public static FileData getFileDataObjectFromMessage ( Message message ){

        // ֻ�������� SENDING_FILE �� Message �е������ֶβ��� FileData ���͵�
        // �ж�����
        if( message.getType() != MessageType.SENDING_FILE ){
            System.out.println("message type not match can not parse message ") ;
            return null ;
        }

        // ���ͺϷ��� ���г�ȡ FileData ����
        FileData fileData = (FileData)message.getObjectBody(FileData.class) ;
        return fileData ;
    }

}
