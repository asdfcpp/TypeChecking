import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Stack;

//import symboltable.*;
import syntaxtree.*;
import visitor.*;

abstract class TypeCheckingException {
	int Line;
	String error_message;
	TypeCheckingException(int line, String error_m) {
		Line = line;
		error_message = error_m;
	}
	abstract void print_message();
}

class NoDefException extends TypeCheckingException {
	NoDefException(int line, String error_m) {
		super(line, error_m);
	}
	void print_message() {
		System.out.println("Not Defined Error in line " + Line + "; " + error_message);
	}
}

class TypeMismatchException extends TypeCheckingException {
	TypeMismatchException(int line, String error_m) {
		super(line, error_m);
	}
	void print_message() {
		System.out.println("Type Not Matched in line " + Line + "; " + error_message);
	}
}

class InheritException extends TypeCheckingException {
	InheritException(int line, String error_m) {
		super(line, error_m);
	}
	void print_message() {
		System.out.println("Round Inherition in line " + Line + "; " + error_message);
	}
}

class ParamMismatchException extends TypeCheckingException {
	ParamMismatchException(int line, String error_m) {
		super(line, error_m);
	}
	void print_message() {
		System.out.println("Param Number Not Match in line " + Line + "; " + error_message);
	}
}

class Environment {
	String class_env;
	String method_env;
}

public class TypeCheckVis extends GJNoArguDepthFirst<Type> {
	
	SymTable symbolTable;
	Stack<Environment> Variable_env;
	final static int IDENTIFIER = 0, BOOLEAN = 1, INTEGER = 2, ARRAY = 3;
	final static String TYPE[] = {"IDENTIFIER", "BOOLEAN", "INTEGER", "ARRAY"};
	public boolean error = false;
	
	public TypeCheckVis(SymTable m) {
		symbolTable = m;
		Variable_env = new Stack<Environment>();
	}
	
	public String get_name(Type x) {
		if(x == null) return null;
		if(x.f0.which > 0 && x.f0.which <= 3) return TYPE[x.f0.which];
		if(x.f0.which == 0) return ((Identifier)x.f0.choice).f0.toString();
		return "???";
	}
	
	public boolean type_match(Type x, int p, Type y) {
		// 2 parameters, between type and int
		if(p != -1) {
			if(x == null) {
				if(y == null) return true;
				return y.f0.which == p;
			}
			if(y == null) return x.f0.which == p;
		}
		// 2 parameters, between types
		else {
			if(x == null || y == null) return true;
			int x0 = x.f0.which;
			int y0 = y.f0.which;
			if(x0 != y0) return false;
			if(x0 != 0) return true;
			return get_name(x).equals(get_name(y));
		}
		// 3 parameters
		int x0 = x.f0.which;
		int y0 = y.f0.which;
		if(x0 != y0) return false;
		if(x0 == p && p != 0) return true; // except identifier
		// x and y are both identifier
		return get_name(x).equals(get_name(y));
	}
	
	public boolean is_ancestor(Type x, Type y) {
		if(x.f0.which != IDENTIFIER || y.f0.which != IDENTIFIER) return false;
		int count = 1;
		String x0 = ((Identifier)(x.f0.choice)).f0.toString();
		String y0 = ((Identifier)(y.f0.choice)).f0.toString();
		while(count <= symbolTable.classTable.size()) {
			if(symbolTable.classTable.get(y0).isSonClass == true) {
				if(symbolTable.classTable.get(y0).father.equals(x0)) return true;
				else y0 = symbolTable.classTable.get(y0).father;
			}
			else return false;
			++count;
		}
		return false;
	}
	
	boolean declared(Type x)
	{
		if(x == null || x.f0.which < 3) return true;
		else {
			Identifier n = ((Identifier)(x.f0.choice));
			if(!symbolTable.classTable.containsKey(n.f0.toString())) {
				String error_m = "Name: " + n.f0.toString();
				NoDefException ex = new NoDefException(n.f0.beginLine, error_m);
				ex.print_message();
				error = true;
				return false;
			}
			return true;
		}
	}
	
	boolean searchkey(LinkedHashMap<String, TypeInfo> linkmap, Type x, Identifier n, String str) {
		if(linkmap.containsKey(str)) {
			x.f0 = linkmap.get(str).typeVal.f0;
			if(declared(x)) return true;
			else {
				x.f0 = null;
				String error_m = "Name: " + n.f0.toString();
				NoDefException ex = new NoDefException(n.f0.beginLine, error_m);
				ex.print_message();
				error = true;
				return false;
			}
		}
		return false;		
	}
	
	public MethodSymTable visitMethod(Identifier n) {
		String str = n.f0.toString();
		Environment env = Variable_env.peek();
		int count = 0;
		Type x;
		MethodSymTable mst;
		LinkedHashMap<String, MethodInfo> linkmap;
		linkmap = symbolTable.classTable.get(env.class_env).methods;
		if(linkmap.containsKey(str)) {
			mst = linkmap.get(str).methodST;
			x = linkmap.get(str).methodST.retVal;
			if(declared(x)) return mst;
			else {
				String error_m = "Return Value of method "+ str;
				NoDefException ex = new NoDefException(n.f0.beginLine, error_m);
				ex.print_message();
				error = true;
				return null;			
			}
		}
		boolean found = false;
		while(!found && count < symbolTable.classTable.size()) {
			ClassSymTable cst = symbolTable.classTable.get(env.class_env);
			if(cst.isSonClass == true) {
				env = new Environment();
				env.class_env = cst.father;
			}
			else break;
			if(!symbolTable.classTable.containsKey(env.class_env)) return null;
			linkmap = symbolTable.classTable.get(env.class_env).methods;
			if(linkmap.containsKey(str)) {
				found = true;
				mst = linkmap.get(str).methodST;
				x = linkmap.get(str).methodST.retVal;
				if(declared(x)) return mst;
				else {
					String error_m = "Return Value of method "+ str;
					NoDefException ex = new NoDefException(n.f0.beginLine, error_m);
					ex.print_message();
					error = true;
					return null;
				}
			}
			++count;
		}
		if(found == false) {
			String error_m = "Method "+ str;
			NoDefException ex = new NoDefException(n.f0.beginLine, error_m);
			ex.print_message();
			error = true;
			return null;
		}
		return null;		
	}
	
	/**
	 * Grammar production:
	 * f0 -> "new"
	 * f1 -> Identifier()
	 * f2 -> "("
	 * f3 -> ")"
	 */
	public Type visit(AllocationExpression n) { // need to check
		if(!symbolTable.classTable.containsKey(n.f1.f0.toString())) {
			String error_m = "Name: " + n.f1.f0.toString();
			NoDefException ex = new NoDefException(n.f0.beginLine, error_m);
			ex.print_message();
			error = true;
			return null;
		}
		NodeChoice NC = new NodeChoice(new Identifier(new NodeToken(n.f1.f0.toString())), IDENTIFIER);
		return new Type(NC);
	}
	
	/**
	 * Grammar production:
	 * f0 -> PrimaryExpression()
	 * f1 -> "&&"
	 * f2 -> PrimaryExpression()
	 */
	public Type visit(AndExpression n) {
		Type F0 = n.f0.accept(this);
		Type F2 = n.f0.accept(this);
		if(!type_match(F0, BOOLEAN, F2)) {
			String error_m = "operators of && must be BOOLEAN type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.beginLine, error_m);
			ex.print_message();
			error = true;
			return null;
		}
		NodeChoice NC = new NodeChoice(new BooleanType(), BOOLEAN);
		return new Type(NC);
	}
	
	/**
	 * Grammar production:
	 * f0 -> "new"
	 * f1 -> "int"
	 * f2 -> "["
	 * f3 -> Expression()
	 * f4 -> "]"
	 */
	public Type visit(ArrayAllocationExpression n) {
		Type F3 = n.f3.accept(this);
		if(!type_match(F3, INTEGER, null)) {
			String error_m = "Index in Array Allocation must be INTEGER";
			TypeMismatchException ex = new TypeMismatchException(n.f0.beginLine, error_m);
			ex.print_message();
			error = true;
			return null;
		}
		NodeChoice NC = new NodeChoice(new ArrayType(), ARRAY);
		return new Type(NC);
	}
	
	/**
	 * Grammar production:
	 * f0 -> Identifier()
	 * f1 -> "["
	 * f2 -> Expression()
	 * f3 -> "]"
	 * f4 -> "="
	 * f5 -> Expression()
	 * f6 -> ";"
	 */
	public Type visit(ArrayAssignmentStatement n) {
		Type F0 = n.f0.accept(this);
		Type F2 = n.f2.accept(this);
		Type F5 = n.f5.accept(this);
		if(!type_match(F0, ARRAY, null)) {
			String error_m = "Array Assignment must start by ARRAY type";
			TypeMismatchException ex = new TypeMismatchException(n.f0.f0.beginLine, error_m);
			ex.print_message();
			error = true;
		}
		if(!type_match(F2, INTEGER, null)) {
			String error_m = "Array Index must be INTEGER type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.endLine, error_m);
			ex.print_message();
			error = true;
		}
		if(!type_match(F5, INTEGER, null)) {
			String error_m = "Elements in Array must be INTEGER type";
			TypeMismatchException ex = new TypeMismatchException(n.f4.endLine, error_m);
			ex.print_message();
			error = true;
		}
		return null;
	}
	
	/**
	 * Grammar production:
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> "length"
	 */
	public Type visit(ArrayLength n) {
		Type F0 = n.f0.accept(this);
		if(!type_match(F0, ARRAY, null)) {
			String error_m = "ArrayLength is meaningful only when operator is ARRAY type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.beginLine, error_m);
			ex.print_message();
			error = true;
			return null;
		}
		NodeChoice NC = new NodeChoice(new IntegerType(), INTEGER);
		return new Type(NC);
	}
	
	/**
	 * Grammar production:
	 * f0 -> PrimaryExpression()
	 * f1 -> "["
	 * f2 -> PrimaryExpression()
	 * f3 -> "]"
	 */
	public Type visit(ArrayLookup n) {
		Type F0 = n.f0.accept(this);
		Type F2 = n.f2.accept(this);
		if(!type_match(F0, ARRAY, null)) {
			String error_m = "Only ARRAY type can be indexed";
			TypeMismatchException ex = new TypeMismatchException(n.f1.beginLine, error_m);
			ex.print_message();
			error = true;
			return null;
		}
		if(!type_match(F2, INTEGER, null)) {
			String error_m = "Array Index must be INTEGER type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.beginLine, error_m);
			ex.print_message();
			error = true;
			return null;
		}
		NodeChoice NC = new NodeChoice(new IntegerType(), INTEGER);
		return new Type(NC);
	}
	
	/**
	 * Grammar production:
	 * f0 -> Identifier()
	 * f1 -> "="
	 * f2 -> Expression()
	 * f3 -> ";"
	 */
	public Type visit(AssignmentStatement n)
	{
		Type F0 = n.f0.accept(this);
		Type F2 = n.f2.accept(this);
		if(type_match(F0, -1, F2) || is_ancestor(F0, F2)) return null;
		else {
			String error_m = "Cannot Assign " + get_name(F2) + " to " + get_name(F0);
			TypeMismatchException ex = new TypeMismatchException(n.f0.f0.beginLine, error_m);
			ex.print_message();	
			error = true;
			return null;			
		}
	}
	
	/**
	 * Grammar production:
	 * f0 -> "("
	 * f1 -> Expression()
	 * f2 -> ")"
	 */	
	public Type visit(BracketExpression n) {
		return n.f1.accept(this);
	}
	
	/**
	 * Grammar production:
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
	 * f5 -> "}"
	 */
	public Type visit(ClassDeclaration n) {
		Environment env = new Environment();
		env.class_env = n.f1.f0.toString();
		Variable_env.push(env);	
	    n.f4.accept(this);	    
	    Variable_env.pop();
	    return null;
	}
	
	/**
	 * Grammar production:
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "extends"
	 * f3 -> Identifier()
	 * f4 -> "{"
	 * f5 -> ( VarDeclaration() )*
	 * f6 -> ( MethodDeclaration() )*
	 * f7 -> "}"
	 */
	public Type visit(ClassExtendsDeclaration n) {
		String f_name = n.f3.f0.toString();
		String s_name = n.f1.f0.toString();
		if(!symbolTable.classTable.containsKey(f_name)) {
			String error_m = "Father Class Name: " + f_name;
			NoDefException ex = new NoDefException(n.f3.f0.beginLine, error_m);
			ex.print_message();	
			error = true;
			return null;
		}
		symbolTable.classTable.get(f_name).son = s_name;
		symbolTable.classTable.get(s_name).father = f_name;
		boolean round = true;
		String f_name_copy = n.f3.f0.toString();
		while(f_name != s_name) {
			String f_f_name = symbolTable.classTable.get(f_name).father;
			if(f_f_name != null) f_name = f_f_name;
			else {
				round = false;
				break;
			}
		}
		if(round) {
			String error_m = "Class " + f_name_copy + "is related to Round Inherition";
			InheritException ex = new InheritException(n.f3.f0.beginLine, error_m);
			ex.print_message();	
			error = true;
		}
		Environment env = new Environment();
		env.class_env = n.f1.f0.toString();
		Variable_env.push(env);	
		n.f6.accept(this);
		Variable_env.pop();
		return null;			
	}
	
	/**
	 * Grammar production:
	 * f0 -> PrimaryExpression()
	 * f1 -> "<"
	 * f2 -> PrimaryExpression()
	 */
	public Type visit(CompareExpression n) {
		Type F0 = n.f0.accept(this);
		Type F2 = n.f2.accept(this);
		if(!type_match(F0, INTEGER, F2)) {
			String error_m = "operators of < must be INTEGER type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.beginLine, error_m);
			ex.print_message();
			error = true;
		}
		NodeChoice NC = new NodeChoice(new BooleanType(), BOOLEAN);
		return new Type(NC);
	}
	
	/**
	 * Grammar production:
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
	public Type visit(Expression n)	{
		return n.f0.accept(this);
	}
	
	/**
	 * Grammar production:
	 * f0 -> ","
	 * f1 -> Expression()
	 */
	public Type visit(ExpressionRest n) {
	   return n.f1.accept(this);
	}
	
	/**
	 * Grammar production:
	 * f0 -> "false"
	 */
	public Type visit(FalseLiteral n) {
		NodeChoice NC = new NodeChoice(new BooleanType(), BOOLEAN);
		return new Type(NC);	
	}
	
	/**
	 * Grammar production:
	 * f0 -> Type()
	 * f1 -> Identifier()
	 */
	public Type visit(FormalParameter n) {
		return null;
	}
	
	/**
	 * Grammar production:
	 * f0 -> <IDENTIFIER>
	 */
	public Type visit(Identifier n) {
		Environment env = Variable_env.peek();
		String ss = env.class_env;
		int count = 0;
		LinkedHashMap<String, TypeInfo> linkmap;
		linkmap = symbolTable.classTable.get(env.class_env).methods.get(env.method_env).methodST.locals;		
		Type t = new Type(new NodeChoice(new NodeToken("")));
		if(searchkey(linkmap, t, n, n.f0.toString()) == true) return t;
		else {
			linkmap = symbolTable.classTable.get(env.class_env).field;
			if(searchkey(linkmap, t, n, env.class_env + "." + n.f0.toString()) == true) return t;
			else {
				boolean found = false;
				ClassSymTable p;
				while(found == false && count < symbolTable.classTable.size()) {
					p = symbolTable.classTable.get(env.class_env);
					if(!symbolTable.classTable.containsKey(env.class_env)) return null;
					linkmap = p.field;
					if(searchkey(linkmap, t, n, env.class_env + "." + n.f0.toString()) == true) {
						found = true;
						return t;
					}
					if(p.isSonClass) env.class_env = p.father;
					else break;
					++count;
				}
				env.class_env = ss;
				if(found == false) {
					String error_m = "Variable " + n.f0.toString() + " not found";
					NoDefException ex = new NoDefException(n.f0.beginLine, error_m);
					ex.print_message();
					error = true;
				}
				return null;
			}			
		}
	}
	
	/**
	 * Grammar production:
	 * f0 -> "if"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 * f5 -> "else"
	 * f6 -> Statement()
	 */
	public Type visit(IfStatement n) {
		Type F2 = n.f2.accept(this);
		if(!type_match(F2, BOOLEAN, null)) {
			String error_m = "Condition in If Statement must be BOOLEAN type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.endLine, error_m);
			ex.print_message();	
			error = true;
		}
		n.f4.accept(this);
		n.f6.accept(this);
		return null;
	}
	
	/**
	 * Grammar production:
	 * f0 -> <INTEGER_LITERAL>
	 */
	public Type visit(IntegerLiteral n) {
		NodeChoice NC = new NodeChoice(new IntegerType(), INTEGER);
		return new Type(NC);		
	}
	
	/**
	 * Grammar production:
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
	 * f14 -> ( VarDeclaration() )*
	 * f15 -> ( Statement() )*
	 * f16 -> "}"
	 * f17 -> "}"
	 */
	public Type visit(MainClass n) {
		Environment env = new Environment();
		env.class_env = n.f1.f0.toString();
		Variable_env.push(env);
		n.f14.accept(this);
		Variable_env.pop();
		return null;
	}
	
	/**
	 * Grammar production:
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( ExpressionList() )?
	 * f5 -> ")"
	 */
	public Type visit(MessageSend n) // tbc
	{
		Type F0 = n.f0.accept(this);
		if(F0 == null) return null;
		else if(F0.f0.which != IDENTIFIER) {
			String error_m = "Method-Calling can only be done by a CLASS";
			TypeMismatchException ex = new TypeMismatchException(n.f1.beginLine, error_m);
			ex.print_message();
			error = true;
			return null;
		}
		else {
			Environment env = new Environment();
			env.class_env = ((Identifier)(F0.f0.choice)).f0.toString();
			Variable_env.push(env);			
			MethodSymTable mst = visitMethod(n.f2);			
			Variable_env.pop();
			if(mst == null) return null;
			else {
				Type x = mst.retVal;
				LinkedList<Type> newl = mst.params;
				if(!n.f4.present() && newl.size() == 0) return x;
				else if(n.f4.present() && newl.size() > 0) {
					ExpressionList e1 = (ExpressionList)n.f4.node;
					if(((e1).f1.nodes.size() + 1) == newl.size()) {
						Type y = (e1).f0.accept(this);
						int i = 0;
						if(!type_match(y, -1, newl.get(i)) && !is_ancestor(newl.get(i), y)) {
							String error_m = "Method: "+ mst.methodName;
							ParamMismatchException ex = new ParamMismatchException(n.f1.beginLine, error_m);
							ex.print_message();	
							error = true;
							return null;
						}
						++i;
				        for (Enumeration<Node> e = ((ExpressionList)n.f4.node).f1.elements(); e.hasMoreElements(); ++i) {
				        	Type t4 = e.nextElement().accept(this);
				        	if(!type_match(t4, -1, newl.get(i)) && !is_ancestor(newl.get(i), t4)) {
				        		String error_m = "Method: "+ mst.methodName;
				        		ParamMismatchException ex = new ParamMismatchException(n.f1.beginLine, error_m);
				        		ex.print_message();	
				        		error = true;
				        		return null;
				           }				          
				        }
				        return x;
				    }
					else {
						String error_m = "Method: "+ mst.methodName;
		        		ParamMismatchException ex = new ParamMismatchException(n.f1.beginLine, error_m);
		        		ex.print_message();	
		        		error = true;
		        		return null;
					}
				}
				else {
					String error_m = "Method: "+ mst.methodName;
	        		ParamMismatchException ex = new ParamMismatchException(n.f1.beginLine, error_m);
	        		ex.print_message();	
	        		error = true;
	        		return null;
				}
			}
		}
	}
	
	/**
	 * Grammar production:
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
	public Type visit(MethodDeclaration n) {
		Environment env = Variable_env.peek();
		env.method_env = n.f2.f0.toString();
		n.f8.accept(this);
		Type t10 = n.f10.accept(this);
		if(!type_match(n.f1, -1, t10) && !is_ancestor(n.f1, t10)) {
			String error_m = "Type of Return Value Mismatched in "+ n.f2.f0.toString();
			error_m += (": " + get_name(n.f1)+ " and " + get_name(t10));
			TypeMismatchException ex = new TypeMismatchException(n.f9.beginLine, error_m);
			ex.print_message();	
			error = true;
			return null;
		}
		
		ClassSymTable table = symbolTable.classTable.get(env.class_env);
		int count = 0;
		while(count < symbolTable.classTable.size()) {
			if(table.isSonClass) {
				env.class_env = table.father;
				table = symbolTable.classTable.get(env.class_env);
				LinkedHashMap<String, MethodInfo> linkmap0 = table.methods;
				if(linkmap0.containsKey(n.f2.f0.toString())) {
					boolean b1 = type_match(linkmap0.get(env.method_env).methodST.retVal, -1, t10);
					boolean b2 = is_ancestor(linkmap0.get(env.method_env).methodST.retVal, t10);
					if(!b1 && !b2) {
						String error_m = "Type of Return Value Mismatched between class "+ n.f2.f0.toString();
						error_m += " and its ancestor";
						TypeMismatchException ex = new TypeMismatchException(n.f9.beginLine, error_m);
						ex.print_message();	
						error = true;
						return null;
					}
				}
			}
			++count;
		}
		return null;
	}
	
	/**
	 * Grammar production:
	 * f0 -> PrimaryExpression()
	 * f1 -> "-"
	 * f2 -> PrimaryExpression()
	 */
	public Type visit(MinusExpression n) {
		Type F0 = n.f0.accept(this);
		Type F2 = n.f2.accept(this);
		if(!type_match(F0, INTEGER, F2)) {
			String error_m = "operator in Minus Expr must be INTEGER type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.beginLine, error_m);
			ex.print_message();
			error = true;
		}
		NodeChoice NC = new NodeChoice(new IntegerType(), INTEGER);
		return new Type(NC);
	}
	
	/**
	 * Grammar production:
	 * f0 -> "!"
	 * f1 -> Expression()
	 */
	public Type visit(NotExpression n) {
		Type F1 = n.f1.accept(this);
		if(!type_match(F1, BOOLEAN, null)) {
			String error_m = "Expr in Not Expr must be BOOLEAN type";
			TypeMismatchException ex = new TypeMismatchException(n.f0.beginLine, error_m);
			ex.print_message();
			error = true;
			return null;
		}
		NodeChoice NC = new NodeChoice(new BooleanType(), BOOLEAN);
		return new Type(NC);		
	}
	
	/**
	 * Grammar production:
	 * f0 -> PrimaryExpression()
	 * f1 -> "+"
	 * f2 -> PrimaryExpression()
	 */
	public Type visit(PlusExpression n) {
		Type F0 = n.f0.accept(this);
		Type F2 = n.f2.accept(this);
		if(!type_match(F0, INTEGER, F2)) {
			String error_m = "operator in Add Expr must be INTEGER type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.beginLine, error_m);
			ex.print_message();
			error = true;
		}
		NodeChoice NC = new NodeChoice(new IntegerType(), INTEGER);
		return new Type(NC);
	}
	
	/**
	 * Grammar production:
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
	public Type visit(PrimaryExpression n) {
		return n.f0.accept(this);
	}
	
	/**
	 * Grammar production:
	 * f0 -> "System.out.println"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> ";"
	 */
	public Type visit(PrintStatement n) {
		Type F2 = n.f2.accept(this);
		if(!type_match(F2, INTEGER, null)) {
			String error_m = "operator in Print Expr must be INTEGER type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.endLine, error_m);
			ex.print_message();
			error = true;
		}
		return null;
	}
	
	/**
	 * Grammar production:
	 * f0 -> "this"
	 */
	public Type visit(ThisExpression n) {
		NodeToken NT = new NodeToken(Variable_env.peek().class_env);
		NodeChoice NC = new NodeChoice(new Identifier(NT), IDENTIFIER);
		return new Type(NC);	
	}
	
	/**
	 * Grammar production:
	 * f0 -> PrimaryExpression()
	 * f1 -> "*"
	 * f2 -> PrimaryExpression()
	 */
	public Type visit(TimesExpression n) {
		Type F0 = n.f0.accept(this);
		Type F2 = n.f2.accept(this);
		if(!type_match(F0, INTEGER, F2)) {
			String error_m = "operator in Times Expr must be INTEGER type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.beginLine, error_m);
			ex.print_message();
			error = true;
		}
		NodeChoice NC = new NodeChoice(new IntegerType(), INTEGER);
		return new Type(NC);
	}
	
	/**
	 * Grammar production:
	 * f0 -> "true"
	 */
	public Type visit(TrueLiteral n) {
		NodeChoice NC = new NodeChoice(new BooleanType(), BOOLEAN);
		return new Type(NC);	
	}
	
	/**
	 * Grammar production:
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 */
	public Type visit(VarDeclaration n) {
		return null;
	}
	
	/**
	 * Grammar production:
	 * f0 -> "while"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 */
	public Type visit(WhileStatement n) {
		Type F2 = n.f2.accept(this);		
		if(!type_match(F2, BOOLEAN, null)) {
			String error_m = "Condition in While Statement must be BOOLEAN type";
			TypeMismatchException ex = new TypeMismatchException(n.f1.endLine, error_m);
			ex.print_message();
			error = true;
		}
		n.f4.accept(this);
		return null;		
	}
}
