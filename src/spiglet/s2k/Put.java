package spiglet.s2k;

public class Put {
	static String tab = "";
	static String filename;
	static String output = "";

	public static void gen(String s) {
		output += tab + s;
	}
	public static String kangaCode() {
		return output;
	}
}