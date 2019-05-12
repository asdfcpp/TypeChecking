package symboltable;

import syntaxtree.Type;

public class TypeInfo {
	public Type typeVal;//该变量类型
	public String cName;
	public String originName;//该变量名
	public int offset;	//类中定义的变量的偏移量，即TEMP 0 offset
	public int number;	//函数中定义的的第number个变量，从20开始
	public TypeInfo(Type t, String c, String s, int o,int n)
	{
		typeVal = t;
		cName = c;
		originName = s;
		offset = o;
		number = n;
	}
}
