import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.RetryNTimes;
import org.junit.Test;
import org.kylin.zhang.zookeeper.zkDao;

/**
 * Created by win-7 on 2015/10/12.
 */
public class split_test {

    @Test
    public void splitTest(){
        String line= "/Aimer/listen/server1" ;

        String lisn[] = line.split("/") ;

        System.out.println(lisn[3]) ;
    }

    @Test
    public void isZkServerAvailable(){
        CuratorZookeeperClient client = new CuratorZookeeperClient("127.0.0.1:2181",9000,90000,null , new RetryNTimes(1,1000)) ;

        System.out.println(client.isConnected()) ;
    }


    @Test
    public void testZkDaoConn(){
        zkDao zkDao = new zkDao("127.0.0.1" , (short)2181) ;





    }
}
