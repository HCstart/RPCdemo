package org.sjtu.spi.jdk;

/**
 * @author hcstart
 * @create 2022-06-19 13:03
 */
public class DefaultISpiTest2 implements ISpiTest{
    @Override
    public void doTest() {
        System.out.println("执行测试方法2");
    }
}
