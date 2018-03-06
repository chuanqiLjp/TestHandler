package handler;

public class Handler {
	private MessageQueue mQueue;
	private Looper mLooper;
	
	
	/**【2】
	 * handler的创建一般在主线程中
	 */
	public Handler(){
		mLooper = Looper.myLooper();//获取主线程的Looper对象
		mQueue=mLooper.mQueue;//主线程的消息队列
	}
	/**【1】
	 * 发送消息，压入队列
	 * @param msg
	 */
	public  void  sendMessage(Message msg) {
		msg.target=this;
		mQueue.enqueueMessage(msg);
	}
	public void dispatchMessage(Message msg){
		handlMessage(msg);
	}
	public void handlMessage(Message msg){
		
	}
}
