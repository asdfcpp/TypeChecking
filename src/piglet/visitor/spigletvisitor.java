package piglet.visitor;

import java.util.Vector;

import piglet.p2s.Put;
import piglet.p2s.Tmp;
import piglet.syntaxtree.*;
import piglet.visitor.GJDepthFirst;

public class spigletvisitor extends GJDepthFirst<String,Object> {
	
	public spigletvisitor(String s, int num) {
		Tmp.num = num;
	}
	
	/**
	 * Grammar production:
	 * f0 -> "MAIN"
	 * f1 -> StmtList()
	 * f2 -> "END"
	 * f3 -> ( Procedure() )*
	 * f4 -> <EOF>
	 */
	public String visit(Goal n, Object argu) {
		Put.gen("MAIN\n");
		n.f1.accept(this, argu);
		Put.gen("END\n");
		n.f3.accept(this, argu);
		Put.gen("\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> ( ( Label() )? Stmt() )*
	 */
	public String visit(StmtList n, Object argu) {
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> StmtExp()
	 */
	public String visit(Procedure n, Object argu) {
		n.f0.accept(this, argu);
		Put.gen("[" + n.f2.f0.toString() + "]\n");
		Put.gen("BEGIN\n");
		Put.gen("RETURN " + n.f4.accept(this, argu) + "\n");
		Put.gen("END\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> NoOpStmt()
	 *       | ErrorStmt()
	 *       | CJumpStmt()
	 *       | JumpStmt()
	 *       | HStoreStmt()
	 *       | HLoadStmt()
	 *       | MoveStmt()
	 *       | PrintStmt()
	 */
	public String visit(Stmt n, Object argu) {
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> "NOOP"
	 */
	public String visit(NoOpStmt n, Object argu) {
		Put.gen("NOOP\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> "ERROR"
	 */
	public String visit(ErrorStmt n, Object argu) {
		Put.gen("ERROR\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> "CJUMP"
	 * f1 -> Exp()
	 * f2 -> Label()
	 */
	public String visit(CJumpStmt n, Object argu) {
		Tmp t = new Tmp();
		Put.gen("MOVE " + t + " " + n.f1.accept(this, argu) + "\n");
		Put.gen("CJUMP " + t + " ");
		n.f2.accept(this, argu);
		Put.gen("\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public String visit(JumpStmt n, Object argu) {
		Put.gen("JUMP ");
		n.f1.accept(this, argu);
		Put.gen("\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> "HSTORE"
	 * f1 -> Exp()
	 * f2 -> IntegerLiteral()
	 * f3 -> Exp()
	 */
	public String visit(HStoreStmt n, Object argu) {
		String s = n.f1.accept(this, argu);
		if(n.f1.f0.which != 4) {
			Tmp t = new Tmp();
			Put.gen("MOVE " + t + " " + s + "\n");
			s = t.toString();
		}
		Tmp t = new Tmp();
		if(n.f3.f0.which == 6) { 
			Put.gen("MOVE " + t + " ");
			Put.gen(n.f3.accept(this, argu));
			Put.gen("\n");
		}
		else Put.gen("MOVE " + t + " " + n.f3.accept(this, argu) + "\n");
		Put.gen("HSTORE " + s + " " + n.f2.f0.toString() + " " + t + "\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> "HLOAD"
	 * f1 -> Temp()
	 * f2 -> Exp()
	 * f3 -> IntegerLiteral()
	 */
	public String visit(HLoadStmt n, Object argu) {
		if(n.f2.f0.which != 4) { // need to check if it's already a temp
			Tmp t = new Tmp();
			Put.gen("MOVE " + t + " " + n.f2.accept(this, argu) + "\n");
			Put.gen("HLOAD " + n.f1.accept(this, argu) + " " + t.toString() +  " " + n.f3.f0.toString() + "\n");
		}
		else Put.gen("HLOAD " + n.f1.accept(this, argu) + " " + n.f2.accept(this, argu) +  " " + n.f3.f0.toString() + "\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> "MOVE"
	 * f1 -> Temp()
	 * f2 -> Exp()
	 */
	public String visit(MoveStmt n, Object argu) {
		Put.gen("MOVE " + n.f1.accept(this, argu) + " " + n.f2.accept(this, argu) + "\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> "PRINT"
	 * f1 -> Exp()
	 */
	public String visit(PrintStmt n, Object argu) {
		if(n.f1.f0.which > 3) { // need to check if it need > 1 lines
			Tmp t = new Tmp();
			Put.gen("MOVE " + t + " " + n.f1.accept(this, argu) + "\n");
			Put.gen("PRINT " + t.toString() + "\n");
		}
		else Put.gen("PRINT " + n.f1.accept(this, argu) + "\n");
		return null;
	}

	/**
	 * Grammar production:
	 * f0 -> StmtExp()
	 *       | Call()
	 *       | HAllocate()
	 *       | BinOp()
	 *       | Temp()
	 *       | IntegerLiteral()
	 *       | Label()
	 */
	public String visit(Exp n, Object argu) {
		return n.f0.accept(this, argu);
	}

	/**
	 * Grammar production:
	 * f0 -> "BEGIN"
	 * f1 -> StmtList()
	 * f2 -> "RETURN"
	 * f3 -> Exp()
	 * f4 -> "END"
	 */
	public String visit(StmtExp n, Object argu) {
		n.f1.accept(this,argu);
		return n.f3.accept(this,argu);
	}

	/**
	 * Grammar production:
	 * f0 -> "CALL"
	 * f1 -> Exp() // should be simple-exp
	 * f2 -> "("
	 * f3 -> ( Exp() )*
	 * f4 -> ")"
	 */
	public String visit(Call n, Object argu) {
		Tmp t = new Tmp();
		Put.gen("MOVE " + t + " " + "CALL " + n.f1.accept(this, argu) + "(" + n.f3.accept(this, argu) + ")\n");
		return t.toString();
	}

	/**
	 * Represents an optional grammar list, e.g. ( A )*
	 */
	public String visit(NodeListOptional n, Object argu) {
		int i = 0;
		Vector<Node> nodeArray = n.nodes;
		String s = "", str;
		for(i = 0; i < nodeArray.size(); ++i) {
			str = nodeArray.get(i).accept(this,argu);
			if(str != null && !str.startsWith("TEMP ")) {
				Tmp t = new Tmp();
				Put.gen("MOVE " + t + " " + str + "\n");
				str = t.toString();
			}
			if(i > 0) s += " ";
			s += str;
		}
		return s;
	}

	/**
	 * Grammar production:
	 * f0 -> "HALLOCATE"
	 * f1 -> Exp()
	 */
	public String visit(HAllocate n, Object argu) {
		if(n.f1.f0.which == 3) {
			Tmp t  = new Tmp();
			Put.gen("MOVE " + t + " " + n.f1.accept(this,argu) + "\n");
			return "HALLOCATE " + t + "\n";
		}
		else return "HALLOCATE " + n.f1.accept(this,argu) + "\n";
	}

	/**
	 * Grammar production:
	 * f0 -> Operator()
	 * f1 -> Exp() shuold be a temp
	 * f2 -> Exp() should be a simple-exp
	 */
	public String visit(BinOp n, Object argu) {
		String s1 = n.f1.accept(this, argu), s2 = n.f2.accept(this, argu);
		if(n.f1.f0.which != 4) {
			Tmp t = new Tmp();
			Put.gen("MOVE " + t + " " + s1 + "\n");
			s1 = t.toString();
		}
		if(n.f2.f0.which > 3) {
			Tmp t = new Tmp();
			Put.gen("MOVE " + t + " " + s2 + "\n");
			s2 = t.toString();
		}
		return (n.f0.accept(this, argu) + " " + s1 + " " + s2 + "\n");
	}

	/**
	 * Grammar production:
	 * f0 -> "LT"
	 *       | "PLUS"
	 *       | "MINUS"
	 *       | "TIMES"
	 */
	public String visit(Operator n, Object argu) {
		return n.f0.choice.toString();
	}

	/**
	 * Grammar production:
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public String visit(Temp n, Object argu) {
		return "TEMP " + n.f1.f0.toString();
	}

	/**
	 * Grammar production:
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n, Object argu) {
		return n.f0.toString();
	}

	/**
	 * Grammar production:
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Label n, Object argu) {
		Put.gen(n.f0.toString() +  " ");
		return "";
	}
}
