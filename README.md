 zookeeper netty p2p 程序使用说明
 可执行脚本共有 2 个
 1. run_with_zk.bat 该脚本用于在系统中有 zookeeper-server 服务运行的时候，
	启动 zookeeper 监控程序，从 zk-server 读取配置文件，并且更新 netty-server 的本地配置文件，
	以及启动 5 个 netty-server 服务器
 2. run_without_zk.bat 该脚本用于在系统中没有 zookeeper 服务的时候，
	通过加载本地文件的方式来启动 5 个 netty-server
 3. zkCmdTool.bat 该脚本用于启动本地监控每个 netty-server 接收文件的状态

---------------------------------------------------------------------------------
使用方法说明(仅支持 windows 操作系统):

1.  准备工作
	1.1.首先确保主机上已经安装好 zookeeper-3.4.6 版本，
	1.2.启动 zookeeper 服务，让该服务运行在 ip=127.0.0.1 , port=2181 
	1.3 确保主机上端口号 1027 - 1031 端口处于空闲状态
	1.4 zk-server 上面所创建的路径名称，以及 netty-server的名称，地址，端口号，等配置信息
		会记录在 zk_conf.xml 和 server*.properties 的配置文件中

2.  启动程序
	 (双击运行） run_with_zk.bat 脚本
	 等到在命令窗口上显示所有的服务器 server1-server5 均启动并且运行在各自的端口号(默认是：1027-1032)上之后，
	 
	 (双击)运行 zkCmdTool.bat 脚本，开启一个新的 cmd 窗口
	  根据现实出的提示信息来具体的输入命令
	  
	   list_info server1 ---> 便会显示最新的 10 条 server1 节点上正在接收的文件的详细信息
	   kill      server1 ---> 会关闭 server1 的进程； 
	   关闭服务器的命令需要在集群中所有的服务器完成文件传输之后才能执行
	   文件传输完成之后会在服务器上显示如下的信息:
	   "now the client of serverX is close"
	   
	    如果 serverX 是 server1 命令出现 4 次, 
		因为 server1 会创建 netty-client 向 server2-5 剩余的 4 个 serverName 大于自己的 netty-server 发送请求文件消息
		
		同理，serverX 是 server2 的话， 上述命令会出现 3 次
		同理，serverX 是 server3 的话， 上述命令会出现 2 次
		如果，serverX 是 server4 的话， 上述命令会出现 1 次
		如果，serverX 是 server5 的话， 上述命令将会出现 0 次，
		因为 server1-4 中没有一个 serverName 大于 server5，
		所以 server5 不会创建 netty-client 向任何集群中的服务器发送请求文件的消息
	   
	   reset ----------------> 这条命令用来在所有服务器均停止之后执行，用于清空 zk 上面注册节点信息 
	   exit  ----------------> 输入这条命令之后，将会退出当前的 zkCmdTool 进程
	  
	  接收和发送的文件会在 data/ 路径下面找到
	  
	  
3.  在这里需要注意的是，如果没有在指定端口启动 zookeeper 服务器端的话，
	那么，需要运行 run_without_zk.bat 脚本，
    此时 netty-server 1-5 会通过加载本地配置文件的方式来启动，然后监听在配置文件的指定端口中
    zkCmdTool.bat 是zk-server 的客户端命令脚本， 如果 zk-server 没有运行的话，不要运行 zkCmdTool.bat 脚本 
	 
	 run_with_zk.bat 和 run_without_zk.bat 不可以同时运行
	 
4.  错误及处理方法: 由于多线程并发发送文件，所以很有可能会造成一些不可重现的错误；
     这个时候，需要强行停止程序 (ctrl+c) ,然后重新运行的脚本
	 
	 不过在重新运行脚本之前，需要保证运行程序的环境要和首次运行相同
	 4.1 通过 zkCmdTool 的 reset 命令将 zk-server 上的路径清零
     4.2 将 data/ 下面的所有文件全部清除(可选操作)


5.   没有修复的 bug: 
	 如果同一个节点接收 多次同样的文件将会显示出接收文件的进度数值为  n%, 接收到 n 次同样的文件	 