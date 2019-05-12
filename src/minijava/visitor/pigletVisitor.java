package minijava.visitor;

import java.util.Enumeration;
import java.util.LinkedList;

import minijava.visitor.GJDepthFirst;
import minijava.m2p.Label;
import minijava.m2p.Put;
import minijava.m2p.Temp;
import minijava.symboltable.*;
import minijava.syntaxtree.*;


public class pigletVisitor extends GJDepthFirst<String,Object> {
	SymTable SymTable;
	final static String[] types= {"ArrayType", "BooleanType", "IntegerType"};
	
	public pigletVisitor(SymTable m, int _num,String ss){
		SymTable = m;
		Temp.num = _num;
		Label.num = 0;
	}

	String get_name(Type x) {
		if(x == null) return null;
		if(x.f0.which >= 0 && x.f0.which < 3) return types[x.f0.which];
		if(x.f0.which == 3) return ((Identifier)x.f0.choice).f0.toString();
		return "???";
	}
	
	/**
	 * f0 -> MainClass()
	 * f1 -> ( StringDeclaration() )*
	 * f2 -> <EOF>
	 */
	public String visit(Goal n, Object argu) {
		Put.gen("MAIN\n");
	    n.f0.accept(this, argu);
	    Put.gen("END\n");
	    n.f1.accept(this, argu);
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
	public String visit(MainClass n, Object argu) {
		n.f14.accept(this, argu);
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
	public String visit(ClassDeclaration n, Object argu) {
		n.f4.accept(this, SymTable.classTable.get(n.f1.f0.toString()));
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
	public String visit(ClassExtendsDeclaration n, Object argu) {
		n.f6.accept(this, SymTable.classTable.get(n.f1.f0.toString()));
		return null;
	}
	
	/**
	 * f0 -> String()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 */
	public String visit(VarDeclaration n, Object argu) {
	    return null;
	}
	
	/**f0 -> "public"
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
	public String visit(MethodDeclaration n, Object argu) {
		ClassSymTable currClassTable = (ClassSymTable)argu;
		String currClassName = currClassTable.className;
		String currMethodName = n.f2.f0.toString();
		MethodSymTable currMethodTable = currClassTable.methods.get(currMethodName).methodST;
		int i = currMethodTable.params.size();
		Temp t = new Temp();
	    Put.gen(currClassName + "_" + currMethodName + " [" + (i+1) + "]" + "\n");
		//打印函数头，第一个参数为this指针
	    Put.up();
		Put.gen("\n");
		Put.gen("BEGIN\n");
		Put.up();
		n.f8.accept(this, currMethodTable);
		//语句遍历
		Put.gen("MOVE " + t + " ");
		n.f10.accept(this, currMethodTable);
		//设置返回值
		Put.down();
		Put.gen("\n");
		Put.gen("RETURN " + t);
		Put.gen("\n");
		Put.gen("END\n");
		Put.down();
		return null;
	}
	
	/**
	 * f0 -> Identifier()
	 * f1 -> "="
	 * f2 -> Expression()
	 * f3 -> ";"
	 */
	public String visit(AssignmentStatement n, Object argu) {
		String s = n.f0.f0.toString();
		MethodSymTable currMST = (MethodSymTable)argu;
		String inClass = currMST.inClass;
		ClassSymTable currCST = SymTable.classTable.get(inClass);
		//局部变量的值存放在寄存器中
		if(currMST.locals.containsKey(s)) Put.gen("MOVE " + new Temp(currMST.locals.get(s).number) + " ");
		//局部变量的值存放在寄存器中
		else{
			//向上寻找局部变量所在的类符号表
			currCST = getCST(currCST,s);
			//计算偏移量
			int offset = getOffset(currCST,s);
			//TEMP(0)存放this指针
			Put.gen("HSTORE " + new Temp(0) + " " + offset + " ");
		}
		//计算表达式的值
	    n.f2.accept(this, argu);
	    Put.gen("\n");
	    return null;
	}
	
	/**
	 * f0 -> Identifier()
	 * f1 -> "["
	 * f2 -> Expression()
	 * f3 -> "]"
	 * f4 -> "="
	 * f5 -> Expression()
	 * f6 -> ";"
	 */
	public String visit(ArrayAssignmentStatement n, Object argu) {
		String s = n.f0.f0.toString();
		MethodSymTable currMST = (MethodSymTable)argu;
		String inClass = currMST.inClass;
		ClassSymTable currCST = SymTable.classTable.get(inClass);
		Temp t = new Temp();
		Put.gen("MOVE "+ t);
		//局部变量，数组地址保存在寄存器中
		if(currMST.locals.containsKey(s)){
			int number = currMST.locals.get(s).number;
			Put.gen(" " + new Temp(number));
		}
		//成员变量，数组地址从类表指定偏移量处获取
		else{
			currCST = getCST(currCST,s);
			int offset = getOffset(currCST,s);
			Temp t2 = new Temp();
			Put.up();
			Put.gen("\n");
			Put.gen("BEGIN\n");
			Put.up();
			Put.gen("HLOAD " + t2 + " " + new Temp(0) + " " + offset + "\n");
			Put.down();
			Put.gen("RETURN " + t2 + "\n");
			Put.gen("END\n");
			Put.down();
		}
		//判断基地址>0
		Label l = new Label( "NOERROR" );
		Put.gen("CJUMP LT " + t + " 1 " + l + "\n");
		Put.gen("ERROR\n");
		Put.gen(l + " NOOP\n");
		Temp t3 = new Temp();
		//计算下标并判断是否越界
		Put.gen("MOVE " + t3 + " ");
		n.f2.accept(this, argu);
		Put.gen("\n");
		Temp t4 = new Temp();
		Put.gen("HLOAD " + t4 + " " + t + " 0\n");
		Label l2 = new Label("BEERROR");
		Put.gen("CJUMP LT " + t3 + " " + t4 + " " + l2 + "\n");
		Label l3 = new Label("NOERROR");
		Put.gen("JUMP " + l3 + "\n");
		Put.gen(l2 + " NOOP\n");
		Put.gen("ERROR\n");
		Put.gen(l3 + " NOOP\n");
		Put.gen("MOVE " + t + " PLUS " + t + " TIMES " + "4" + " PLUS " + "1 " + t3 + "\n");
		Put.gen("HSTORE " + t + " 0");
		n.f5.accept(this, argu);
		Put.gen("\n");
	    return null;
	}
	
	/**
	 * f0 -> "if"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 * f5 -> "else"
	 * f6 -> Statement()
	 */
	public String visit(IfStatement n, Object argu) {
		Temp t = new Temp();
		Label l1 = new Label("IF");
		Label l2 = new Label("IF");
		Put.gen("MOVE " + t + " ");
		n.f2.accept(this, argu);
		Put.gen("CJUMP" + t + " " + l1 + "\n");
		n.f4.accept(this, argu);
		Put.gen("JUMP " + l2 + "\n");
		Put.gen(l1 + " NOOP\n");
		n.f6.accept(this, argu);
		Put.gen(l2 + " NOOP\n");
	    return null;
	}
	
	/**
	 * f0 -> "while"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 */
	public String visit(WhileStatement n, Object argu) {
		Temp t = new Temp();
		Label l1 = new Label("WHILE");
		Label l2 = new Label("WHILE");
		Put.gen(l1 + " NOOP\n");
		Put.gen("MOVE " + t + " ");
		n.f2.accept(this, argu);
		Put.gen("CJUMP " + t + " " + l2 + "\n");
		n.f4.accept(this, argu);
		Put.gen("JUMP " + l1 + "\n");
		Put.gen(l2 + " NOOP\n");
	    return null;
	}
	
	/**
	 * f0 -> "System.out.println"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> ";"
	 */
	public String visit(PrintStatement n, Object argu) {
	    Put.gen("PRINT ");
	    n.f2.accept(this, argu);
	    return null;
	}
	
	/**
	 * f0 -> AndExpression()
	 *       | CompareExpression()
	 *       | PlusExpression()
	 *       | MinusExpression()
	 *       | TimesExpression()
	 *       | ArrayLookup()
	 *       | ArrayLength()
	 *       | MessageSend()
	 *       | PrimaryExpression()
	 */
	public String visit(Expression n, Object argu) {
	     return n.f0.accept(this, argu);
	}
	
	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "&&"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(AndExpression n, Object argu) {
		Temp t = new Temp();
		Label l1 = new Label("AND");
		Label l2 = new Label("AND");
		Put.up();
		Put.gen("BEGIN\n");
		Put.up();
		Put.gen("CJUMP ");
		n.f0.accept(this, argu);
		Put.gen(l1 + "\n");
		Put.gen("CJUMP ");
		n.f2.accept(this, argu);
		Put.gen(l1 + "\n");
		Put.gen("MOVE " + t + " 1\n");
		Put.gen("JUMP " + l2 + "\n");
		Put.gen(l1 + " NOOP\n");
		Put.gen("MOVE " + t + " 0\n");
		Put.gen(l2 + " NOOP\n");
		Put.down();
		Put.gen("RETURN " + t +"\n");
		Put.gen("END\n");
		Put.down();
		return "boolean";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "<"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(CompareExpression n, Object argu) {
		Put.gen(" LT ");
	    n.f0.accept(this, argu);
	    n.f2.accept(this, argu);
	    return "boolean";
	}
	
	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "+"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(PlusExpression n, Object argu) {
		Put.gen(" PLUS ");
	    n.f0.accept(this, argu);
	    n.f2.accept(this, argu);
	    return "int";
	}
	
	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "-"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(MinusExpression n, Object argu) {
		Put.gen(" MINUS ");
	    n.f0.accept(this, argu);
	    n.f2.accept(this, argu);
	    return "int";
	}
	
	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "*"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(TimesExpression n, Object argu) {
		Put.gen(" TIMES ");
	    n.f0.accept(this, argu);
	    n.f2.accept(this, argu);
	    return "int";
	}
	
	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "["
	 * f2 -> PrimaryExpression()
	 * f3 -> "]"
	 */
	public String visit(ArrayLookup n, Object argu) {
		MethodSymTable currMST = (MethodSymTable)argu;
		String inClass = currMST.inClass;
	    ClassSymTable currCST = SymTable.classTable.get(inClass);
	    Put.up();
		Put.gen("\n");
		Put.gen("BEGIN\n");
		Put.up();
	    Temp t = new Temp();
	    Put.gen("MOVE " + t );
		// 获取数组名称
	    String arrayName = n.f0.accept(this, argu);
	    String cName = "";
		int index = arrayName.indexOf(".");
		if(index >= 0) {
			cName = arrayName.substring(0,index);
			arrayName = arrayName.substring(index + 1, arrayName.length());			 
		}
		//判断基地址>0
		Label l = new Label("NOERROR");
		Put.gen("CJUMP LT " + t + " 1 " + l + "\n");
		Put.gen("ERROR\n");
		Put.gen(l + " NOOP\n");
		Temp t3 = new Temp();
		//计算下标并判断是否越界
		Put.gen("MOVE " + t3 + " ");
		n.f2.accept(this, argu);
		Put.gen("\n");
		Temp t4 = new Temp();
		Put.gen("HLOAD " + t4 + " " + t + " 0\n");
		Label l2 = new Label("BEERROR");
		Put.gen("CJUMP LT " + t3 + " " + t4 + " " + l2 + "\n");
		Label l3 = new Label("NOERROR");
		Put.gen("JUMP " + l3 + "\n");
		Put.gen(l2 + " NOOP\n");
		Put.gen("ERROR\n");
		Put.gen(l3 + " NOOP\n");
		Put.gen("MOVE " + t + " PLUS " + t + " TIMES " + "4" + " PLUS " + "1 " + t3 + "\n");
		Put.gen("HLOAD " + t + " " + t + " 0\n");
		Put.down();
		Put.gen("RETURN " + t + "\n");
		Put.gen("END");
		Put.down();
	    return cName;
    }
	 
	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> "length"
	 */
	public String visit(ArrayLength n, Object argu) {
		MethodSymTable currMST = (MethodSymTable)argu;
		String inClass = currMST.inClass;
		ClassSymTable currCST = SymTable.classTable.get(inClass);
		Put.up();
		Put.gen("\n");
		Put.gen("BEGIN\n");
		Put.up();
		Temp t = new Temp();
		Put.gen("MOVE " + t);
		String arrayName = n.f0.accept(this, argu);
		int index = arrayName.indexOf(".");
		if(index >= 0) arrayName = arrayName.substring(index + 1,arrayName.length());
		//数组长度放在偏移量为0的地方
		Label l = new Label("NOERROR");
		Put.gen("CJUMP LT " + t + " 1 " + l + "\n");
		Put.gen("ERROR\n");
		Put.gen(l + " NOOP\n");
		Put.gen("HLOAD " + t + " " + t + " 0\n");
		Put.down();
		Put.gen("RETURN " + t +"\n");
		Put.gen("END\n");
		Put.down();
		return "int";
	} 
	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( ExpressionList() )?
	 * f5 -> ")"
	 */
	public String visit(MessageSend n, Object argu) {
		Put.gen("CALL");
	    Put.up();
		Put.gen("\n");
		Put.gen("BEGIN\n");
		Put.up();
	    Temp t = new Temp();
		//类的基地址放在t中
	    Put.gen("MOVE " + t);
	    String cName = n.f0.accept(this,argu);
	    int index = cName.indexOf(".");
	    if(index >= 0) cName = cName.substring(0,index);
	    Put.gen("\n");
	    Label l = new Label("NOERROR");
	    Put.gen("CJUMP LT " + t +" 1 " + l + "\n");
	    Put.gen("ERROR\n");
	    Put.gen(l + " NOOP\n");
	    Temp t2 = new Temp();
		//cName方法表地址
	    Put.gen("HLOAD " + t2 +" " +  t +" 0\n");
	    //方法名
	    String fName = n.f2.f0.toString();
	    //类信息表
	    ClassSymTable cTable = SymTable.classTable.get(cName);
	    //计算方法所属真正基类和偏移量 
	    ClassSymTable c = cTable;
	    int i = 0;		
	    while(c.isSonClass) {
	    	if(c.methods.containsKey(fName)) break;
	    	i += c.methods.size()*4;
	    	c = SymTable.classTable.get(c.father);    		
	    }
	    MethodInfo mInfo = c.methods.get(fName);
	    int offset = mInfo.offset + i;
	    Put.gen("HLOAD " + t2 + " " +  t2 + " " + offset + "\n");
	    Put.down();
	    Put.gen("RETURN " + t2 + "\n");
	    Put.gen("END ( " + t + " ");
	    Put.down();
	    MethodSymTable mTable = c.methods.get(fName).methodST;
	    LinkedList<Type> newl = mTable.params;
	    if(newl.size() != 0) {
	    	ExpressionList e1 = (ExpressionList)n.f4.node;
	    	String trueClassName = e1.f0.accept(this,argu);
			int tempindex = trueClassName.indexOf(".");
			if(tempindex >= 0) trueClassName = trueClassName.substring(0, tempindex);
		    i = 0;
		    if(trueClassName != get_name(newl.get(i))) {
		    	NodeToken n1 = new NodeToken(trueClassName);         
		    	NodeChoice n0 = new NodeChoice(new Identifier(n1), 3);
		    	Type temType = new Type(n0);
		    	newl.set(i,temType );
		    }
		    i++;
		    for(Enumeration<Node> e = ((ExpressionList)n.f4.node).f1.elements(); e.hasMoreElements(); i++) {
		    	String trueClassName2 = e.nextElement().accept(this,argu);
		    	tempindex = trueClassName2.indexOf(".");
			    if(tempindex >= 0) trueClassName2 = trueClassName2.substring(0, tempindex);
		    	if(trueClassName2 != get_name(newl.get(i))) {
					NodeToken n1 = new NodeToken(trueClassName2);         
					NodeChoice n0 = new NodeChoice(new Identifier(n1), 3);
					Type temType = new Type(n0);
					newl.set(i,temType);
				}
		    }
	    }	   
	    Put.gen(" )\n");
	    return mInfo.retVal;
	}

	/**
	 * f0 -> Expression()
	 * f1 -> ( ExpressionRest() )*
	 */
	public String visit(ExpressionList n, Object argu) {
		n.f0.accept(this, argu);
		Put.gen(" ");
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> ","
	 * f1 -> Expression()
	 */
	public String visit(ExpressionRest n, Object argu) {
	    String cName = n.f1.accept(this, argu);
	    Put.gen(" ");
	    return cName;
	}

	/**
	 * f0 -> IntegerLiteral()
	 *       | TrueLiteral()
	 *       | FalseLiteral()
	 *       | Identifier()
	 *       | ThisExpression()
	 *       | ArrayAllocationExpression()
	 *       | AllocationExpression()
	 *       | NotExpression()
	 *       | BracketExpression()
	 */
	public String visit(PrimaryExpression n, Object argu) {
		return n.f0.accept(this,argu);
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n, Object argu) {
		Put.gen(" " + n.f0.toString() + " ");
	    return "int";
	}

	/**
	 * f0 -> "true"
	 */
	public String visit(TrueLiteral n, Object argu) {
		Put.gen(" 1 ");
		return "boolean";
	}

	/**
	 * f0 -> "false"
	 */
	public String visit(FalseLiteral n, Object argu) {
		Put.gen(" 0 ");
		return "boolean";
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Identifier n, Object argu) {
		String cName = "";
		String s = n.f0.toString();
		MethodSymTable MST = (MethodSymTable)argu;
		String inClass = MST.inClass;
		if(MST.locals.containsKey(s)) {
			int number = MST.locals.get(s).number;
			if(number < 20)	cName = get_name(MST.params.get(number-1));
			else cName = MST.locals.get(s).cName;
			Put.gen(" " + new Temp(number));
		}
		//类成员变量
		else {
			//向上寻找包含该变量的父类
			ClassSymTable CST = SymTable.classTable.get(inClass);
			while(true){
				if(CST.field.containsKey(inClass + "." + s)){
					cName = CST.field.get(inClass + "." + s).cName;
					break;
				}
				else {
					if(CST.isSonClass){
						CST = SymTable.classTable.get(CST.father);
						inClass = CST.className;
					}
					else break;
				}
			}			   
			int offset = CST.field.get(inClass+"."+s).offset;
			while(CST.isSonClass){
				CST = SymTable.classTable.get(CST.father);
				offset += CST.field.size() * 4;
			}
			Temp tt = new Temp();
			Put.up();
			Put.gen("\n" + "BEGIN\n");
			Put.up();
			Put.gen("HLOAD " + tt + " " + new Temp(0) + " " + offset + "\n");
			Put.down();
			Put.gen("RETURN " + tt + "\n");
			Put.gen("END\n");
			Put.down();
		}
		return cName + "." + s;
	}

	/**
	 * f0 -> "this"
	 */
	public String visit(ThisExpression n, Object argu) {
		Put.gen(" TEMP 0\n");
		MethodSymTable MST = (MethodSymTable)argu;
		String inClass = MST.inClass;
		return inClass;
	}
	
	/**
	 * f0 -> "new"
	 * f1 -> "int"
	 * f2 -> "["
	 * f3 -> Expression()
	 * f4 -> "]"
	 */
	public String visit(ArrayAllocationExpression n, Object argu) {
		Temp t = new Temp();
		Put.up();
		Put.gen("\n" + "BEGIN\n");
		Put.up();
		Put.gen("MOVE " + t + " ");
		n.f3.accept(this,argu);
		Put.gen("\n");
		Temp t1 = new Temp();
		Put.gen("MOVE " + t1 +" HALLOCATE TIMES 4 PLUS " + t + " 1\n");
		Put.gen("HSTORE " + t1 + " 0 " + t + "\n");
		Temp t2 = new Temp();
		Put.gen("MOVE " + t2 + " 0\n");
		Label l = new Label("NOERROR");
		Label l1 = new Label("NOERROR");
		Put.gen(l + " NOOP\n");
		Put.gen("CJUMP LT "+ t2 + " " + t + " " + l1 + "\n");
		Temp t3 = new Temp();
		Put.gen("MOVE " +  t3 + " PLUS " + t1 + " TIMES 4 PLUS 1 " + t2 + "\n");
		Put.gen("HSTORE " + t3 + " 0 0\n");
		Put.gen("MOVE " + t2 + " PLUS " + t2 + " 1\n");
		Put.gen("JUMP " + l + "\n");
		Put.gen(l1 + " NOOP\n");
		Put.down();
		Put.gen("RETURN " + t1 + "\n");
		Put.gen("END\n");
		Put.down();
		return "null";
	}
	   
	/**
	 * f0 -> "new"
	 * f1 -> Identifier()
	 * f2 -> "("
	 * f3 -> ")"
	 */
	public String visit(AllocationExpression n,Object argu) {
		Temp t = new Temp();
		String cName = n.f1.f0.toString();
		ClassSymTable currClassTable = SymTable.classTable.get(cName);
		Put.up();
		Put.gen("\n" + "BEGIN\n");
		Put.up();
		int Mnumber = 0;
		int Tnumber = 0;
		ClassSymTable c = currClassTable;
		while(c.father !=null)
		{
			Mnumber += c.methods.size();
			Tnumber += c.field.size();
			c = SymTable.classTable.get(c.father);
		}
		Mnumber += c.methods.size();
		Tnumber += c.field.size();
		Put.gen("MOVE " + t + " HALLOCATE " + Mnumber * 4 + "\n");//???????????
		   
		int i = 0;
		c = currClassTable;
		ClassSymTable tempTable = c;
		do {
			tempTable = c;
			for(String s:c.methods.keySet())
			{
				Put.gen("HSTORE " + t + " " + i*4 + " " + c.className +"_" + s + "\n");
				System.out.println();                     
				i++;
			}
			if(c.isSonClass) c = SymTable.classTable.get(c.father);
		} while(tempTable.isSonClass);
		Temp t2 = new Temp();
		
		int numVar = Tnumber;
		 
		Put.gen("MOVE " + t2 + " HALLOCATE " + " " + ( ( numVar + 1 )* 4 )+"\n");
		
		for(i = 1;i <= numVar;i++)
		{
			Put.gen("HSTORE " + t2 + " " + i*4 +" " + 0 + "\n");
		}
		Put.gen("HSTORE " + t2 + " 0 " + t + "\n");
		Put.down();
		Put.gen("RETURN " + t2 + "\n");
		Put.gen("END\n");
		Put.down();
		return n.f1.f0.toString();
	}
	
	/**
	 * f0 -> "!"
	 * f1 -> Expression()
	 */
	public String visit(NotExpression n, Object argu) {
		Temp t = new Temp();
		Label l1 = new Label("NOT");
		Label l2 = new Label("NOT");
		Put.up();
		Put.gen("BEGIN\n");
		Put.up();
		Put.gen("CJUMP ");
		n.f1.accept(this,argu);
		Put.gen(" " + l1);
		Put.gen("MOVE " + t + " 0 \n");
		Put.gen("JUMP " + l2 + "\n");
		Put.gen(l1 + " NOOP\n");
		Put.gen("MOVE " + t + " 1 \n");
		Put.gen(l2 + " NOOP\n");
		Put.down();
		Put.gen("RETURN " + t + "\n");
		Put.gen("END\n");
		Put.down();
		return null;
	}

	/**
	 * f0 -> "("
	 * f1 -> Expression()
	 * f2 -> ")"
	 */
	public String visit(BracketExpression n, Object argu) {
		return n.f1.accept(this,argu);
	}

	//从以c为类表的类开始往上找，直到找到包含变量key的父类
	public ClassSymTable getCST(ClassSymTable c, String key) {
		ClassSymTable cc = new ClassSymTable("");
		if(c.field.containsKey(c.className + "." + key)) cc = c;
		else {
			while(c.isSonClass) {
				c = SymTable.classTable.get(c.father);
				if(c.field.containsKey(c.className + "." + key)) {
					cc = c;
					break;
				}
			}
		}
		return cc;		
	}
	
	//计算变量key在类表c中的偏移量（变量已经在类表中找到）
	public int getOffset(ClassSymTable c,String key){
		int offset = c.field.get(c.className + "." + key).offset;
		while(c.isSonClass){
			c = SymTable.classTable.get(c.father);
			offset += c.field.size()*4;
		}
		return offset;		
	}
}
