package org.kylin.zhang.zookeeper.zkClientTool;

import org.kylin.zhang.beans.ServerInClusterInfo;
import org.kylin.zhang.util.JsonPacker;
import org.kylin.zhang.zookeeper.zkDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by win-7 on 2015/10/7.
 */
public class zkClientCmdTool {
    private final String pathName ="/Aimer/command" ;

    private zkDao zkClientHandler ;

    public zkClientCmdTool( String zkIP , short zkPort ){
        zkClientHandler = new zkDao(zkIP,zkPort) ;

        zkClientHandler.connectToServer();
    }

    public List<ServerInClusterInfo> getInfoFromPath( String serverName ){

          String jsonInfoString = new String (zkClientHandler.getDataByPath(pathName+'/'+serverName)) ;


        System.out.println(jsonInfoString) ;

           String [] jsonStringArray  = jsonInfoString.split("&") ;

            List<ServerInClusterInfo> clusterInfosList = new ArrayList<ServerInClusterInfo>() ;

            for( int i = 0 ; i < jsonStringArray.length ; i++ ){
                ServerInClusterInfo serverInClusterInfo = (ServerInClusterInfo)JsonPacker.getJsonObject(jsonStringArray[i] , ServerInClusterInfo.class) ;

                clusterInfosList.add(serverInClusterInfo) ;

            }
        return clusterInfosList ;
    }


    public void printServerInfo( String serverName ){
        List<ServerInClusterInfo> serverInClusterInfosList = getInfoFromPath( serverName) ;

        if(serverInClusterInfosList.size() == 0 )
            return ;

        long currentTimer = new Date().getTime() ;
        System.out.println("Node Name\t\t|| FileName \t\t|| Total Length || Process \t\t\t || Time Past(s)  ") ;
        for( ServerInClusterInfo serverInfo : serverInClusterInfosList ){
            System.out.print(serverInfo.getSenderName()+"   \t\t\t") ;
            System.out.print("("+serverInfo.getSenderName()+")"+serverInfo.getFileName()+"    \t\t") ;
            System.out.print(serverInfo.getFileTotalLength()+"   \t\t") ;
            System.out.printf("%.2f \t\t\t\t" ,serverInfo.getProcess()) ;
          //  System.out.print(serverInfo.getProcess()+"   \t\t") ;
            System.out.println(serverInfo.getSecPast() / 1000 + "   \t\t") ;
        }

    }

    public static void startZkCmdTool(){
        new Thread (){
            @Override
            public void run(){
                System.out.println("input server name ") ;

                System.out.println("you input server2") ;

                System.out.println("create cmd tool instance ") ;
                zkClientCmdTool zkClientCmdTool = new zkClientCmdTool("127.0.0.1" , (short)2181) ;

                System.out.println("get information from server 2 to local ") ;
                zkClientCmdTool.getInfoFromPath("server2") ;

                System.out.println("will out put info of server on cluster") ;
                zkClientCmdTool.printServerInfo("server2");
            }

        }.start();
    }

}
