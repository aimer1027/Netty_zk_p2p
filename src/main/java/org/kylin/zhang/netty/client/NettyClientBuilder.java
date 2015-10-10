package org.kylin.zhang.netty.client;

import org.kylin.zhang.beans.ServerInfo;
import org.kylin.zhang.message.Message;

import java.util.List;

/**
 * Created by win-7 on 2015/10/6.
 */
public class NettyClientBuilder {

    public static NettyClient getInstance ( List<Message> messageList , ServerInfo receiverInfo ){
        return new NettyClient(messageList, receiverInfo) ;
    }


}
