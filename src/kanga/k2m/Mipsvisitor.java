package kanga.k2m;
import kanga.syntaxtree.*;
import kanga.visitor.*;
public class Mipsvisitor extends   GJDepthFirst<String,Object> 
{
	final String endl = "\n";
	final String fp = "$fp";
	final String sp = "$sp";
	final String ra = "$ra";
	final String space = " ";
	int max_arg;
	int a,b,c;
	int t1,t2;
	public Mipsvisitor()
	{};
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
	public String visit(Goal n, Object argu)
	{
		max_arg = Integer.parseInt(n.f8.f0.tokenImage);
		Put.addTab();
		Put.con(".text" + endl);
		Put.con(".globl main" + endl);
		Put.minusTab();
		Put.con("main:" + endl);
		Put.addTab();
		Put.con("sw" +space + fp+"," + " -8(" + sp + ")"+endl);//�����ϸ�֡�ĵ�ַ���ڱ�֡��fpλ(��ջ��)
		Put.con("move" + space + fp + space+sp + endl);
		
		a = Integer.parseInt(n.f2.f0.toString());
		b = Integer.parseInt(n.f5.f0.tokenImage);
		c = Integer.parseInt(n.f8.f0.toString());
		t1 = (a>4?(a-4):0);
		t2 = (c>4?(c-4):0);
		int stackLength= (b+2+t2-t1)*4;
		//int  = Integer.parseInt(n.f5.f0.tokenImage);
		//stackLength = (stackLength + 2) * 4;
		Put.con("subu" + space+sp +"," + space+sp+","+ space +stackLength + endl);//��֡�õ�ջ�ռ��С
		Put.con("sw"+space + ra+"," + " -4("+fp+")"+endl);
		n.f10.accept(this,argu);//���������ľ������
		Put.con("lw" +space +ra+ " -4("+fp+")"+endl);//ȡ��ص�ַ
		Put.con("lw"+space +fp+ " -8("+fp+")"+endl);//��֡fp��ַ
		Put.con("addu"+ space+sp +"," + space+sp+","+ space +stackLength + endl);//������֡sp��ַ
		Put.con("j" + space + ra + endl);
		n.f12.accept(this,argu);
		Put.called_function();
		return null;
	}

	/**
	 * f0 -> ( ( Label() )? Stmt() )*
	 */
	public String visit(StmtList n, Object argu)
	{
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
		Put.con(".text" + endl);
		Put.minusTab();
		Put.con(n.f0.f0.tokenImage+":" + endl);
		Put.addTab();
		Put.con("sw" +space + fp +","+ " -8(" + sp + ")"+endl);//�����ϸ�֡�ĵ�ַ���ڱ�֡��fpλ(��ջ��)
		Put.con("move" + space + fp + space+sp + endl);
		b = Integer.parseInt(n.f5.f0.tokenImage);
		c = Integer.parseInt(n.f8.f0.toString());
		t1 = (a>4?(a-4):0);
		t2 = (c>4?(c-4):0);
		int stackLength= (b+2+t2-t1)*4;
		//int stackLength = Integer.parseInt(n.f5.f0.tokenImage);
		//stackLength = (stackLength + 2) * 4;
		Put.con("subu" + space+sp +"," + space+sp+","+ space +stackLength + endl);//��֡�õ�ջ�ռ��С
		Put.con("sw"+space + ra +","+ " -4("+fp+")"+endl);
		n.f10.accept(this,argu);//���������ľ������
		Put.con("lw" +space +ra+ " -4("+fp+")"+endl);//ȡ��ص�ַ
		Put.con("lw"+space +fp+ " -8("+fp+")"+endl);//��֡fp��ַ
		Put.con("addu"+ space+sp +"," + space+sp+","+ space +stackLength + endl);//������֡sp��ַ
		Put.con("j" + space + ra + endl);
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
	public String visit(Stmt n, Object argu) 
	{
	      n.f0.accept(this, argu);
	      return null;
	}

	/**
	 * f0 -> "NOOP"
	 */
	public String visit(NoOpStmt n, Object argu)
	{
	     Put.con(" nop" + endl);
	     Put.addTab();
	     return null;
	}

	/**
     * f0 -> "ERROR"
	 */
	public String visit(ErrorStmt n, Object argu) 
	{
	      Put.con("jal _error"+endl);//����������ò����
	      return null;
	}

	   /**
	    * f0 -> "CJUMP"
	    * f1 -> Reg()
	    * f2 -> Label()
	    */
	public String visit(CJumpStmt n, Object argu)
	{//Ҫ�ж�����һ��Ĵ���
		int tempi = n.f1.f0.which;
	     Put.con("beqz" + space + Regs.REGS[tempi] + ","+space + n.f2.f0.toString() + endl);
	    return null;
	}

	   /**
	    * f0 -> "JUMP"
	    * f1 -> Label()
	    */
	   public String visit(JumpStmt n, Object argu)
	   {
	      Put.con("j" + space  + n.f1.f0.toString()+endl);
	      return null;
	   }

	   /**
	    * f0 -> "HSTORE"
	    * f1 -> Reg()
	    * f2 -> IntegerLiteral()
	    * f3 -> Reg()
	    */
	   public String visit(HStoreStmt n, Object argu)
	   {
		   int temprs = n.f3.f0.which;
		   int temprd = n.f1.f0.which;
		   Put.con("sw" + space + Regs.REGS[temprs] +","+ space + n.f2.f0.toString() + "(" + Regs.REGS[temprd] +")" + endl);
		   return null;
	   }

	   /**
	    * f0 -> "HLOAD"
	    * f1 -> Reg()
	    * f2 -> Reg()
	    * f3 -> IntegerLiteral()
	    */
	   public String visit(HLoadStmt n, Object argu)
	   {
		   int temprs = n.f2.f0.which;
		   int temprd = n.f1.f0.which;
		   Put.con("lw" + space +Regs.REGS[temprd] +","+ space + n.f3.f0.toString() + "(" + Regs.REGS[temprs] +")" + endl);
	   		return null;
	   }

	   /**
	    * f0 -> "MOVE"
	    * f1 -> Reg()
	    * f2 -> Exp()
	    * 				/** Exp()
	    				* Grammar production:
						 * f0 -> HAllocate()
						 *       | BinOp()//�����˷���ֵ
						 *       | SimpleExp()//�����˷���ֵ
	    */
	   public String visit(MoveStmt n, Object argu) //expֻ����move����
	   {	     
	     int temprd = n.f1.f0.which; 
		 if(n.f2.f0.which == 0)
		 {
			   n.f2.f0.accept(this,argu);
			   Put.con("jal " + "_hallocs" + endl);
			   Put.con("move" + space + Regs.REGS[temprd] + space + "$v0"+endl);
		 }
		 if(n.f2.f0.which == 1)
		 {
			  BinOp p = ((BinOp)n.f2.f0.choice);
			  int temprs = p.f1.f0.which;
			  String val2 = p.f2.accept(this,argu);
			  String val1 = p.accept(this,argu);
			  Put.con(val1 +space +Regs.REGS[temprd] +","+ space + Regs.REGS[temprs] + "," + space + val2 + endl);
		 }
		 else if(n.f2.f0.which == 2)
		 {
			 SimpleExp p = ((SimpleExp)n.f2.f0.choice);
			 int temp = p.f0.which;
			 String val = p.accept(this,argu);
			 if(temp == 0)
		      {
		    	  Put.con("move" + space + Regs.REGS[temprd] + space + val + endl);
		      }
		      if(temp == 1)
		      {
		    	  Put.con("li" + space + "$a0" + space + val + endl);
		    	  Put.con("move" + space + Regs.REGS[temprd] + space + "$a0" + endl);
		      }
		      if(temp == 2)
		      {
		    	  Put.con("la" + space + "$a0" + space + val + endl);
		    	  Put.con("move" + space + Regs.REGS[temprd] + space + "$a0" + endl);

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
	   public String visit(PrintStmt n, Object argu)
	   {
	      int temp =  n.f1.f0.which;
	      String val  =  n.f1.accept(this,argu);
	      if(temp == 0)
	      {
	    	  Put.con("move" + space + "$a0" + space + val + endl);
	      }
	      if(temp == 1)
	      {
	    	  Put.con("li" + space + "$a0" + space + val + endl);
	      }
	      if(temp == 2)
	      {
	    	  Put.con("la" + space + "$a0" + space + val + endl);
	      }
	      Put.con("jal"+ space + "_printint" + endl);
	      return null;
	   }
	   
	   

	   /**
	    * f0 -> "ALOAD"
	    * f1 -> Reg()
	    * f2 -> SpilledArg()
	    */
	   public String visit(ALoadStmt n, Object argu)
	   {
	      int temp = Integer.parseInt(n.f2.f1.f0.tokenImage);
	      temp = temp * 4;
	      int temprd = n.f1.f0.which;
	      if(temp >= max_arg*4)
	    	  Put.con("lw" + space + Regs.REGS[temprd] +","+ space + temp + "(" + sp + ")" + endl);
	      else if(temp < max_arg*4)
	      {
	    	  Put.con("lw" + space + Regs.REGS[temprd] +","+ space + temp + "(" + fp + ")" + endl);
	      }
	      return null;
	   }

	   /**
	    * f0 -> "ASTORE"
	    * f1 -> SpilledArg()
	    * f2 -> Reg()
	    */
	   public String visit(AStoreStmt n, Object argu)
	   {
		   int temp = Integer.parseInt(n.f1.f1.f0.tokenImage);
		  // int len = Integer.parseInt(n.f1.f1.f0.tokenImage);
		   temp = temp * 4;
		   int temprs = n.f2.f0.which;
		   if(temp >= max_arg *4)
		   {
			   Put.con("sw" + space + Regs.REGS[temprs] +","+ space + temp + "(" + sp + ")" + endl);
		   }
		   else if(temp < max_arg*4)
			   Put.con("sw" + space + Regs.REGS[temprs] +","+ space + temp + "(" + fp + ")" + endl);
		   return null;
	   }

	   /**
	    * f0 -> "PASSARG"
	    * f1 -> IntegerLiteral()
	    * f2 -> Reg()
	    */
	   public String visit(PassArgStmt n, Object argu)
	   {
		   int len = Integer.parseInt(n.f1.f0.tokenImage);
		   int temprs = n.f2.f0.which;
	       Put.con("sw" + space + Regs.REGS[temprs] + "," + space + (len-1)*4+ "(" + sp + ")" + endl);
	       return null;
	   }

	   /**
	    * f0 -> "CALL"
	    * f1 -> SimpleExp()
	    */
	   public String visit(CallStmt n, Object argu) 
	   {
		  String val = n.f1.accept(this,argu);
	      if(n.f1.f0.which == 0)
	      {
	    	  Put.con("jalr" + space + val + endl);
	      }
	      if(n.f1.f0.which == 1)
	      {
	    	  Put.con("li" + " $a0" + space + val+ endl);
	    	  Put.con("jalr" + space + "$a0" +endl);
	      }
	      else if(n.f1.f0.which == 2)
	    	  Put.con("jal" + space + val + endl);	 
	      return null;
	   }
	   

	   /**
	    * f0 -> HAllocate()
	    *       | BinOp()
	    *       | SimpleExp()
	    */
	   public String visit(Exp n, int argu)
	   {
		  return null;
		   
	   }

	   /**
	    * f0 -> "HALLOCATE"
	    * f1 -> SimpleExp()
	    */
	   public String visit(HAllocate n, Object argu)
	   {
		  String val = n.f1.accept(this,argu);
		  int temp = n.f1.f0.which;
		  if(temp == 0)
		  {
		   	  Put.con("move" + space + "$a0" + space + val + endl);
		  }
		  if(temp == 1)
		  {
		      Put.con("li" + space + "$a0" + space + val + endl);
		  }
		  if(temp == 2)
		  {
		      Put.con("la" + space + "$a0" + space + val + endl);
		  }
		  return null;
	   }

	   /**
	    * f0 -> Operator()
	    * f1 -> Reg()
	    * f2 -> SimpleExp()
	    */
	   public String visit(BinOp n, Object argu)
	   {
	      int temp = n.f0.f0.which;
	      if(temp == 0)
	      {
	    	  return "slt";
	      }
	      if(temp == 1)
	      {
	    	  return "add";
	      }
	      if(temp == 2)
	      {
	    	  return "sub";
	      }
	      else
	      {
	    	  return  "mul";
	      }	      
	   }

	   /**
	    * f0 -> "LT"
	    *       | "PLUS"
	    *       | "MINUS"
	    *       | "TIMES"
	    */
	   public String visit(Operator n, Object argu)
	   {
	      return null;
	   }

	   /**
	    * f0 -> "SPILLEDARG"
	    * f1 -> IntegerLiteral()
	    */
	   public String visit(SpilledArg n, Object argu)
	   {
		   return null;
	   }

	   /**
	    * f0 -> Reg()
	    *       | IntegerLiteral()
	    *       | Label()
	    */
	   public String visit(SimpleExp n, Object argu) 
	   {
		   if(n.f0.which != 2)
			   return n.f0.accept(this,argu);
		   else
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
	   public String visit(Reg n, Object argu)
	   {
	     return "$" + n.f0.choice.toString();
	   }

	   /**
	    * f0 -> <INTEGER_LITERAL>
	    */
	   public String visit(IntegerLiteral n, Object argu) 
	   {
	      return n.f0.tokenImage;
	   }

	   /**
	    * f0 -> <IDENTIFIER>
	    */
	   public String visit(Label n, Object argu)
	   {
		  Put.minusTab();
	      Put.con(n.f0.tokenImage + ":");
	      return null;
	   }
}
	
