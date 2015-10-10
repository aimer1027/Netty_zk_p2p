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
 * ��������У���Ҫ��ȡ���� xml �ļ���Ȼ�󷵻�һ������
 * 1. �� xml �ļ����ص���ǰ�ģ��ڴ�ռ���
 * 2. �� xml �ļ��ж�ȡ  ��·����������Ϣ·��������·������Ϣ
 *
 * 3. �����Ķ�ȡ�� conf-node �е���Ϣ����������ӵ�
 *    ��Ӧ��������ȥ
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

            // ��ȡ�ĵ��ĸ��ڵ����
            Element root = doc.getRootElement() ;

            // ��������ȡ��·��������
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
