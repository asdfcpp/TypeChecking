package minijava.m2p;

public class Label {
	public static int num;
	String currS;
	int mynum;
	public Label(String s) {
		++num;
		mynum = num;
		currS = s;
	}
	public String toString() {
		return currS + mynum;
	}
}