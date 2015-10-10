package org.kylin.zhang.util;

import java.util.Random;

/**
 * Created by win-7 on 2015/10/10.
 */
public class RandomInteger {

    public static int getRandomInteger (int min , int max){
        Random random = new Random() ;

        return (random.nextInt(max)%(max-min+1) + min) ;
    }
}
