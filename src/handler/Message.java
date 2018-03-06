package handler;

public class Message {
	public int what;
	public Object obj;
	 Handler target;
	@Override
	public String toString() {
		return obj.toString();
	}
}
