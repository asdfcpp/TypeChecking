package minijava.m2p;

public class Put {
	static String tab = ""; /**ָʾ����*/
	static String filename;
	static String output = "";
	
	/**��������ʽ����Ϊ��������ӡ����*/
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