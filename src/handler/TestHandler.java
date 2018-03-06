package handler;

import java.util.UUID;

/**
 * 模拟ActivityThread类的操作
 * @author yuxue
 *
 */
public class TestHandler {
	
	public static void main(String[] args) {
		Looper.prepare();//轮询器初始化
		final Handler handler=new Handler(){
			@Override
			public void handlMessage(Message msg) {
				super.handlMessage(msg);
				System.out.println(Thread.currentThread().getName()+",receiv msg="+msg.toString());
			}
		};
		
		for (int i = 0; i < 10; i++) {
			new Thread("子线程"+i){
				public void run() {
					while(true){
						Message msg=new Message();
						msg.what=1;
						synchronized (UUID.class) {
							msg.obj=Thread.currentThread().getName()+" send msg :"+UUID.randomUUID().toString();	
						}
						System.out.println(msg.obj);
						handler.sendMessage(msg);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
			}.start();
		}
		Looper.loop();//开始轮询,Activity不会ANR是因为后面的方法（生命周期）都是采用的消息处理机制,消息到来Main会被唤醒
	}
}
