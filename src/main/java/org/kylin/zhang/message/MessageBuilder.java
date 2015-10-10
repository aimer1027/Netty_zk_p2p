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


    // 如果是 zk --> server 发送的消息的话， 数据段中的数据一定是 ServerInfo 的
    public static Message getServerInfoDataInstance ( byte type , ServerInfo info  ){
        byte [] data = MsgPacker.Packer(info, ServerInfo.class) ;

        return new Message(data , type ) ;
    }

    // 如果是 FileData， 也就是发送的消息是 文件内容的话
    public static Message getFileDataInstance( FileData fileData ){
        // 1. 如果数据字段是 FileData 的话， 消息的类型一定是 SENDING_FILE
        // 现将 fileData 进行序列化
        byte[] data = MsgPacker.Packer(fileData , FileData.class) ;

        // 2. 然后组装成 Message
        return new Message(data , MessageType.SENDING_FILE) ;
    }



  /*  // 2. 创建命令类型的消息对象 ， 仅需要传入 消息类型 和消息发送方的 服务器名称即可
    public static Message getCmdMessage(String serverName ,byte type){
        Message msg = new Message(serverName, type) ;
        return msg ;
    }


    // 3. 创建数据类型的消息对象， 需要传入 消息类型 消息发送方 服务器名称
    // 和 消息体 对象实体
    public static Message getDataMessage ( String serverName , byte type , Object obj ,Class className ){
        Message msg = new Message(serverName, type ) ;

        // 首先 将 Object 的obj 转换成 className 对应的 对象实体， 等等，在这里需要做一个实验:
        // 如果对于 ServerInfo 传入到方法中， 它会被当做是 Object 类型的；
        // 在被当做是 Object 类型的时候被 JsonPacker 编码成 Json 字符串的话， 是什么样子的呢？
        // 将其从 JsonString ---》 Object---> 强制转型为 ServerInfo 的话， 是否可以呢？


        *//**
         * 在这里通过 v3_msg_tester 中的方法得到验证:
         * 1. 将 ServerInfo ---> 向下转型成 Object 之后 ---> json 化 Object 到 String
         *    是可以正常保存 ServerInfo 中的内容字段的；
         *
         * 2. 将 Object 转型成的 String 调用 Object.class 转换成 的 Object 对象 无法强制转换成 ServerInfo
         *
         * 3. 但是， 在将 Object 转型成的 json String ---> Object 对象的过程中传入的 .class 对象
         *     如果是 ServerInfo.class 得到的 Object 是可以强制转换成 ServerInfo 对象的
         *
         *     这么说来对于 Message 中的 Object getBody(Class ) 如此设计就可以了
         *     Message 中本身便具有 json 化的对象 String 数据
         *     通过 JsonPacker 调用 getJsonObject 传入 Class 就可以获得 对应特定类型(但是确实 Object形式的)
         *     对象了
         * *//*

        // 下面通过 Message 中的 String getBody( Object  ) 来获取传入的消息体的 Object 的序列化的 jsonString
        // 然后再通过 Message 中的 void setBody( String str) 来将刚刚创建好的 jsonString 传入其中

       msg.setObjectBody(obj, className);
        return msg ;
    }

    public static Message getSendFileDataMessage ( String serverName  ){
        Message msg = new Message(serverName,MessageType.SENDING_FILE) ;
        return msg ;
    }

    public static Message getZKDataMessage( ServerInfo dataContent , byte type ){
        Message msg = new Message("zookeeper" , type) ;

        // 将需要发送的 服务器的信息， 进行序列化之后，然后进行封装
        msg.setObjectBody(dataContent , ServerInfo.class);
        return msg ;
    }
*/

}
