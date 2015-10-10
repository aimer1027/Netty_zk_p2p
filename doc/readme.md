 ���ĵ��򵥸���һ�£�Netty-Server �� message-handler �ڽ��յ���ͬ��Ϣ��ʱ��
 ��ͬ�Ĵ����߼���
 ��ִ���߼�ʵ���ڣ�F:\current\dataguru_hw2\zk_netty_p2p\src\main\java\org\kylin\zhang\netty\server\handlers\NettyServerHandler.java
 ����ļ��У�
 ���ڷ��������ԣ��ڱ���Ŀ�н����趨Ϊ�����õı�����Ӧ�ģ�
 �ڴ������ӵĿ�ʼ������ִ���κε�����������Ϣ�Ĳ��������ǽ��յ���Ϣ֮���ڸ�����Ϣ�����Ͳ�ȡ��ͬ�Ĵ����߼�
 
 ���ԣ� ���� channelActive ���������κβ���
 
 private NettyServer nettyServerProxy ;
 
 public void channelRead( ChannelHandlerContext ctx, Object msg  ) throws Exception {
 
	Message recvMessage = (Message) msg  ;/// ��Ϊ���յ�����Ϣ
	// 1. ���յ� ���� zk-monitor ���͹�������Ϣ�� ����Ϣ���Ϳ����� {ZK_ONLINE , ZK_OFFLINE} 
	// 	  �������͵�һ�֣� 
	//    1.1 ����� ZK_ONLINE �Ļ��� ˵��֪ͨ�������½ڵ������ˣ����Ҹ��½ڵ�� server-name Ҫ���ڽ��մ���Ϣ�� server-name
	// 	      �ң� ����Ϣ�����ݲ�����Я���� byte[] һ���� ServerInfo �����л�����
			  /**
				ִ�еĲ�����������
					1. ����ȡ�� ServerInfo ����ʵ�� :serverInfo ����ӵ� nettyServerProxy �� finger-table ��
					2. ���� Message message = MessageBuilder.getServerInfoMessage( MessageType.REQ_FILE , �Լ��� serverInfo ��Ϊ��Ϣ������  )
					3. ���� NettyClient ���� �������(Message �� message�� ServerInfo: ��Ϣ�Ľ����ߣ�Ϊ zk-monitor ���͹�������Ϣ�е� ����:���� ���յ��� serverInfo)
					4.  new Thread(NettyClient).start() ; �����̷߳�����Ϣ 
					
					ע�⣬��������ͨ�� channel �� zk-monitor �� netty-server 
					������������Ϣ�ķ��ͣ�������ʹ�� ctx.writeAndFlush( message ) ��ִ��
			  */
	
	
	//    1.2 ����� ZK_OFFLINE �Ļ��� ˵���� zk-server �ļ���·������һ���нڵ㼴�����ߣ� ����Ϣ�����ݲ��� ͬ��Ҳ�� ServerInfo(�������ߵ� Server ����Ϣ)
				
				
				�����Ϊ˼·����������ӵĹ����ǣ� ÿ���ڵ��ڵ�֪ ĳ���ڵ㼴������֮�� ��ظ���Ӧ����Ϣ�� 
							  ���������ߵ� �ڵ㣬�ڽ��յ�������Ϣ֮�������ߣ� ���û�н��յ����е���Ϣ�� ��ȴ� 25 s ֮���������û��ȫ�����յ�
							  Ҳ��ִ�����߲�����
					
				����������һ���趨�� Ҳ���� finger-table �İ���	
				/***
				 ִ�еĲ����������£� 
					 1. �� Message �г�ȡ���ݲ��֣� ���仹ԭ�� ServerInfo :recvServerInfo
					 2. ͨ�� NetyServer �Ĵ������ NettyServer �� finger-table
					     ɾ����Ӧ�� recvServerInfo.getServerName() ��Ԫ��
					 3. ������Ϣ
					     Message 
							type : CLOSE_CONN
							 data : �Լ��� ServerInfo: senderServerInfo 
				     4. ������Ϣ���ͽ��̣�������Ϣ���ͳ�ȥ.start(); Ӧ�ð� zk-monitor ������Ϣ�ĵ��̷߳����㷺�ռ�һ��
				
					 5. �� channelReadComplete(...){
						���������  �� �ر����ӣ�
						
					 }
				
				*/
				
				/**
					ִ�еĲ�����������:
							1. �� Message ��ȡ���ݲ��֣� Ȼ���仹ԭ�� ServerInfo 
							 2. ͨ�� NettyServer �Ĵ������ Proxoy ������ NettyServer �� finger-table 
							    ����ҵ��� ���յ��� ServerInfo ͬ����Ԫ�صĻ��� ���� �Ƴ�����	
			
			
							3. �ᴴ�� CONN_CLOSE ����Ϣ�� ServerInfo ����{���������Ҫ�Է�����Ϣ�ķ�ʽ��ʵ�ֵĻ���
								������ larger-server-name �� netty-server ���Դ�����еģ� 
								smaller-server-name �� netty-server �Ļ�����в���
								�����Ļ��� larger-one ������һ�ݣ����� name Ҫ���Լ�С�� �ڵ���Ϣ��
								
								�� zk �� larger-server-name �������ߵ���Ϣ���͸� ���������еĽڵ��ʱ��
										 1.����ǽ��յ�����Ϣ�� serverName < �Լ��� serverName
												  �����Լ��� fileReqServeTable , ����Ӧ��Ԫ�ؽ����Ƴ� �� Ȼ��ظ���Ϣ�� CONN_CLOSE
										 2. ������ܵ�����Ϣ�� serverName > �Լ��� serverName
												  �����Լ��� fingerTable �� ��Ӧɾ��Ԫ�أ� Ȼ��ָ���Ϣ CONN_CLOSE
								��Ȼ�� finger-table �����趨�� ����name > �Լ����� < �Լ��������ŵ� finger-table �к���
								
								
								
								
								}
							<
								�ȵȣ�����������ڿ���һ��������߼�
								
								�趨��  s1<s2<s3 
								s1 ->register 
								
								s2 -> register  |---> zk-monitor ---> s1 ���� s2 ����Ϣ��Ϣ
												|---> s1 �Ὣ s2 ����Ϣ����� finger-table �У� ���� s2 �����Ὣ s1 ����� finger-table �У�
								
							if	s1-> ready_off_line  |---> zk-monitor ---> s2 ���� s1 �������ߵ���Ϣ 
													 |---> s2 ���Լ��� finger-table �Ҳ��� s1 ����Ϣ
													  
							if  s2-> ready_off_line  |---> zk-monitor ---> s1 ���� s2 �������ߵ���Ϣ
													 |---> s1 ���Լ��� finger-table ���ܹ��ҵ� s2 ����Ϣ	�� ����ɾ��
													 |---> 
							
							>
							
							
				*/
				
		2. ���յ�����Ϣ�Ǵ� ���� Netty-Server ���͹�����( ctx.writeAndFlush() ��ʵ����Ϣ�ķ��� )
		   2.1 REQ_FILE{
				 ���ȿ����ж����ǣ� ����Ϣ���͵ķ����ߵ� server-name < ���ո���Ϣ�� server-name
				 ��Σ�����Ϣ�����ݶε� byte[] ��Ӧ�����л�֮��õ��Ķ��������� ServerInfo 
				 ��� ��ͨ������ͨ�Ŷ��ǣ� netty-server <---> netty-server' ,���Է�����Ϣ�Ļ�������ֱ�ӵ��� ctx.writeAndFlush() 
				         ��������������
				 
				 1. �� Message �г�ȡ ServerInfo: recvServerInfo 
				 2. ͨ������� Netty-Server-Proxy �� recvServerInfo ��ӵ� server �� finger-table ��
				 3. ���� Message 
						type: READY_SEND 
						data : null 
				 4.  ͨ�� ctx.writeAndFlush( Message ) ��������Ϣ			   
		   
		   }
		   2.2 READY_SEND{
				ͨ����Ϣ���Ϳ����ж���data �� = null , ������ȡ
				��Ϣ�ĶԵ����˷ֱ��� �� ����  netty-server ����
				
				1. ���յ���Ϣ֮�󣬴��� Message:
					type�� READY_RECV
					data : null 
				2. ͨ��  ctx.writeAndFlush( Message ) ��������Ϣ 	
		   }

		   2.2 READY_RECV{
				ͨ����Ϣ���Ϳ����жϳ��� �� data : null 
				
				ͨ�� Netty-Server-Proxy ����ȡ�����ļ�  10 ��
				ִ�� ѭ���� �� 10 ���ļ�ȫ��ת����һ�� List<Message> fileMsgList ;
				List �е���Ϣ Message 
							type : FILE_SENDING 
							data : FileData 
				
				�ٴ�ִ�� fileMsgList ��ѭ��
				for( Message message : fileMsgList){
					ctx.writeAndFlush(message) ;				
				}
				
				���ѭ��ִ�н���֮���ٴ���һ�� Message:
					type : END_SEND
					data : null 
		   }
		   
		   2.3 SENDING_FILE{
				
				ͨ����Ϣ�����Ϳ����жϳ����� data �� ��ŵ��� FileData 
				
				ͨ�� Netty-Server-Proxy ���ṩ�� addFileData ( FileData  ) ;
				
				�÷�����ִ���ļ��ı��ش�����׷�Ӻ���ز�����
				ͬʱ������£����� Netty-Server ���ڼ�¼ �ļ�����״̬��������Ϣ��
				���Ұ���Ϣͬ���� zk-server ������
				
		   }
		   
		   2.4 END_SEND{
				�����Ϣ���Ϳ��Ե�֪�� data = null 
				
				�ڽ��յ������Ϣ֮�� netty-server ��֪���������ļ����ͽ���
				���������һ�� Netty-Server-Proxy ��Ӧ�� resizeTable �������������ˣ�
				�����������֮�� �Ὣ table ��� table �е�Ԫ�س����� 10 ��(�涨�� 10 ������ڻ�����)
				�д���״̬Ϊ 100% ��Ԫ���Ƴ��� �����״̬< 100% ��������Ԫ�س����� 10 ��Ҳ�����Ƴ���

				����ÿ�ν�����ʾ 10 �����µ�Ԫ��
		   
		   }
		   2.5 CLOSE_CONN{
				
				�����Ϣ����Ӧ�������ֶ��� ServerInfo :senderServerInfo 
				
				1. ͨ�� Netty-Server-Proxy �����Լ��� finger-table ,�����ҵ�
				    senderServerInfo.getServerName() ��Ӧ��Ԫ�ز��ҽ���ɾ��
				2. Ȼ�����Լ��� finger-table �Ƿ�Ϊ�գ�
					���Ϊ��---> �ȴ� 1s ֮��ͨ�� Netty-Server-Proxy ���ùرշ������ķ�����
				     �����Ϊ�� ---> �ȴ� 100 s ֮��ͬ���رշ�����	
					��������λ��ѣ�Thread.sleep(100000) ; ���ַ����� �Ͼ��Ҳ��ܹ���֤
					�� ���Thread.sleep ��ʱ����һ��������һ�� CLOSE_CONN ����Ϣ�Ļ����Ƿ�� ������� sleep ��thread 
					ͬʱ�� �����Ҫͨ�� Netty-Server-Proxy ���رշ������Ļ���
					����Ҫ�����������ǣ��ر��ӳ�ȥ�Ǹ� �����߳�
					
					��Ҳ����˵�� EventLoopGroup ��Щnetty-server ���������Ҫ������Ϊ  Netty-Server �ĳ�Ա����(��... )
					
				3. ����� channelReadComplete (...){
					��������У��ر� channel ����
					
				��Ϊ�� netty-server ������ʱ���Դ� sync() ͬ�����������ԣ�
				��⵽�Եȶ˹ر����ӵĻ����Լ�Ҳ��ر����� (Ҳ����)
				
				�������ٶԷ��������йرգ� ��Ȼ�ͻ���ɣ� ���յ���Ϣ֮���� channelRead ������ͣ��(thread.sleep())
				Ȼ��ͣ��֮�� ִ�� channelReadComplete �� �ر�����(�����ͻ���ɣ��ȹرշ�������Ȼ���ڹر����ӣ�������׳��쳣)
				}	
		   
		   }
	 
 }
 
 =======================
 2015/10/10 ���ȣ�
 /**
  * ��Ԫ���Ժ�ϵͳ���ԣ�undone
  * zkMonitor �� NettyServer_v1 ��û�о���
  * zkCmdTool ����������û��ʵ��
  * */