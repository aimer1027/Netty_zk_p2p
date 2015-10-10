package org.kylin.zhang.message;

import org.kylin.zhang.util.MsgPacker;

import java.util.Arrays;

/**
 * Created by win-7 on 2015/9/20.
 *
 * �� Message �е� ServerName ��Ա����ɾ��
 *
 * �����Ҫ��֪ Message ������ ��������˭�Ļ���
 *
 * �Ὣ�����ߵ���Ϣ ServerInfo �в���Ϊ��Ϣ�����ݲ��ֽ��з���
 */
public class Message {
    private static short maxLen = (short)32750 ;
     // ���ǵ���̬���������л���ʱ����ռ�ռ䣬 �� json �������ԣ���Ҫ����һ���������������ֵ
    private byte type = MessageType.UNKNOWN_MSG;
   // private String serverName = null;
    private byte[] body = null;

    // -------------- ��ȡ��Ϣ��ǰ���ڵ��ֽڸ���
    public short getRemainedBytes(){
        short remained =0 ;// ������ 3 short ��2 byte , byte : 1 byte

        remained += Message.getSerialized(this).length ;

        return (short)( maxLen - remained) ;
    }

    // ---------- constructors ---------

    public Message() {

        body = null ;
    }

    public Message(byte type) {
        body = null ;

        this.type = type;
    }

    public Message(byte[] body,   byte type) {
        this.body = body;

        this.type = type;
    }

    //--------------- getter and setter

    public byte [] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

/*    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }*/

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public Object getObjectBody(Class className){
        return MsgPacker.UnPacker(this.body, className) ;
    }

    public void setObjectBody ( Object obj, Class className)
    {
        setBody(MsgPacker.Packer(obj, className)) ;
    }


    //----------- serialized & deserialized
    public static byte [] getSerialized(Message msg ){
        return MsgPacker.Packer(msg, Message.class) ;
    }

    public static Message getDeSerialized(byte [] bytes){

        return (Message)MsgPacker.UnPacker(bytes, Message.class) ;
    }

    //----------- toString


    @Override
    public String toString() {
        return "Message{" +
                "body=" + Arrays.toString(body) +
                ", type=" + type +
                '}';
    }
}
