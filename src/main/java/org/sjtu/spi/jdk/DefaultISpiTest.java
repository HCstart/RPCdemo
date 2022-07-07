package org.sjtu.spi.jdk;

/**
 * @author hcstart
 * @create 2022-06-19 12:46
 */
public class DefaultISpiTest implements ISpiTest{
    @Override
    public void doTest() {
        System.out.println("执行测试方法");
    }
}
