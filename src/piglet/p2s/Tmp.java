package piglet.p2s;

public class Tmp { // syntaxtree中有同名节点Temp，避免重名
	public static int num;
	int mynum = 0;
	public Tmp() {
		++num;
		mynum = num;
	}
	Tmp(int number) {
		 mynum = number;
	}
	public String toString() {
		return "TEMP " + mynum;
	}
	public static int getNumber() {
		return num;
	}
}