package symboltable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import syntaxtree.*;

public class MethodSymTable {
	public Type retVal;//返回值类型
	
	//形参（只纪录类型信息）
	public LinkedList<Type> params = new LinkedList<Type>();
	//局部变量
	public LinkedHashMap<String, TypeInfo> locals = new LinkedHashMap<String, TypeInfo>();
	
	public MethodSymTable(String s) {methodName = s;}
	//方法名
	public String methodName;
	//方法所在的类名
	public String inClass;
}


