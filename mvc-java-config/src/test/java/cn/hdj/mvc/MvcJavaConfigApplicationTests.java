package cn.hdj.mvc;


import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class MvcJavaConfigApplicationTests {

	@Test
	public void test(){

		List<Integer> list = Arrays.asList(
				1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
		);

		PriorityQueue queue=new PriorityQueue(list);
		queue.offer(5);
		System.out.println(queue.size());
	}
}
