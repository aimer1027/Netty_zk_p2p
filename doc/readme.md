 本文档简单概述一下，Netty-Server 的 message-handler 在接收到不同消息的时候，
 不同的处理逻辑；
 该执行逻辑实现在，F:\current\dataguru_hw2\zk_netty_p2p\src\main\java\org\kylin\zhang\netty\server\handlers\NettyServerHandler.java
 这个文件中；
 对于服务器而言，在本项目中将其设定为是永久的被动相应的，
 在创建连接的开始，不会执行任何的主动发送消息的操作，均是接收到消息之后，在根据消息的类型采取不同的处理逻辑
 
 所以， 它的 channelActive 方法中无任何操作
 
 private NettyServer nettyServerProxy ;
 
 public void channelRead( ChannelHandlerContext ctx, Object msg  ) throws Exception {
 
	Message recvMessage = (Message) msg  ;/// 此为接收到的消息
	// 1. 接收到 来自 zk-monitor 发送过来的消息， 该消息类型可能是 {ZK_ONLINE , ZK_OFFLINE} 
	// 	  两种类型的一种， 
	//    1.1 如果是 ZK_ONLINE 的话， 说明通知的是有新节点上线了，并且该新节点的 server-name 要大于接收此消息的 server-name
	// 	      且， 该消息的数据部分所携带的 byte[] 一定是 ServerInfo 的序列化数据
			  /**
				执行的操作步骤如下
					1. 将获取的 ServerInfo 对象实例 :serverInfo ，添加到 nettyServerProxy 的 finger-table 中
					2. 创建 Message message = MessageBuilder.getServerInfoMessage( MessageType.REQ_FILE , 自己的 serverInfo 作为消息的数据  )
					3. 创建 NettyClient 对象， 构造参数(Message ： message， ServerInfo: 消息的接收者，为 zk-monitor 发送过来的消息中的 数据:即， 接收到的 serverInfo)
					4.  new Thread(NettyClient).start() ; 调用线程发送消息 
					
					注意，在这里连通的 channel 是 zk-monitor 和 netty-server 
					所以在这里消息的发送，并不能使用 ctx.writeAndFlush( message ) 来执行
			  */
	
	
	//    1.2 如果是 ZK_OFFLINE 的话， 说明在 zk-server 的监听路径下面一定有节点即将下线， 该消息的数据部分 同样也是 ServerInfo(即将掉线的 Server 的消息)
				
				
				下面的为思路（在这里添加的功能是， 每个节点在得知 某个节点即将掉线之后， 会回复相应的消息， 
							  而即将掉线的 节点，在接收到所有消息之后便会离线； 如果没有接收到所有的消息， 会等待 25 s 之后如果还是没有全部接收到
							  也会执行离线操作）
					
				在这里多出来一份设定， 也就是 finger-table 的安置	
				/***
				 执行的操作步骤如下， 
					 1. 从 Message 中抽取数据部分， 将其还原成 ServerInfo :recvServerInfo
					 2. 通过 NetyServer 的代理遍历 NettyServer 的 finger-table
					     删除对应的 recvServerInfo.getServerName() 的元素
					 3. 创建消息
					     Message 
							type : CLOSE_CONN
							 data : 自己的 ServerInfo: senderServerInfo 
				     4. 创建消息发送进程，并将消息发送出去.start(); 应该把 zk-monitor 发送消息的单线程方法广泛普及一下
				
					 5. 在 channelReadComplete(...){
						这个方法中  ， 关闭连接；
						
					 }
				
				*/
				
				/**
					执行的操作步骤如下:
							1. 从 Message 抽取数据部分， 然后将其还原成 ServerInfo 
							 2. 通过 NettyServer 的代理对象 Proxoy 来遍历 NettyServer 的 finger-table 
							    如果找到和 接收到的 ServerInfo 同名的元素的话， 将其 移除即可	
			
			
							3. 会创建 CONN_CLOSE 的消息给 ServerInfo 对象；{这里如果想要以发送消息的方式来实现的话，
								必须让 larger-server-name 的 netty-server 可以存放所有的， 
								smaller-server-name 的 netty-server 的缓冲队列才行
								这样的话， larger-one 便有了一份，所有 name 要比自己小的 节点信息了
								
								在 zk 将 larger-server-name 即将掉线的信息发送给 网络中所有的节点的时候，
										 1.如果是接收到的消息中 serverName < 自己的 serverName
												  遍历自己的 fileReqServeTable , 将对应的元素进行移除 ， 然后回复消息， CONN_CLOSE
										 2. 如果接受到的消息中 serverName > 自己的 serverName
												  遍历自己的 fingerTable ， 对应删除元素， 然后恢复消息 CONN_CLOSE
								不然， finger-table 将其设定， 无论name > 自己还是 < 自己都将其存放到 finger-table 中好了
								
								
								
								
								}
							<
								等等，在这里继续在考虑一下整体的逻辑
								
								设定有  s1<s2<s3 
								s1 ->register 
								
								s2 -> register  |---> zk-monitor ---> s1 发送 s2 的信息消息
												|---> s1 会将 s2 的消息存放在 finger-table 中， 但是 s2 并不会将 s1 存放在 finger-table 中，
								
							if	s1-> ready_off_line  |---> zk-monitor ---> s2 发送 s1 即将掉线的消息 
													 |---> s2 在自己的 finger-table 找不到 s1 的消息
													  
							if  s2-> ready_off_line  |---> zk-monitor ---> s1 发送 s2 即将掉线的消息
													 |---> s1 在自己的 finger-table 中能够找到 s2 的消息	， 将其删除
													 |---> 
							
							>
							
							
				*/
				
		2. 接收到的消息是从 其他 Netty-Server 发送过来的( ctx.writeAndFlush() 来实现消息的发送 )
		   2.1 REQ_FILE{
				 首先可以判定的是， 该消息类型的发送者的 server-name < 接收该消息的 server-name
				 其次，该消息的数据段的 byte[] 对应反序列化之后得到的对象类型是 ServerInfo 
				 最后， 连通的两个通信端是， netty-server <---> netty-server' ,所以发送消息的话，可以直接调用 ctx.writeAndFlush() 
				         方法来发送数据
				 
				 1. 从 Message 中抽取 ServerInfo: recvServerInfo 
				 2. 通过自身的 Netty-Server-Proxy 将 recvServerInfo 添加到 server 的 finger-table 中
				 3. 创建 Message 
						type: READY_SEND 
						data : null 
				 4.  通过 ctx.writeAndFlush( Message ) 来发送消息			   
		   
		   }
		   2.2 READY_SEND{
				通过消息类型可以判定，data 段 = null , 无需提取
				消息的对等两端分别是 ， 两个  netty-server 对象
				
				1. 接收到消息之后，创建 Message:
					type： READY_RECV
					data : null 
				2. 通过  ctx.writeAndFlush( Message ) 来发送消息 	
		   }

		   2.2 READY_RECV{
				通过消息类型可以判断出来 ， data : null 
				
				通过 Netty-Server-Proxy 来获取本地文件  10 个
				执行 循环， 将 10 个文件全部转换成一个 List<Message> fileMsgList ;
				List 中的消息 Message 
							type : FILE_SENDING 
							data : FileData 
				
				再次执行 fileMsgList 的循环
				for( Message message : fileMsgList){
					ctx.writeAndFlush(message) ;				
				}
				
				最后循环执行结束之后，再创建一个 Message:
					type : END_SEND
					data : null 
		   }
		   
		   2.3 SENDING_FILE{
				
				通过消息的类型可以判断出来， data 段 存放的是 FileData 
				
				通过 Netty-Server-Proxy 中提供的 addFileData ( FileData  ) ;
				
				该方法会执行文件的本地创建，追加和相关操作，
				同时还会更新，本地 Netty-Server 用于记录 文件传输状态的数据信息；
				并且把信息同步到 zk-server 的上面
				
		   }
		   
		   2.4 END_SEND{
				这个消息类型可以得知， data = null 
				
				在接收到这个消息之后， netty-server 便知道整个的文件发送结束
				在这里调用一下 Netty-Server-Proxy 对应的 resizeTable （具体名忘记了）
				调用这个方法之后， 会将 table 如果 table 中的元素超过了 10 个(规定是 10 个存放在缓存中)
				中传输状态为 100% 的元素移除， 如果是状态< 100% ，即便是元素超过了 10 个也不会移除；

				但是每次仅仅显示 10 个最新的元素
		   
		   }
		   2.5 CLOSE_CONN{
				
				这个消息，对应的数据字段是 ServerInfo :senderServerInfo 
				
				1. 通过 Netty-Server-Proxy 遍历自己的 finger-table ,从中找到
				    senderServerInfo.getServerName() 对应的元素并且将其删除
				2. 然后检查自己的 finger-table 是否为空，
					如果为空---> 等待 1s 之后，通过 Netty-Server-Proxy 调用关闭服务器的方法；
				     如果不为空 ---> 等待 100 s 之后，同样关闭服务器	
					不过，如何唤醒，Thread.sleep(100000) ; 这种方法， 毕竟我不能够保证
					当 这个Thread.sleep 的时候，万一来了另外一个 CLOSE_CONN 的消息的话，是否会 唤醒这个 sleep 的thread 
					同时， 如果想要通过 Netty-Server-Proxy 来关闭服务器的话，
					必须要解决的问题就是，关闭扔出去那个 监听线程
					
					这也就是说， EventLoopGroup 这些netty-server 组件，必须要将其作为  Netty-Server 的成员变量(泪... )
					
				3. 最后在 channelReadComplete (...){
					这个方法中，关闭 channel 连接
					
				因为， netty-server 启动的时候，自带 sync() 同步函数，所以，
				检测到对等端关闭连接的话，自己也会关闭连接 (也就是)
				
				在这里再对服务器进行关闭， 不然就会造成， 接收到消息之后，在 channelRead 方法中停滞(thread.sleep())
				然后停滞之后， 执行 channelReadComplete ， 关闭连接(这样就会造成，先关闭服务器，然后在关闭连接，出错会抛出异常)
				}	
		   
		   }
	 
 }
 
 =======================
 2015/10/10 进度：
 /**
  * 单元测试和系统测试：undone
  * zkMonitor 和 NettyServer_v1 还没有精简
  * zkCmdTool 交互环境还没有实现
  * */