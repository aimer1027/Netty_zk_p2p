package org.kylin.zhang.util;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.kylin.zhang.beans.ServerInfo;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by win-7 on 2015/9/29.
 *
 * 在这个类中，主要读取解析 xml 文件，然后返回一个数组
 * 1. 将 xml 文件加载到当前的，内存空间中
 * 2. 从 xml 文件中读取  主路径，配置信息路径，监听路径的信息
 *
 * 3. 迭代的读取出 conf-node 中的信息，并将其添加到
 *    对应的链表中去
 */
public class xmlLoader {
    private String mainPath ;
    private String confPath ;
    private String listenPath ;

    private List<ServerInfo> confDataList ;

    public xmlLoader(){
        this.confDataList = new ArrayList<ServerInfo>() ;


    }

    public void parseXML(){
        File xmlFile = new File("conf/zk_conf.xml") ;

        if( !xmlFile.exists()){
            System.out.println("config xml not exists ! \n path "+ xmlFile.getAbsolutePath()) ;
            System.exit(-1);
        }

        try{
        SAXReader reader = new SAXReader() ;
        Document doc = reader.read(xmlFile) ;

            // 获取文档的根节点对象
            Element root = doc.getRootElement() ;

            // 接下来获取主路径的名称
            this.mainPath = root.elementText("main-path") ;
            this.confPath = root.elementText("conf-path") ;
            this.listenPath = root.elementText("listen-path") ;

            List<Element> confList = root.elements("conf-node") ;

            for ( Element e: confList){
                ServerInfo servNode = new ServerInfo() ;

                servNode.setIp( e.elementText("ip"));
                servNode.setPort(Short.parseShort(e.elementText("port")));
                servNode.setServerName( e.attributeValue("id"));

                this.confDataList.add(servNode) ;
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public List<ServerInfo> getConfDataList() {
        return confDataList;
    }

    public void setConfDataList(List<ServerInfo> confDataList) {
        this.confDataList = confDataList;
    }

    public String getConfPath() {
        return confPath;
    }

    public void setConfPath(String confPath) {
        this.confPath = confPath;
    }

    public String getListenPath() {
        return listenPath;
    }

    public void setListenPath(String listenPath) {
        this.listenPath = listenPath;
    }

    public String getMainPath() {
        return mainPath;
    }

    public void setMainPath(String mainPath) {
        this.mainPath = mainPath;
    }

    @Override
    public String toString() {
        return "xmlLoader{" +
                "confDataList=" + confDataList +
                ", mainPath='" + mainPath + '\'' +
                ", confPath='" + confPath + '\'' +
                ", listenPath='" + listenPath + '\'' +
                '}';
    }
}
