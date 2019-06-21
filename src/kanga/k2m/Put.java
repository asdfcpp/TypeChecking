package kanga.k2m;
public class Put {
	static String Tab = "";
	static String filename;
	static String output = "";
	public static void con(String s){
		System.out.printf(Tab + s);
	
	}
	public static void file(String s){
		
	}
	public static String getoutput()
	{
		return output;
	}
	public static void addTab()
	{
		Tab += "    ";
	}
	public static void minusTab()
	{
		Tab = Tab.substring(0,Tab.length()-4) ;
	}
	public static void called_function()
	{
		String temp = "         .text            \n" +
		 "         .globl _hallocs  \n" +
		 "_hallocs:                 \n" +
		 "         li $v0, 9        \n" +
		 "         syscall          \n" +
		 "         j $ra            \n" +
		 "                          \n" +
		 "         .text            \n" +
		 "         .globl _printint \n" +
		 "_printint:                \n" +
		 "         li $v0, 1        \n" +
		 "         syscall          \n" +
		 "         la $a0, newl     \n" +
		 "         li $v0, 4        \n" +
		 "         syscall          \n" +
		 "         j $ra            \n" +
		 "                          \n" +
		 "         .data            \n" +
		 "         .align   0       \n" +
		 "newl:    .asciiz \"\\n\"  \n" +
		 "         .data            \n" +
		 "         .align   0       \n" +
		 "str_er:  .asciiz \" ERROR: abnormal termination\\n\" "+
		 "                          \n" +
		 "         .text            \n" +
		 "         .globl _error    \n" +
		 "_error:                   \n" +
		 "         li $v0, 4        \n" +
		 "         la $a0, str_er   \n" +
		 "         syscall          \n" +
		 "         li $v0, 10       \n" +
		 "         syscall          \n";
		System.out.printf(temp);
	}
}

