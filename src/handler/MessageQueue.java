package handler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageQueue {
	// 通过数组的结构存储Message对象
	Message[] items;
	final int msgMaxSize = 50;
	int putIndex;// 入队的元素索引位置
	int takeIndex;// 出队的元素索引位置
	int count = 0;// 计数器
	// 互斥锁,可以选择性的加锁，synchronized可以为代码块加锁
	private Lock lock;
	// 条件变量
	private Condition notEmpty;
	private Condition notFull;

	public MessageQueue() {
		items = new Message[msgMaxSize];
		this.lock = new ReentrantLock();// 可重入的锁
		notEmpty = lock.newCondition();
		notFull = lock.newCondition();

	}

	/**
	 * 加入队列，主要在子线程运行,生产产品，要立马通知消费
	 * 
	 * @param msg
	 */
	public void enqueueMessage(Message msg) {
		// System.out.println("加入队列");
		try {
			lock.lock();// 加锁
			while (count == items.length) {// 消息队列满了，子线程阻塞停止发送消息，有可能多个子线程if只会判断一次
				try {
					notFull.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			items[putIndex] = msg;
			putIndex = (++putIndex == items.length) ? 0 : putIndex;// 循环取值
			count++;
			// 生产出了产品通知消费线程（主线程）消费消息【notEmpty.wait()】
			notEmpty.signal();
		} finally {
			lock.unlock();
		}

	}

	/**
	 * 出队列，主要在主线程运行，消费产品通知可以生产
	 * 
	 * @return
	 */
	public Message next() {
		Message msg = null;
		try {
			lock.lock();
			// 消息队列为null，主线程阻塞（Looper停止轮询）
			while (count == 0) {
				try {
					notEmpty.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			msg = items[takeIndex];
			items[takeIndex] = null;// 取出以后元素置空
			takeIndex = (++takeIndex == items.length) ? 0 : takeIndex;// 循环取值
			count--;
			//消费了产品，通知生产消息【notFull.await()】
			notFull.signalAll();
		} finally {
			lock.unlock();
		}
		return msg;
	}
}
