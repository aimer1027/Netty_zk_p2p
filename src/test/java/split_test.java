import org.junit.Test;

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
}
