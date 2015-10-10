package org.kylin.zhang.beans;

/**
 * Created by win-7 on 2015/10/6.
 *
 * 这个类是用来在 netty-server 上面存放临时文件接收元素的
 *
 * HashTable<String, FileInfo>
 *
 */
public class FileInfo {

    private String senderName ;  // 消息发送者的 ServerName
    private String fileName ;    // 文件名称
    private int    fileTotalLength ;    // 文件的总长度
    private int    receivedTotalLength ;    // 已经接收到的文件长度(接收到的消息中数据长度  + 本地文件长度)
    private long    lastUpdateTimer ;       // 最后一次接收到文件的时间点

    public FileInfo() {
    }


    public FileInfo(String fileName, int fileTotalLength, long lastUpdateTimer, int receivedTotalLength, String senderName) {
        this.fileName = fileName;
        this.fileTotalLength = fileTotalLength;
        this.lastUpdateTimer = lastUpdateTimer;
        this.receivedTotalLength = receivedTotalLength;
        this.senderName = senderName;
    }

    // ------------- getter and setter -----------------

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

    public long getLastUpdateTimer() {
        return lastUpdateTimer;
    }

    public void setLastUpdateTimer(long lastUpdateTimer) {
        this.lastUpdateTimer = lastUpdateTimer;
    }

    public int getReceivedTotalLength() {
        return receivedTotalLength;
    }

    public void setReceivedTotalLength(int receivedTotalLength) {
        this.receivedTotalLength = receivedTotalLength;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
