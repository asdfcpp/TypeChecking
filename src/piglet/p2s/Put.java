package piglet.p2s;

import piglet.p2s.Put;
// same as put in piglet, but no need to use tab (and "up" "down").
public class Put {
	static String filename;
	static String output = "";
	
	/**��������ʽ����Ϊ��������ӡ����*/
	public static void gen(String s) {
		output += s;
	}
	public static String spigletCode() {
		return output;
	}
}