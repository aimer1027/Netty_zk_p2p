package org.kylin.zhang.message;

/**
 * Created by win-7 on 2015/9/19.
 */
public class MessageType {
   public static byte  REQ_FILE = 0x01 ;
    public static byte  READY_SEND = 0x02 ;
    public static byte   READY_RECV = 0x03 ;
    public static byte  END_SEND = 0x04 ;
    public static byte  CLOSE_CONN = 0x05 ;

    public static byte  SENDING_FILE = 0x06 ;
    public static byte UNKNOWN_MSG = 0x00 ;


    // zk---> netty server

    public static byte ZK_ONLINE = 0x20 ;
    public static byte ZK_OFFLINE = 0x21 ;



}
