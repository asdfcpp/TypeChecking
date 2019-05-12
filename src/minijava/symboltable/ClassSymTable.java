package minijava.symboltable;

import java.util.LinkedHashMap;

public class ClassSymTable {
	//field储存类中定义的变量信息。
	public LinkedHashMap<String, TypeInfo> field = new LinkedHashMap<String, TypeInfo>();
	//methods储存类中定义的方法的相关信息
	public LinkedHashMap<String, MethodInfo> methods = new LinkedHashMap<String, MethodInfo>();
	//存储此类
	//类名
	public String className;
	
	//是否是子类
	public boolean isSonClass = false;
	
	//父类名
	public String father;
	
	public ClassSymTable(String s)
	{
		className = s;
	}
}
