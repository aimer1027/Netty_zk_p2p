package org.kylin.zhang.message;

import org.kylin.zhang.util.MsgPacker;

import java.util.Arrays;

/**
 * Created by win-7 on 2015/9/20.
 *
 * 将 Message 中的 ServerName 成员变量删除
 *
 * 如果需要告知 Message 接收者 发送者是谁的话，
 *
 * 会将发送者的消息 ServerInfo 中并作为消息的数据部分进行发送
 */
public class Message {
    private static short maxLen = (short)32750 ;
     // 考虑到静态变量在序列化的时候所占空间， 和 json 的冗余性，需要减少一下最大容量上限数值
    private byte type = MessageType.UNKNOWN_MSG;
   // private String serverName = null;
    private byte[] body = null;

    // -------------- 获取消息当前生于的字节个数
    public short getRemainedBytes(){
        short remained =0 ;// 基数是 3 short ：2 byte , byte : 1 byte

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
