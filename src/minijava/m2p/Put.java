package minijava.m2p;

public class Put {
	static String tab = ""; /**指示缩进*/
	static String filename;
	static String output = "";
	
	/**以增量方式（作为变量）打印代码*/
	public static void gen(String s) {
		output += tab + s;
	}
	public static void up() {
		tab += "\t";
	}
	public static void down() {
		tab = tab.substring(0, Put.tab.length() - 1);
	}
	public static String pigletCode() {
		return output;
	}
}