package org.kylin.zhang.beans;

/**
 * Created by win-7 on 2015/9/19.
 */
public class ServerInfo {
    private String serverName ;
    private String ip ;
    private short port ;

    public ServerInfo() {
    }

    public ServerInfo(String serverName, String ip, short port) {
        this.serverName = serverName;
        this.ip = ip;
        this.port = port;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }



    @Override
    public String toString() {
        return "ServerInfo{" +
                "ip='" + ip + '\'' +
                ", serverName='" + serverName + '\'' +
                ", port=" + port +
                '}';
    }


}
