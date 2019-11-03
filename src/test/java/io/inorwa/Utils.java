package io.inorwa;

//import org.junit.Test;

import java.util.Date;

/**
 * Created by vgrazi on 9/2/16.
 */
public class Utils {
    public static void print(Object s) {
        System.out.printf("%s:%s%n", new Date(), s);
    }
    
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println("Exiting");
        }
    }
    
    private static long start = System.currentTimeMillis();
    
    public static Boolean isSlowTime() {
        return (System.currentTimeMillis() - start) % 12_000 >= 3_000;
    }
    
    public static Boolean isFastTime() {
        return ! isSlowTime();
    }

}

