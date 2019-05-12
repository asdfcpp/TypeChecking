package minijava.m2p;

public class Temp {
	public static int num;
	int mynum = 0;
	public Temp() {
		++num;
		mynum = num;
	}
	public Temp(int number) {
		 mynum = number;
	}
	public String toString() {
		return "TEMP " + mynum;
	}
	public static int getNumber() {
		return num;
	}
}