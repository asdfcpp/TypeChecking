package visitor;
import java.util.LinkedHashMap;

import symboltable.*;
import syntaxtree.*;


public class BuildSymbolTableVisitor  extends GJDepthFirst<Object,Object>
{
	boolean ClassField = false;	//是否在类作用域
	String currClass;			//当前所在的类名
	public boolean flag = false;		//扫描中是否发现错误
	public int temp = 20;	//寄存器编号
	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 */	
	public String visit(Goal n, Object o)
	{
		n.f0.accept(this, o);
		//访问主类定义
		n.f1.accept(this, o);
		//访问其他类定义
		return null;
	}
    /**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> "public"
	 * f4 -> "static"
     * f5 -> "void"
	 * f6 -> "main"
	 * f7 -> "("
	 * f8 -> "String"
	 * f9 -> "["
	 * f10 -> "]"
	 * f11 -> Identifier()
	 * f12 -> ")"
	 * f13 -> "{"
	 * f14 -> PrintStatement()
	 * f15 -> "}"
	 * f16 -> "}"
     */
	public String visit(MainClass n, Object o)
	{
		SymTable s = (SymTable) o;
		//全局符号表
		if(!s.classTable.containsKey(n.f1.f0.toString()))
		{//如果classTable中不包含该类名，将其添加到符号表中
			currClass = n.f1.f0.toString();
			String Func = "main";
			String Arg =  n.f11.f0.toString();
			
			ClassSymTable ClassTb = new ClassSymTable(currClass);
			//	创建主类信息表
			MethodSymTable MethodTb = new MethodSymTable(Func);
			//	创建主函数信息表
			ClassTb.methods.put(Func, new MethodInfo(MethodTb,"void",ClassTb.methods.size()*4));
			//	主类方法表，加入（方法名，方法信息+返回类型）
			ClassTb.isSonClass = false;
			//设置方法相关信息
			NodeToken n1 = new NodeToken("String []");
			NodeChoice n0 = new NodeChoice(new Identifier(n1), 3);
			
			MethodTb.retVal = null;
			MethodTb.inClass = currClass;
			MethodTb.params.addLast(new Type(n0));
			MethodTb.locals.put(Arg, new TypeInfo(new Type(n0),"String []", Arg,0,-1));
			//局部变量表加入（参数名，参数信息类型），String [] 参数用不到
			s.classTable.put(currClass, ClassTb);
			//将该类的信息加入到classTable中
		}
		else
		{//如果该类已定义过，报错并继续分析
			System.out.println("类重复定义");
			flag = true;
		}
		return null;
	}
	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
	public String visit(ClassDeclaration n, Object o)
	{
		SymTable s = (SymTable) o;
		//符号表
		if(!s.classTable.containsKey(n.f1.f0.toString()))
		{
			currClass = n.f1.f0.toString();
			ClassSymTable ClassTb = new ClassSymTable(currClass);
			ClassTb.isSonClass = false;
			//添加类中域的信息（因为类中域和函数中局部变量的处理方法不同，所以用了ClassField指示）
			ClassField=true;
			n.f3.accept(this,(Object) ClassTb.field);
			ClassField=false;
			//添加类中函数的信息
			n.f4.accept(this,(Object) ClassTb.methods);
			//将类信息加入符号表
			s.classTable.put(currClass, ClassTb);
		}
		else
		{
			System.out.println("类重复定义");
			flag = true;
		}
		return null;		
	}
	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "extends"
	 * f3 -> Identifier()
	 * f4 -> "{"
	 * f5 -> ( VarDeclaration() )*
	 * f6 -> ( MethodDeclaration() )*
	 * f7 -> "}"
	 */	
	public String visit(ClassExtendsDeclaration n, Object o)
	{	
		SymTable s = (SymTable) o;
		if(!s.classTable.containsKey(n.f1.f0.toString()))
		{
			currClass = n.f1.f0.toString();
			ClassSymTable ClassTb=new ClassSymTable(currClass);
			//设置父类信息
			ClassTb.father = n.f3.f0.toString();
			ClassTb.isSonClass = true;
			//添加类中域的信息（因为类中域和函数中局部变量的处理方法不同，所以用了ClassField指示）
			ClassField=true;
			n.f5.accept(this,(Object) ClassTb.field);
			ClassField=false;
			//添加方法信息
			n.f6.accept(this,(Object) ClassTb.methods);
			//将这个类的信息加入符号表
			s.classTable.put(currClass, ClassTb);
		}
		else
		{
			System.out.println("类重复定义");
			flag = true;
		}
		return null;
	}
	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 */
	public String visit(VarDeclaration n, Object o)
	{
			LinkedHashMap<String, TypeInfo> s  = (LinkedHashMap<String, TypeInfo>) o;
			//s可能是filed或local
			String TypeName = (String)n.f0.accept(this, o);
			String id=n.f1.f0.toString();
			
			if (ClassField  && !s.containsKey(currClass + "." + id))//类中的变量，为了和函数中的临时变量区分，用所在的类名作为前缀，为了避免和其他标识符混淆，故加“.”做进一步区分
			{
				s.put(currClass + "." + id, new TypeInfo(n.f0, TypeName, id , s.size()*4+4, -1));
			}
			else//函数中的临时变量，名字正常
			if (!ClassField && !s.containsKey(id))
			{
				s.put(id, new TypeInfo(n.f0, TypeName, id , -1, temp++));
			}
			else
			{
				System.out.println("类中存在同名变量");
				flag = true;
			}
			return null;
	}
	/**
	 * f0 -> ArrayType()
	 *       | BooleanType()
	 *       | IntegerType()
	 *       | Identifier()
	 */
	public String visit(Type n, Object argu) {
	    String s = (String)n.f0.accept(this, argu);
	    return s;
	}
	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Identifier n, Object argu) {
	   
	    n.f0.accept(this, argu);
	    return n.f0.toString();
	}
	public String visit(ArrayType n, Object argu) {
		   
	    
	    return n.f0.toString();
	}
	public String visit(IntegerType n, Object argu) {
		   
	   
	    return n.f0.toString();
	}
	public String visit(BooleanType n, Object argu) {
		   
	    
	    return n.f0.toString();
	}
	
	/**
	 * f0 -> "public"
	 * f1 -> Type()
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( FormalParameterList() )?
	 * f5 -> ")"
	 * f6 -> "{"
	 * f7 -> ( VarDeclaration() )*
	 * f8 -> ( Statement() )*
	 * f9 -> "return"
	 * f10 -> Expression()
	 * f11 -> ";"
	 * f12 -> "}"
	 */
	public String visit(MethodDeclaration n, Object o)
	{
		LinkedHashMap<String, MethodInfo> s = (LinkedHashMap<String, MethodInfo>) o;
		String Func = n.f2.f0.toString();
		if(!s.containsKey(Func))
		{
			MethodSymTable MethodTb = new MethodSymTable(Func);
			MethodTb.retVal = n.f1;//设置返回值类型
			String retType = (String)n.f1.accept(this,o);
			MethodTb.inClass = currClass;//记录所在类名
			n.f4.accept(this,(Object) MethodTb);	//遍历形参表
			n.f7.accept(this,(Object) MethodTb.locals);//遍历局部变量声明
			s.put(Func, new MethodInfo(MethodTb,retType,s.size()*4));
		}
		else
		{
			System.out.println("类中存在同名方法");
			flag = true;
		}
		return null;
	}
	/**
	 * Grammar production:
	 * f0 -> Type()
	 * f1 -> Identifier()
	 */
	public String visit(FormalParameter n, Object o)
	{//对形参表
		MethodSymTable s = (MethodSymTable) o;
		String TypeName = (String)n.f0.accept(this,o);
		String id = n.f1.f0.toString();
		if(!s.locals.containsKey(n.f1.f0.toString()))	//只需在locals中判断重复定义
		{//将形参类型记录在params链表中，形参类型和名字记录在locals表中（即作为局部变量处理）
			s.locals.put(id, new TypeInfo(n.f0,TypeName,id,-1,s.params.size()+1));
			s.params.addLast(n.f0);
		}
		else
		{
			System.out.println("函数参数和局部变量重复");
			flag = true;
		}
		return null;
	}
}
