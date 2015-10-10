package org.kylin.zhang.util;


import org.codehaus.jackson.map.ObjectMapper;


/**
 * Created by win-7 on 2015/9/20.
 *
 * 可以通过 将 Info --> String --> getBytes() ---> data 上传到 zookeeper 的上面
 *
 */

public class JsonPacker {
    public static String getJsonString (Object obj ) {
        ObjectMapper objMapper = new ObjectMapper();
        String str = null;
        try {
            str = objMapper.writeValueAsString(obj);

        } catch (Exception e) {

            throw new RuntimeException("[error: JsonPacker getJsonString error ]");
        }

        return str;
    }

    public static Object getJsonObject (String str , Class className){
        ObjectMapper objMapper = new ObjectMapper() ;
        Object obj = null ;

        if ( str == null || str.length() <= 0 ){
            System.out.println("[error: null String value]") ;
            return null ;
        }

        try{
            obj = objMapper.readValue(str, className) ;
        } catch(Exception e){
            throw new RuntimeException("[error: JsonPacker getJsonObject error ]");
        }

        return obj ;
    }
}


