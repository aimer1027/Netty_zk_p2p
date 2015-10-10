package org.kylin.zhang.beans;

/**
 * Created by win-7 on 2015/10/6.
 *
 * 这个类对象将会被 json 处理之后上传到 zk-server 的上面
 * 1. 文件发送者名字
 * 2. 文件名字
 * 3. 文件的总长度
 * 4. 发送进程
 * 5. 从接收到此文件数据消息开始，到最新一次文件内容的更新总共过了多久
 *
 */
public class ServerInClusterInfo {
    private String senderName ;
    private String fileName ;
    private int    fileTotalLength ;
    private double process ;
    private long     secondsPast ;

    public ServerInClusterInfo() {
    }

    public ServerInClusterInfo(String fileName, int fileTotalLength, double process, long secPast, String senderName) {
        this.fileName = fileName;
        this.fileTotalLength = fileTotalLength;
        this.process = process;
        this.secondsPast = secPast;
        this.senderName = senderName;
    }

    // ------------ getter and setter -----------------

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileTotalLength() {
        return fileTotalLength;
    }

    public void setFileTotalLength(int fileTotalLength) {
        this.fileTotalLength = fileTotalLength;
    }

    public double getProcess() {
        return process;
    }

    public void setProcess(double process) {
        this.process = process;
    }

    public long getSecPast() {
        return secondsPast;
    }

    public void setSecPast(int secPast) {
        this.secondsPast = secPast;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
