package org.kylin.zhang.netty.client.handlers;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.kylin.zhang.message.Message;

import java.util.List;

/**
 * Created by win-7 on 2015/10/6.
 */
public class NettyClientSenderHandler extends ChannelHandlerAdapter {

    private List<Message> messageList ;


    public NettyClientSenderHandler(List<Message> messageList){
        this.messageList = messageList ;
    }

    // client ������������Ϣ��һ���� ������ channel active ��ʱ�����Ƚ���Ϣ���ͳ�ȥ


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (Message message : messageList){
         //   System.out.println("-------------------- client sending message ---------------------") ;
            ctx.writeAndFlush( message) ;
            try{
                Thread.sleep( 3000 ); // Ϊ���� client cmd tool �����ã�ÿ�η���һ����Ϣ���ͻ���� 3 ����
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();

        throw new RuntimeException("error happens in NettyClientSenderHandler | exceptionCaught") ;
    }
}
