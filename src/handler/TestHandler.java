package handler;

import java.util.UUID;

/**
 * ģ��ActivityThread��Ĳ���
 * @author yuxue
 *
 */
public class TestHandler {
	
	public static void main(String[] args) {
		Looper.prepare();//��ѯ����ʼ��
		final Handler handler=new Handler(){
			@Override
			public void handlMessage(Message msg) {
				super.handlMessage(msg);
				System.out.println(Thread.currentThread().getName()+",receiv msg="+msg.toString());
			}
		};
		
		for (int i = 0; i < 10; i++) {
			new Thread("���߳�"+i){
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
		Looper.loop();//��ʼ��ѯ,Activity����ANR����Ϊ����ķ������������ڣ����ǲ��õ���Ϣ�������,��Ϣ����Main�ᱻ����
	}
}
