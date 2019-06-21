package kanga.k2m;
import kanga.syntaxtree.*;
import kanga.visitor.*;
public class Mipsvisitor extends   GJDepthFirst<String,Object> {
	int max_arg, a, b, c, t1, t2;
	public Mipsvisitor() {
		
	};
	/**
	 * f0 -> "MAIN"
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> "["
	 * f5 -> IntegerLiteral()
	 * f6 -> "]"
	 * f7 -> "["
	 * f8 -> IntegerLiteral()
	 * f9 -> "]"
	 * f10 -> StmtList()
	 * f11 -> "END"
	 * f12 -> ( Procedure() )*
	 * f13 -> <EOF>
	 */
	public String visit(Goal n, Object argu) {
		max_arg = Integer.parseInt(n.f8.f0.tokenImage);
		Put.up();
		Put.gen(".text\n");
		Put.gen(".globl main\n");
		Put.down();
		Put.gen("main:\n");
		Put.up();
		Put.gen("sw $fp, -8($sp)\n");
		Put.gen("move $fp $sp\n");
		
		a = Integer.parseInt(n.f2.f0.toString());
		b = Integer.parseInt(n.f5.f0.tokenImage);
		c = Integer.parseInt(n.f8.f0.toString());
		t1 = (a>4?(a-4):0);
		t2 = (c>4?(c-4):0);
		int stackLength = (b+2+t2-t1) * 4;
		Put.gen("subu $sp, $sp, " + stackLength + "\n");
		Put.gen("sw $ra, -4($fp)\n");
		n.f10.accept(this,argu);
		Put.gen("lw $ra -4($fp)\n");
		Put.gen("lw $fp -8($fp)\n");
		Put.gen("addu $sp, $sp, " + stackLength + "\n");
		Put.gen("j $ra\n");
		n.f12.accept(this,argu);
		Put.called_function();
		return null;
	}

	/**
	 * f0 -> ( ( Label() )? Stmt() )*
	 */
	public String visit(StmtList n, Object argu) {
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> "["
	 * f5 -> IntegerLiteral()
	 * f6 -> "]"
	 * f7 -> "["
	 * f8 -> IntegerLiteral()
	 * f9 -> "]"
	 * f10 -> StmtList()
	 * f11 -> "END"
	 */
	public String visit(Procedure n, Object argu) 
	{
		max_arg = Integer.parseInt(n.f8.f0.tokenImage);
		Put.gen(".text\n");
		Put.down();
		Put.gen(n.f0.f0.tokenImage + ":\n");
		Put.up();
		Put.gen("sw $fp, -8($sp)\n");
		Put.gen("move $fp $sp\n");
		b = Integer.parseInt(n.f5.f0.tokenImage);
		c = Integer.parseInt(n.f8.f0.toString());
		t1 = (a>4?(a-4):0);
		t2 = (c>4?(c-4):0);
		int stackLength = (b+2+t2-t1) * 4;
		Put.gen("subu $sp, $sp, " + stackLength + "\n");
		Put.gen("sw $ra, -4($fp)\n");
		n.f10.accept(this,argu);
		Put.gen("lw $ra -4($fp)\n");
		Put.gen("lw $fp -8($fp)\n");
		Put.gen("addu $sp, $sp, " + stackLength + "\n");
		Put.gen("j $ra\n");
		return null;
	}
	/**
	 * f0 -> NoOpStmt()
	 *       | ErrorStmt()
	 *       | CJumpStmt()
	 *       | JumpStmt()
	 *       | HStoreStmt()
	 *       | HLoadStmt()
	 *       | MoveStmt()
	 *       | PrintStmt()
	 *       | ALoadStmt()
	 *       | AStoreStmt()
	 *       | PassArgStmt()
	 *       | CallStmt()
	 */
	public String visit(Stmt n, Object argu) {
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "NOOP"
	 */
	public String visit(NoOpStmt n, Object argu) {
		Put.gen(" nop\n");
		Put.up();
		return null;
	}

	/**
     * f0 -> "ERROR"
	 */
	public String visit(ErrorStmt n, Object argu) {
	   Put.gen("jal _error\n");
	   return null;
	}

	   /**
	    * f0 -> "CJUMP"
	    * f1 -> Reg()
	    * f2 -> Label()
	    */
	public String visit(CJumpStmt n, Object argu) {
		int tempi = n.f1.f0.which;
	    Put.gen("beqz " + Regs.REGS[tempi] + ", " + n.f2.f0.toString() + "\n");
	    return null;
	}

	   /**
	    * f0 -> "JUMP"
	    * f1 -> Label()
	    */
	   public String visit(JumpStmt n, Object argu) {
	      Put.gen("j "  + n.f1.f0.toString() + "\n");
	      return null;
	   }

	   /**
	    * f0 -> "HSTORE"
	    * f1 -> Reg()
	    * f2 -> IntegerLiteral()
	    * f3 -> Reg()
	    */
	   public String visit(HStoreStmt n, Object argu) {
		   int temprs = n.f3.f0.which, temprd = n.f1.f0.which;
		   Put.gen("sw " + Regs.REGS[temprs] + ", " + n.f2.f0.toString() + "(" + Regs.REGS[temprd] + ")\n");
		   return null;
	   }

	   /**
	    * f0 -> "HLOAD"
	    * f1 -> Reg()
	    * f2 -> Reg()
	    * f3 -> IntegerLiteral()
	    */
	   public String visit(HLoadStmt n, Object argu) {
		   int temprs = n.f2.f0.which, temprd = n.f1.f0.which;
		   Put.gen("lw " + Regs.REGS[temprd] + ", " + n.f3.f0.toString() + "(" + Regs.REGS[temprs] + ")\n");
	   	   return null;
	   }

	   /**
	    * f0 -> "MOVE"
	    * f1 -> Reg()
	    * f2 -> Exp()
	    * 				/** Exp()
	    				* Grammar production:
						 * f0 -> HAllocate()
						 *       | BinOp()
						 *       | SimpleExp()
	    */
	   public String visit(MoveStmt n, Object argu) {	     
	     int temprd = n.f1.f0.which; 
		 if(n.f2.f0.which == 0) {
			   n.f2.f0.accept(this,argu);
			   Put.gen("jal _hallocs\n");
			   Put.gen("move " + Regs.REGS[temprd] + " $v0\n");
		 }
		 else if(n.f2.f0.which == 1) {
			  BinOp p = ((BinOp)n.f2.f0.choice);
			  int temprs = p.f1.f0.which;
			  String val2 = p.f2.accept(this,argu);
			  String val1 = p.accept(this,argu);
			  Put.gen(val1 + " " + Regs.REGS[temprd] + ", " + Regs.REGS[temprs] + ", " + val2 + "\n");
		 }
		 else if(n.f2.f0.which == 2) {
			 SimpleExp p = ((SimpleExp)n.f2.f0.choice);
			 int temp = p.f0.which;
			 String val = p.accept(this,argu);
			 if(temp == 0) Put.gen("move " + Regs.REGS[temprd] + " " + val + "\n");
		      if(temp == 1) {
		    	  Put.gen("li $a0 " + val + "\n");
		    	  Put.gen("move " + Regs.REGS[temprd] + " $a0\n");
		      }
		      if(temp == 2) {
		    	  Put.gen("la $a0 " + val + "\n");
		    	  Put.gen("move " + Regs.REGS[temprd] + " $a0\n");
		      }
		 }
	     return null;
	   }

	   /**
	    * f0 -> "PRINT"
	    * f1 -> SimpleExp()
	    * 					/** SimpleExp
					    * Grammar production:
					    * f0 -> Reg()
					    *       | IntegerLiteral()
					    *       | Label()
					    */
	   public String visit(PrintStmt n, Object argu) {
	      int temp = n.f1.f0.which;
	      String val = n.f1.accept(this,argu);
	      if(temp == 0) Put.gen("move $a0 " + val + "\n");
	      if(temp == 1) Put.gen("li $a0 " + val + "\n");
	      if(temp == 2) Put.gen("la $a0 " + val + "\n");
	      Put.gen("jal _printint\n");
	      return null;
	   }

	   /**
	    * f0 -> "ALOAD"
	    * f1 -> Reg()
	    * f2 -> SpilledArg()
	    */
	   public String visit(ALoadStmt n, Object argu) {
	      int temp = Integer.parseInt(n.f2.f1.f0.tokenImage) * 4;
	      int temprd = n.f1.f0.which;
	      if(temp >= max_arg*4) Put.gen("lw " + Regs.REGS[temprd] + ", " + temp + "($sp)\n");
	      else Put.gen("lw " + Regs.REGS[temprd] + ", " + temp + "($fp)\n");
	      return null;
	   }

	   /**
	    * f0 -> "ASTORE"
	    * f1 -> SpilledArg()
	    * f2 -> Reg()
	    */
	   public String visit(AStoreStmt n, Object argu) {
		   int temp = Integer.parseInt(n.f1.f1.f0.tokenImage) * 4;
		   int temprs = n.f2.f0.which;
		   if(temp >= max_arg*4) Put.gen("sw " + Regs.REGS[temprs] + ", " + temp + "($sp)\n");
		   else Put.gen("sw " + Regs.REGS[temprs] + ", " + temp + "($fp)\n");
		   return null;
	   }

	   /**
	    * f0 -> "PASSARG"
	    * f1 -> IntegerLiteral()
	    * f2 -> Reg()
	    */
	   public String visit(PassArgStmt n, Object argu) {
		   int len = Integer.parseInt(n.f1.f0.tokenImage);
		   int temprs = n.f2.f0.which;
	       Put.gen("sw " + Regs.REGS[temprs] + ", " + (len-1)*4 + "($sp)\n");
	       return null;
	   }

	   /**
	    * f0 -> "CALL"
	    * f1 -> SimpleExp()
	    */
	   public String visit(CallStmt n, Object argu) {
		  String val = n.f1.accept(this,argu);
	      if(n.f1.f0.which == 0) Put.gen("jalr " + val + "\n");
	      if(n.f1.f0.which == 1) {
	    	  Put.gen("li $a0 " + val + "\n");
	    	  Put.gen("jalr $a0\n");
	      }
	      else if(n.f1.f0.which == 2) Put.gen("jal " + val + "\n");	 
	      return null;
	   }
	   

	   /**
	    * f0 -> HAllocate()
	    *       | BinOp()
	    *       | SimpleExp()
	    */
	   public String visit(Exp n, int argu) {
		   return null;
	   }

	   /**
	    * f0 -> "HALLOCATE"
	    * f1 -> SimpleExp()
	    */
	   public String visit(HAllocate n, Object argu) {
		  String val = n.f1.accept(this,argu);
		  int temp = n.f1.f0.which;
		  if(temp == 0) Put.gen("move $a0 " + val + "\n");
		  if(temp == 1) Put.gen("li $a0 " + val + "\n");
		  if(temp == 2) Put.gen("la $a0 " + val + "\n");
		  return null;
	   }

	   /**
	    * f0 -> Operator()
	    * f1 -> Reg()
	    * f2 -> SimpleExp()
	    */
	   public String visit(BinOp n, Object argu) {
	      int temp = n.f0.f0.which;
	      if(temp == 0) return "slt";
	      if(temp == 1) return "add";
	      if(temp == 2) return "sub";
	      return  "mul";
	   }

	   /**
	    * f0 -> "LT"
	    *       | "PLUS"
	    *       | "MINUS"
	    *       | "TIMES"
	    */
	   public String visit(Operator n, Object argu) {
	      return null;
	   }

	   /**
	    * f0 -> "SPILLEDARG"
	    * f1 -> IntegerLiteral()
	    */
	   public String visit(SpilledArg n, Object argu) {
		   return null;
	   }

	   /**
	    * f0 -> Reg()
	    *       | IntegerLiteral()
	    *       | Label()
	    */
	   public String visit(SimpleExp n, Object argu) {
		   if(n.f0.which != 2) return n.f0.accept(this,argu);
		   return ((Label)n.f0.choice).f0.tokenImage;
	   }

	   /**
	    * f0 -> "a0"
	    *       | "a1"
	    *       | "a2"
	    *       | "a3"
	    *       | "t0"
	    *       | "t1"
	    *       | "t2"
	    *       | "t3"
	    *       | "t4"
	    *       | "t5"
	    *       | "t6"
	    *       | "t7"
	    *       | "s0"
	    *       | "s1"
	    *       | "s2"
	    *       | "s3"
	    *       | "s4"
	    *       | "s5"
	    *       | "s6"
	    *       | "s7"
	    *       | "t8"
	    *       | "t9"
	    *       | "v0"
	    *       | "v1"
	    */
	   public String visit(Reg n, Object argu) {
	     return "$" + n.f0.choice.toString();
	   }

	   /**
	    * f0 -> <INTEGER_LITERAL>
	    */
	   public String visit(IntegerLiteral n, Object argu) {
	      return n.f0.tokenImage;
	   }

	   /**
	    * f0 -> <IDENTIFIER>
	    */
	   public String visit(Label n, Object argu) {
		  Put.down();
	      Put.gen(n.f0.tokenImage + ":");
	      return null;
	   }
}
	
