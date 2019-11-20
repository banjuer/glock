package io.banjuer.glock.server.lock;


import io.banjuer.glock.core.rpc.api.LockService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 锁并发安全测试: 结果OK
 * 1. 模拟多个服务器同时竞争一把锁
 * 2. 模拟多个服务器同时获取不同锁
 * 3. 统计各种情况下最终计算结果与预期是否一致
 */
public class SimpleLockServiceTest {

	static Map<Node, Object> testResult = new HashMap<>();

	public static void main(String[] args) {
		// 模拟有多少个服务器争取同一把锁
		int threads = 10;
		// 每个服务器在同一个变量上累加次数
		int count = 10;
		// 模拟同时有多少把锁
		int groups = 500;
		SimpleLockService lock = new SimpleLockService();
		String groupName;
		for (int i = 0; i < groups; i++) {
			// 加锁对象
			groupName = "lock" + i;
			threadGroup(groupName, lock, count, threads, new Node(groupName));
		}
		check(groups, threads, count);
	}

	private static void check(int groups, int threads, int count) {
		int numtotal = threads * count;
		boolean success = testResult.size() == groups;
		if (!success) {
			System.out.println("并发测试失败: 并发锁数有问题");
		}
		Set<Node> nodes = testResult.keySet();
		for (Node node : nodes) {
			if (node.num != numtotal)
				System.out.println(String.format("并发测试失败: 组{%s}最终统计结果为{%s},应为{%s}", node.groupName, node.num, numtotal));
		}
		System.out.println(nodes);
		System.out.println(String.format("并发测试成功. 锁并发数{%s}, 同一锁上并发数{%s}", groups, threads));
	}

	private static void threadGroup(String key, SimpleLockService lock, int count, int threads, Node data) {
		for (int i = 0; i < threads; i++) {
			Client client = new Client();
			client.lock = lock;
			client.host = "host" + i;
			client.count = count;
			client.key = key;
			client.data = data;
			Thread t = new Thread(client);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 竞争的资源
	 */
	static class Node {
		public Node(String groupName) {
			this.groupName = groupName;
		}
		int num;
		String groupName;

		@Override
		public String toString() {
			return "Node{" +
					"num=" + num +
					", groupName='" + groupName + '\'' +
					'}';
		}
	}

	static class Client implements Runnable {
		String key;
		String host;
		int count;
		LockService lock;
		Node data;

		@Override
		public void run() {
			lock.lock(key, host);
			System.out.println(String.format("{%s}在{%s}上获得锁", host, key));
			for (int i = 0; i < count; i++) {
				data.num++;
			}
			System.out.println(data.num);
			testResult.put(data, null);
			lock.unlock(key, host);
			System.out.println(String.format("{%s}在{%s}上释放锁", host, key));

		}
	}

}
