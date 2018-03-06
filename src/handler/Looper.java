package handler;

/**
 * 轮询器，一般在主线程中轮询消息队列
 * @author yuxue
 *
 */
public final class Looper {
	// Looper对象保存在ThreadLocal中，保证了数据的隔离，每个主线程中都有一个Looper对象
	static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<>();
	//一个主线程对应一个Looper对象，一个Looper对象对应一个消息队列
	MessageQueue mQueue;

	private Looper() {
		mQueue=new MessageQueue();
	}

	/**1
	 * Looper对象的初始化
	 */
	public static void prepare() {
		if (sThreadLocal.get() != null) {
			throw new RuntimeException(
					"Only one Looper may be created per thread");
		}
		sThreadLocal.set(new Looper());
	}
	
	/**2
	 * 获取当前线程的Looper对象
	 * @return
	 */
	public static Looper myLooper(){
		return sThreadLocal.get();
	}
	/**3
	 * 轮询消息，一般在主线程中轮询消息队列，
	 * 消息队列应该有内存的限制——生产者消费者模式：
	 * 消息队列为null，主线程阻塞（Looper停止轮询），
	 * 消息队列满了，子线程阻塞停止发送消息
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
