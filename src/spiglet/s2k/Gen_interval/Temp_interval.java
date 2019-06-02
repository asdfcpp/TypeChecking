package spiglet.s2k.Gen_interval;
public class Temp_interval {
	public int tempnum;
	public Interval liveInterval;
	Temp_interval(int a) {
		tempnum = a;		
	}
	Temp_interval(int a, int b, int c) {
		tempnum = a;
		liveInterval = new Interval(b,c);
	}
}