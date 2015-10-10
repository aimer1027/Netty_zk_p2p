package org.kylin.zhang.message;


import org.kylin.zhang.beans.FileData;
import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.util.MsgPacker;

/**
 * Created by win-7 on 2015/9/20.
 */
public class MessageBuilder {



    public static Message getInstance(byte [] data , byte type ){
        return new Message( data , type) ;
    }


    // ����� zk --> server ���͵���Ϣ�Ļ��� ���ݶ��е�����һ���� ServerInfo ��
    public static Message getServerInfoDataInstance ( byte type , ServerInfo info  ){
        byte [] data = MsgPacker.Packer(info, ServerInfo.class) ;

        return new Message(data , type ) ;
    }

    // ����� FileData�� Ҳ���Ƿ��͵���Ϣ�� �ļ����ݵĻ�
    public static Message getFileDataInstance( FileData fileData ){
        // 1. ��������ֶ��� FileData �Ļ��� ��Ϣ������һ���� SENDING_FILE
        // �ֽ� fileData �������л�
        byte[] data = MsgPacker.Packer(fileData , FileData.class) ;

        // 2. Ȼ����װ�� Message
        return new Message(data , MessageType.SENDING_FILE) ;
    }



  /*  // 2. �����������͵���Ϣ���� �� ����Ҫ���� ��Ϣ���� ����Ϣ���ͷ��� ���������Ƽ���
    public static Message getCmdMessage(String serverName ,byte type){
        Message msg = new Message(serverName, type) ;
        return msg ;
    }


    // 3. �����������͵���Ϣ���� ��Ҫ���� ��Ϣ���� ��Ϣ���ͷ� ����������
    // �� ��Ϣ�� ����ʵ��
    public static Message getDataMessage ( String serverName , byte type , Object obj ,Class className ){
        Message msg = new Message(serverName, type ) ;

        // ���� �� Object ��obj ת���� className ��Ӧ�� ����ʵ�壬 �ȵȣ���������Ҫ��һ��ʵ��:
        // ������� ServerInfo ���뵽�����У� ���ᱻ������ Object ���͵ģ�
        // �ڱ������� Object ���͵�ʱ�� JsonPacker ����� Json �ַ����Ļ��� ��ʲô���ӵ��أ�
        // ����� JsonString ---�� Object---> ǿ��ת��Ϊ ServerInfo �Ļ��� �Ƿ�����أ�


        *//**
         * ������ͨ�� v3_msg_tester �еķ����õ���֤:
         * 1. �� ServerInfo ---> ����ת�ͳ� Object ֮�� ---> json �� Object �� String
         *    �ǿ����������� ServerInfo �е������ֶεģ�
         *
         * 2. �� Object ת�ͳɵ� String ���� Object.class ת���� �� Object ���� �޷�ǿ��ת���� ServerInfo
         *
         * 3. ���ǣ� �ڽ� Object ת�ͳɵ� json String ---> Object ����Ĺ����д���� .class ����
         *     ����� ServerInfo.class �õ��� Object �ǿ���ǿ��ת���� ServerInfo �����
         *
         *     ��ô˵������ Message �е� Object getBody(Class ) �����ƾͿ�����
         *     Message �б������� json ���Ķ��� String ����
         *     ͨ�� JsonPacker ���� getJsonObject ���� Class �Ϳ��Ի�� ��Ӧ�ض�����(����ȷʵ Object��ʽ��)
         *     ������
         * *//*

        // ����ͨ�� Message �е� String getBody( Object  ) ����ȡ�������Ϣ��� Object �����л��� jsonString
        // Ȼ����ͨ�� Message �е� void setBody( String str) �����ոմ����õ� jsonString ��������

       msg.setObjectBody(obj, className);
        return msg ;
    }

    public static Message getSendFileDataMessage ( String serverName  ){
        Message msg = new Message(serverName,MessageType.SENDING_FILE) ;
        return msg ;
    }

    public static Message getZKDataMessage( ServerInfo dataContent , byte type ){
        Message msg = new Message("zookeeper" , type) ;

        // ����Ҫ���͵� ����������Ϣ�� �������л�֮��Ȼ����з�װ
        msg.setObjectBody(dataContent , ServerInfo.class);
        return msg ;
    }
*/

}
