package handler;

public class Handler {
	private MessageQueue mQueue;
	private Looper mLooper;
	
	
	/**��2��
	 * handler�Ĵ���һ�������߳���
	 */
	public Handler(){
		mLooper = Looper.myLooper();//��ȡ���̵߳�Looper����
		mQueue=mLooper.mQueue;//���̵߳���Ϣ����
	}
	/**��1��
	 * ������Ϣ��ѹ�����
	 * @param msg
	 */
	public  void  sendMessage(Message msg) {
		msg.target=this;
		mQueue.enqueueMessage(msg);
	}
	/**
	 * ��4��
	 * @param msg
	 */
	public void dispatchMessage(Message msg){
		handlMessage(msg);
	}
	/**
	 * ��5��
	 * @param msg
	 */
	public void handlMessage(Message msg){
		
	}
}
