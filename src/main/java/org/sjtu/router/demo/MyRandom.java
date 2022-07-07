package org.sjtu.router.demo;

/**
 * @author hcstart
 * @create 2022-06-18 11:13
 *
 * 对随机方法进行测试
 *
 */
public class MyRandom {

    private long seed;

    private int mod;

    private long last;

    public MyRandom(int end) {
        this.seed = System.currentTimeMillis();
        this.mod = end;

    }

    public long randomCount() {
        if (last == 0) {
            last = (int) (System.currentTimeMillis() % mod);
        }
        long n1 = (last * seed + 11) % mod;
        last = n1;
        return n1;
    }

    public static void main(String[] args) {
        MyRandom myRandom = new MyRandom(13);
        for (int i = 0; i < 100; i++) {
//            long result = myRandom.randomCount();
//            System.out.println(result);
            System.out.println(System.nanoTime());
        }
    }
}