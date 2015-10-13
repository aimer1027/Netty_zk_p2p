package org.kylin.zhang.util;

import org.msgpack.MessagePack;

/**
 * Created by win-7 on 2015/9/15.
 *
 *功能描述： 该类提供两个静态方法
 * 1. Packer : 用于将对象实例转换成 二进制数组类型
 * 2. UnPacker : 用于将二进制数组对象 还原成 Object 对象实例需要强制转换操作才能转换成固定类型
 *
 * ps: 使用该方法进行序列化/反序列化操作的对象实例需要
 *     在类声明的时候加上注释 '@org.msgpack.annotation.Message'
 */
public class MsgPacker {

        public static byte [] Packer ( Object obj, Class className  ) {
            try {
                MessagePack packer = new MessagePack();
                packer.register(className);
                return packer.write(obj);

            }catch (Exception e){
                e.printStackTrace();
                return null ;
       //         throw new RuntimeException("[error MsgPacker| Packer method]") ;
            }
        }
        public static Object UnPacker (byte [] bytes , Class className ) {
            try {
                if( bytes.length <=0 || bytes == null ){
                    System.out.println("[error: bytes value is null MsgPacker | UnPacker]") ;
                    return null ;
                }
                MessagePack unPacker = new MessagePack() ;

                unPacker.register(className);

                return unPacker.read(bytes , className) ;

            }catch (Exception e ){
                throw new RuntimeException("[error MsgPacker | UnPacker method]") ;
             }
        }
    }
