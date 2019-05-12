package symboltable;

import java.util.Hashtable;

public class SymTable {
	//哈希表classTable的每项，储存一个类的符号信息
	public Hashtable<String, ClassSymTable> classTable= new Hashtable<String, ClassSymTable>();
}