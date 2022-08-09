package cn.hdj.scene;

import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Random;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/8/9 9:30
 */
public class BitSetTest {

    @Test
    public void test() {
        //2022年8月的签到情况
        BitSet bitSet = new BitSet(31);

        for (int i = 0; i < 10; i++) {
            int nextInt = new Random().nextInt(30);
            bitSet.set(nextInt,true);
        }

        for (int i = 0; i < 5; i++) {
            bitSet.set(i,true);
        }

        //2022-08-09 的签到情况
        boolean b = bitSet.get(9);
        System.out.println("9号签到情况： " + b);


        //2022-08 签到次数
        String s = bitSet.toString();
        System.out.println("2022-08 签到天: " + s);
        System.out.println("2022-08 签到次数: " + bitSet.cardinality());


        //2022-08-01~2022-08-05  是否连续签到
        BitSet week = bitSet.get(0, 5);
        System.out.println("2022-08-01~2022-08-05  是否连续签到" + week.toString());
        System.out.println("2022-08-01~2022-08-05  是否连续签到" + week.cardinality());
        System.out.println("2022-08-01~2022-08-05  是否连续签到" + (week.cardinality() == 5));

    }
}
