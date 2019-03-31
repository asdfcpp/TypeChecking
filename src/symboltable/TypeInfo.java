package symboltable;

import syntaxtree.Type;

public class TypeInfo {
	public Type typeVal;//该变量类型
	public String cName;
	public String originName;//该变量名
	public TypeInfo(Type t, String c, String s)
	{
		typeVal = t;
		cName = c;
		originName = s;
	}
}
