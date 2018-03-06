package handler;

/**
 * ��ѯ����һ�������߳�����ѯ��Ϣ����
 * @author yuxue
 *
 */
public final class Looper {
	// Looper���󱣴���ThreadLocal�У���֤�����ݵĸ��룬ÿ�����߳��ж���һ��Looper����
	static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<>();
	//һ�����̶߳�Ӧһ��Looper����һ��Looper�����Ӧһ����Ϣ����
	MessageQueue mQueue;

	private Looper() {
		mQueue=new MessageQueue();
	}

	/**1
	 * Looper����ĳ�ʼ��
	 */
	public static void prepare() {
		if (sThreadLocal.get() != null) {
			throw new RuntimeException(
					"Only one Looper may be created per thread");
		}
		sThreadLocal.set(new Looper());
	}
	
	/**2
	 * ��ȡ��ǰ�̵߳�Looper����
	 * @return
	 */
	public static Looper myLooper(){
		return sThreadLocal.get();
	}
	/**3
	 * ��ѯ��Ϣ��һ�������߳�����ѯ��Ϣ���У�
	 * ��Ϣ����Ӧ�����ڴ�����ơ���������������ģʽ��
	 * ��Ϣ����Ϊnull�����߳�������Looperֹͣ��ѯ����
	 * ��Ϣ�������ˣ����߳�����ֹͣ������Ϣ
	 */
	public static void loop(){
		Looper me=Looper.myLooper();
		if (me==null) {
			throw new  RuntimeException("");
		}
		MessageQueue queue=me.mQueue;
		for (;;) {
			Message msg=queue.next();
			if (msg==null) {
				continue;
			}
			msg.target.dispatchMessage(msg);
		}
		
	}
}
