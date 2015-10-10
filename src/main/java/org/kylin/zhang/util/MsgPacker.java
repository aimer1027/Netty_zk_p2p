package org.kylin.zhang.util;

import org.msgpack.MessagePack;

/**
 * Created by win-7 on 2015/9/15.
 *
 *���������� �����ṩ������̬����
 * 1. Packer : ���ڽ�����ʵ��ת���� ��������������
 * 2. UnPacker : ���ڽ�������������� ��ԭ�� Object ����ʵ����Ҫǿ��ת����������ת���ɹ̶�����
 *
 * ps: ʹ�ø÷����������л�/�����л������Ķ���ʵ����Ҫ
 *     ����������ʱ�����ע�� '@org.msgpack.annotation.Message'
 */
public class MsgPacker {

        public static byte [] Packer ( Object obj, Class className  ) {
            try {
                MessagePack packer = new MessagePack();
                packer.register(className);
                return packer.write(obj);

            }catch (Exception e){
                throw new RuntimeException("[error MsgPacker| Packer method]") ;
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
