package visitor;
import java.util.LinkedHashMap;

import symboltable.*;
import syntaxtree.*;


public class BuildSymbolTableVisitor  extends GJDepthFirst<Object,Object>
{
	boolean isField = false;	//�Ƿ�����������
	String currClass;			//��ǰ���ڵ�����
	boolean flag = false;		//ɨ�����Ƿ��ִ���
	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 */	
	public String visit(Goal n, Object o)
	{
		n.f0.accept(this, o);
		//�������ඨ��
		n.f1.accept(this, o);
		//���������ඨ��
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
		//ȫ�ַ��ű�
		if(!s.classTable.containsKey(n.f1.f0.toString()))
		{//���classTable�в�������������������ӵ����ű���
			currClass = n.f1.f0.toString();
			String Func = "main";
			String Arg =  n.f11.f0.toString();
			
			ClassSymTable ClassTb = new ClassSymTable(currClass);
			//	����������Ϣ��
			MethodSymTable MethodTb = new MethodSymTable(Func);
			//	������������Ϣ��
			ClassTb.methods.put(Func, new MethodInfo(MethodTb,"void"));
			//	���෽�������루��������������Ϣ+�������ͣ�
			ClassTb.isSonClass = false;
			//���÷��������Ϣ
			NodeToken n1 = new NodeToken("String []");
			NodeChoice n0 = new NodeChoice(new Identifier(n1), 3);
			
			MethodTb.retVal = null;
			MethodTb.inClass = currClass;
			MethodTb.params.addLast(new Type(n0));
			MethodTb.locals.put(Arg, new TypeInfo(new Type(n0),"String []", Arg));
			//�ֲ���������루��������������Ϣ���ͣ�
			s.classTable.put(currClass, ClassTb);
			//���������Ϣ���뵽classTable��
		}
		else
		{//��������Ѷ������������������
			System.out.println("���ظ�����");
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
		//���ű�
		if(!s.classTable.containsKey(n.f1.f0.toString()))
		{
			currClass = n.f1.f0.toString();
			ClassSymTable ClassTb = new ClassSymTable(currClass);
			ClassTb.isSonClass = false;
			//������������Ϣ����Ϊ������ͺ����оֲ������Ĵ�������ͬ����������isFieldָʾ��
			isField=true;
			n.f3.accept(this,(Object) ClassTb.field);
			isField=false;
			//������к�������Ϣ
			n.f4.accept(this,(Object) ClassTb.methods);
			//������Ϣ������ű�
			s.classTable.put(currClass, ClassTb);
		}
		else
		{
			System.out.println("���ظ�����");
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
			//���ø�����Ϣ
			ClassTb.father = n.f3.f0.toString();
			ClassTb.isSonClass = true;
			//������������Ϣ����Ϊ������ͺ����оֲ������Ĵ�������ͬ����������isFieldָʾ��
			isField=true;
			n.f5.accept(this,(Object) ClassTb.field);
			isField=false;
			//��ӷ�����Ϣ
			n.f6.accept(this,(Object) ClassTb.methods);
			//����������Ϣ������ű�
			s.classTable.put(currClass, ClassTb);
		}
		else
		{
			System.out.println("���ظ�����");
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
			//s������filed��local
			String cName = (String)n.f0.accept(this, o);
			String id=n.f1.f0.toString();
			
			if (isField  && !s.containsKey(currClass + "." + id))//���еı�����Ϊ�˺ͺ����е���ʱ�������֣������ڵ�������Ϊǰ׺��Ϊ�˱����������ʶ���������ʼӡ�.������һ������
			{
				s.put(currClass + "." + id, new TypeInfo(n.f0, cName, id));
			}
			else//�����е���ʱ��������������
			if (!isField && !s.containsKey(id))
			{
				s.put(id, new TypeInfo(n.f0, cName, id));
			}
			else
			{
				System.out.println("���д���ͬ������");
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
			MethodTb.retVal = n.f1;//���÷���ֵ����
			String retType = (String)n.f1.accept(this,o);
			MethodTb.inClass = currClass;//��¼��������
			n.f4.accept(this,(Object) MethodTb);	//�����βα�
			n.f7.accept(this,(Object) MethodTb.locals);//�����ֲ���������
			s.put(Func, new MethodInfo(MethodTb,retType));
		}
		else
		{
			System.out.println("���д���ͬ������");
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
	{//���βα�
		MethodSymTable s = (MethodSymTable) o;
		String cName = (String)n.f0.accept(this,o);
		String id = n.f1.f0.toString();
		if(!s.locals.containsKey(n.f1.f0.toString()))	//ֻ����locals���ж��ظ�����
		{//���β����ͼ�¼��params�����У��β����ͺ����ּ�¼��locals���У�����Ϊ�ֲ���������
			s.locals.put(id, new TypeInfo(n.f0,cName,id));
			s.params.addLast(n.f0);
		}
		else
		{
			System.out.println("���������;ֲ������ظ�");
			flag = true;
		}
		return null;
	}
}
