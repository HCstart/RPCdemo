package org.sjtu.spi.jdk;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author hcstart
 * @create 2022-06-19 12:49
 */
public class TestSpiDemo {

    public static void doTest(ISpiTest iSpiTest){
        System.out.println("begin");
        iSpiTest.doTest();
        System.out.println("end");
    }

    public static void main(String[] args) {
        ServiceLoader<ISpiTest> serviceLoader = ServiceLoader.load(ISpiTest.class);
        Iterator<ISpiTest> iSpiTestIterator = serviceLoader.iterator();
        while (iSpiTestIterator.hasNext()){
            ISpiTest iSpiTest = iSpiTestIterator.next();
            TestSpiDemo.doTest(iSpiTest);
        }
    }

}
