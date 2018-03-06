package handler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageQueue {
	// ͨ������Ľṹ�洢Message����
	Message[] items;
	final int msgMaxSize = 50;
	int putIndex;// ��ӵ�Ԫ������λ��
	int takeIndex;// ���ӵ�Ԫ������λ��
	int count = 0;// ������
	// ������,����ѡ���Եļ�����synchronized����Ϊ��������
	private Lock lock;
	// ��������
	private Condition notEmpty;
	private Condition notFull;

	public MessageQueue() {
		items = new Message[msgMaxSize];
		this.lock = new ReentrantLock();// ���������
		notEmpty = lock.newCondition();
		notFull = lock.newCondition();

	}

	/**
	 * ������У���Ҫ�����߳�����,������Ʒ��Ҫ����֪ͨ����
	 * 
	 * @param msg
	 */
	public void enqueueMessage(Message msg) {
		// System.out.println("�������");
		try {
			lock.lock();// ����
			while (count == items.length) {// ��Ϣ�������ˣ����߳�����ֹͣ������Ϣ���п��ܶ�����߳�ifֻ���ж�һ��
				try {
					notFull.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			items[putIndex] = msg;
			putIndex = (++putIndex == items.length) ? 0 : putIndex;// ѭ��ȡֵ
			count++;
			// �������˲�Ʒ,֪ͨ�����̣߳����̣߳�������Ϣ��notEmpty.wait()��
			notEmpty.signal();
		} finally {
			lock.unlock();
		}

	}

	/**
	 * �����У���Ҫ�����߳����У����Ѳ�Ʒ֪ͨ��������
	 * 
	 * @return
	 */
	public Message next() {
		Message msg = null;
		try {
			lock.lock();
			// ��Ϣ����Ϊnull�����߳�������Looperֹͣ��ѯ��
			while (count == 0) {
				try {
					notEmpty.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			msg = items[takeIndex];
			items[takeIndex] = null;// ȡ���Ժ�Ԫ���ÿ�
			takeIndex = (++takeIndex == items.length) ? 0 : takeIndex;// ѭ��ȡֵ
			count--;
			//�����˲�Ʒ��֪ͨ������Ϣ��notFull.await()��
			notFull.signalAll();
		} finally {
			lock.unlock();
		}
		return msg;
	}
}
