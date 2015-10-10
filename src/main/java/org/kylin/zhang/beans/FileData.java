package org.kylin.zhang.beans;

/**
 * Created by win-7 on 2015/10/6.
 *
 * FileData 类中封装的是，在消息中传输文件对应的数据段信息
 *
 * 1. 发送者名称 ：        senderName    String
 * 2. 发送文件名称:        fileName      String
 * 3. 发送文件的总长度:      fileTotalLen int
 * 4. 此次发送文件数据部分的长度:    fileLength  int
 * 5. 文件的数据部分  fileContent byte []
 * 6. 该消息的发送时间  sendTimer Date
 */
public class FileData {

    private String senderName ;
    private String fileName ;
    private int   fileTotalLen ;
    private int   fileLenght ;
    private byte [] dataContent ;
    private long  sendTimer ;


    public FileData() {
    }

    public FileData(byte[] dataContent, int fileLenght, String fileName, int fileTotalLen, String senderName, long sendTimer) {
        this.dataContent = dataContent;
        this.fileLenght = fileLenght;
        this.fileName = fileName;
        this.fileTotalLen = fileTotalLen;
        this.senderName = senderName;
        this.sendTimer = sendTimer;
    }


    //------------ getter and setter

    public byte[] getDataContent() {
        return dataContent;
    }

    public void setDataContent(byte[] dataContent) {
        this.dataContent = dataContent;
    }

    public int getFileLenght() {
        return fileLenght;
    }

    public void setFileLenght(int fileLenght) {
        this.fileLenght = fileLenght;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileTotalLen() {
        return fileTotalLen;
    }

    public void setFileTotalLen(int fileTotalLen) {
        this.fileTotalLen = fileTotalLen;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getSendTimer() {
        return sendTimer;
    }

    public void setSendTimer(long sendTimer) {
        this.sendTimer = sendTimer;
    }
}
