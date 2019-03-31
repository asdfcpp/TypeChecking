package symboltable;

public class MethodInfo {

	//methodST中记录方法中定义的形参、局部变量等信息
	public MethodSymTable methodST;
	public String retVal;
	public MethodInfo(MethodSymTable s, String _retVal)
	{
		methodST = s;
		retVal = _retVal;
	}
}