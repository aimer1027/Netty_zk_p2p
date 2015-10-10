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
 * 这个类中提供的静态方法
 *
 * public static  List<Message> getMessageListFromFile(String filePath ) ;
 *
 * 是用来将一个 文件 转换成一个由 多个 Message  Message 列表的/list
 *
 */

/**
 * 说明： 对于 message 中可以携带的数据内容大小，也就是一个 message 中最多可以封装多少个字节 来自文件中的数据
 *  Message --->
 *  类型： 1 个字节
 *  长度: 2 个字节
 *          其余内容均是 byte [] data
 *
 *  对于发送 文件数据的 消息封装步骤
 *
 *  1. 创建好 Message 对象，创建好 FileData <将 FileData 中的成员方法除了 byte [] 成员变量其他的内容均初始化好>
 *  2. 然后调用 Message 的进行对象序列化的方法，来把 FileData 置于 Message 对象中，
 *  3. 通过 Message 中的 getRemainBytes 的方法来获取 空闲的字节数目 remainedBytes
 *  4. 从文件中读取 remainedBytes 的 byte[] data 内容， 将其写入到 FileData 对象中
 *  5. 然后重新通过 Message 序列化赋值的方法，来将 FileData 对象重新放置到 Message 中
 *  这样一个 Message 就封装好了， 不过需要单独的测试一下， 测试方法
 *
 *
 *  6. 而控制循环是否结束的便是，便是，文件是否还有没有读取的字节
 *
 *   FileDataMessagePacker_Tester
 * */
public class FilePacker {


    public static List<Message> getMessageListForTest ( String fileName , String senderName ){
        String filePath ="temp/"+senderName+'/'+fileName ;

        List<Message> messageList = new ArrayList<Message>() ;

        BufferedInputStream bis ;

        File file =  new File ( filePath ) ;

        if( !file.exists()){
            // 文件不存在，没法玩了， 抛出异常
            throw new RuntimeException("can not find file "+ filePath +" does exists") ;
        }

        try {

            // 首先获得该文件的输出流对象
            bis = new BufferedInputStream( new FileInputStream( file)) ;

            // 然后进入到循环中

            do{

                // 首先创建一个 FileData 对象
                // (byte[] dataContent, int fileLenght, String fileName, int fileTotalLen, String senderName, int sendTimer)
                FileData fileData = new FileData( null , 0 , fileName ,(int)file.length() , senderName , new Date().getDate()) ;

                // 然后将这个 FileData 对象进行序列化
                byte tempData [] = MsgPacker.Packer(fileData , FileData.class) ;

                // 然后创建 Message 并将 序列化号的 FileData 放入到 Message 中
                Message message = MessageBuilder.getInstance( tempData , MessageType.SENDING_FILE) ;


                //   然后，调用 Message 的方法获取剩余的 空闲的字节个数
                short remainByte = message.getRemainedBytes() ;


                // 接下来，从打开的文件中读出来 对应的空闲字节个数<在这个地方我要做一个测试：
                //  如果当前文件的字节个数有 100 个， 我读取 108 个的话，是否会报错，还是读到文件结尾 就会结束>

                // 在这里进行一个小判断: 判断当前文件中可读取的字节的个数 和 Message 中剩余空间的个数 那个小
                // 去较小的数值来创建 byte[] ， 然后从文件中读取对应个字节个数到这个创建的 字节数组中

                byte [] fileBytes = new byte[(remainByte > bis.available()?bis.available():remainByte)] ;


                // 将读取出来的 字节写入到 FileData 中去， 然后重新序列化， 将序列化之后的数据写入到 Message 中
                bis.read( fileBytes) ;
                fileData.setDataContent(fileBytes); // 将字节内容放置到 FileData 对象实例中
                fileData.setFileLenght( fileBytes.length); // 别忘了更新文件块中存放的文件数据的字节个数


                // 一个 Message 就组件好了， 将这个 Message 放到 messageList 中即可
                // Message 中的 setObjectBody 方法会调用序列化方法将对应传入的 fileData 对象进行序列化
                message.setObjectBody(fileData, FileData.class);

                messageList.add(message) ;

            } while ( bis.available()  !=0   ) ;
            // 读到 文件结尾之后，自动的退出循环即可

        } catch (Exception e ){
            e.printStackTrace();
        }


        return messageList ;

    }

    public static List<Message> getMessageListFromFile (String fileName , String senderName ){

        String filePath = "data/"+senderName+'/'+fileName ;

       List<Message> messageList = new ArrayList<Message>() ;

        BufferedInputStream bis ;

        File file =  new File ( filePath ) ;

        if( !file.exists()){
            // 文件不存在，没法玩了， 抛出异常
            throw new RuntimeException("can not find file "+ filePath +" does exists") ;
        }

        try {

            // 首先获得该文件的输出流对象
            bis = new BufferedInputStream( new FileInputStream( file)) ;

            // 然后进入到循环中

            do{

               // 首先创建一个 FileData 对象
                // (byte[] dataContent, int fileLenght, String fileName, int fileTotalLen, String senderName, int sendTimer)
                FileData fileData = new FileData( null , 0 , fileName ,(int)file.length() , senderName , new Date().getDate()) ;

              // 然后将这个 FileData 对象进行序列化
                byte tempData [] = MsgPacker.Packer(fileData , FileData.class) ;

              // 然后创建 Message 并将 序列化号的 FileData 放入到 Message 中
                Message message = MessageBuilder.getInstance( tempData , MessageType.SENDING_FILE) ;


              //   然后，调用 Message 的方法获取剩余的 空闲的字节个数
                short remainByte = message.getRemainedBytes() ;


              // 接下来，从打开的文件中读出来 对应的空闲字节个数<在这个地方我要做一个测试：
                //  如果当前文件的字节个数有 100 个， 我读取 108 个的话，是否会报错，还是读到文件结尾 就会结束>

                // 在这里进行一个小判断: 判断当前文件中可读取的字节的个数 和 Message 中剩余空间的个数 那个小
                // 去较小的数值来创建 byte[] ， 然后从文件中读取对应个字节个数到这个创建的 字节数组中

                byte [] fileBytes = new byte[(remainByte > bis.available()?bis.available():remainByte)] ;


                // 将读取出来的 字节写入到 FileData 中去， 然后重新序列化， 将序列化之后的数据写入到 Message 中
                bis.read( fileBytes) ;
                fileData.setDataContent(fileBytes); // 将字节内容放置到 FileData 对象实例中
                fileData.setFileLenght( fileBytes.length); // 别忘了更新文件块中存放的文件数据的字节个数


            // 一个 Message 就组件好了， 将这个 Message 放到 messageList 中即可
                // Message 中的 setObjectBody 方法会调用序列化方法将对应传入的 fileData 对象进行序列化
                message.setObjectBody(fileData, FileData.class);

                messageList.add(message) ;

                } while ( bis.available()  !=0   ) ;
            // 读到 文件结尾之后，自动的退出循环即可

    } catch (Exception e ){
        e.printStackTrace();
    }


        return messageList ;

    }

    public static FileData getFileDataObjectFromMessage ( Message message ){

        // 只有类型是 SENDING_FILE 的 Message 中的数据字段才是 FileData 类型的
        // 判断类型
        if( message.getType() != MessageType.SENDING_FILE ){
            System.out.println("message type not match can not parse message ") ;
            return null ;
        }

        // 类型合法， 从中抽取 FileData 对象
        FileData fileData = (FileData)message.getObjectBody(FileData.class) ;
        return fileData ;
    }

}
