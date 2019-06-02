package spiglet.s2k.Gen_interval;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import spiglet.visitor.*;
import spiglet.syntaxtree.*;

public class Live_interval_visitor extends DepthFirstVisitor{
	int position = 0;
	public int max_args = 0;
	Hashtable<String, Integer>Labels = new Hashtable<String,Integer>();
	
	// 树集liveInterval，按活跃周期的升序排列
	public TreeSet<Temp_interval> interval_tree = new TreeSet<Temp_interval>(
		new Comparator<Temp_interval>() {
			public int compare(Temp_interval a, Temp_interval b) {
				if(a.liveInterval.begin == b.liveInterval.begin) return 1;
				return (a.liveInterval.begin - b.liveInterval.begin);
			}
		}
	);
	
	Hashtable<Integer, Temp_interval>hashLI = new Hashtable<Integer, Temp_interval>(); //记录每一个变量的活性区间
	
	/**
	 * Grammar production:
	 * f0 -> ( ( Label() )? Stmt() )*
	 */
	public void visit(StmtList n) {
		if(n.f0.present()) {
			for(Enumeration<Node> e = n.f0.elements(); e.hasMoreElements();) {
				++position;
				// 每一句position+1;
				e.nextElement().accept(this);
			}
		}
		return;
	}
	
	/**
	 * Grammar production:
	 * f0 -> "CALL"
	 * f1 -> SimpleExp()
	 * f2 -> "("
	 * f3 -> ( Temp() )*
	 * f4 -> ")"
	 */
	public void visit(Call n) { //记录最大参数
		max_args = Math.max(max_args, n.f3.size());
		n.f1.accept(this);
		n.f3.accept(this);
	}
	
	/**
	 * Grammar production:
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> StmtExp()
	 */
	public void visit(Procedure n) {		
		n.f4.accept(this);
	}
	
	/**
	 * Grammar production:
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public void visit(Temp n) {
		Integer t = new Integer(Integer.parseInt(n.f1.f0.toString()));
		//如果是TEMP 0~TEMP 3,为了防止递归调用时a0~a3被破坏，让它们也参与活性分析并占用通用寄存器
		if(t.intValue() >= 0 && t.intValue() <= 3 && !hashLI.containsKey(t)) {
			Temp_interval a = new Temp_interval(Integer.parseInt(n.f1.f0.toString()), 0, position);
			hashLI.put(t, a);
			interval_tree.add(a);//每一个活性区间的排列
			return;
		}
		//对4-19的临时变量，它们一定在内存中，不必参与活性分析。
		if(t.intValue() >= 4 && t.intValue() <= 19) return;
		//对其他变量，参与活性分析
		if(hashLI.containsKey(t)) {
			Temp_interval a = hashLI.get(t);
			a.liveInterval.end = position;
		}
		else {
			Temp_interval a = new Temp_interval(Integer.parseInt(n.f1.f0.toString()), position, position);
			hashLI.put(t, a);
			interval_tree.add(a);
		}
		return;
	}
	
	/**
	 * Grammar production:
	 * f0 -> Temp()
	 *       | IntegerLiteral()
	 *       | Label()
	 */
	public void visit(SimpleExp n) {
		if(n.f0.which == 2) return; //此时的label不进行操作,对每一个跳转的label操作，以修正活性区间
		else n.f0.accept(this);
	}
	
	public void visit(Label n) { //对每一个label都要用hash，这是肯定的
		if(Labels.containsKey(n.f0.toString())) {
			System.out.println("Error in LIBuildVisitor.visit(Label n), duplicate label defines may exist.");
			System.exit(1);
		}
		else Labels.put(n.f0.toString(), new Integer(position));
		return;
	}
	
	/**
	 * Grammar production:
	 * f0 -> "CJUMP"
	 * f1 -> Temp()
	 * f2 -> Label()
	 */
	public void visit(CJumpStmt n) {
		n.f1.accept(this);
		if(Labels.containsKey(n.f2.f0.toString())) {
			int endn = Labels.get(n.f2.f0.toString()).intValue();
			modifyInterval_end(endn);
		}
		return;
	}
	
	/**
	 * Grammar production:
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public void visit(JumpStmt n) {
		if(Labels.containsKey(n.f1.f0.toString())) {
			int endn = Labels.get(n.f1.f0.toString()).intValue();
			modifyInterval_end(endn);
		}
		return;
	}
	
	/**
	 * Grammar production:
	 * f0 -> "BEGIN"
	 * f1 -> StmtList()
	 * f2 -> "RETURN"
	 * f3 -> SimpleExp()
	 * f4 -> "END"
	 */
	public void visit(StmtExp n) {
		n.f1.accept(this);
		++position;//这个是不错的
		n.f3.accept(this);
	}
	
	
	/*这个函数的功能在于，对于每一个跳转函数，都要检查一下是否符合某一条件，然后进行活性区间的修正
	这个条件就是，如果某一个temp的起始位置大于label的起始位置，且当下temp的既定结束位置在label的
	 开始位置和当前position（即跳转语句）之间，则需要对temp的结束位置进行修正。
	 */
	void modifyInterval_end(int i) {
		Iterator<Temp_interval> itr = interval_tree.iterator();
		while(itr.hasNext()) {
			Temp_interval a = itr.next();
			if(a.liveInterval.begin > i) break;
			else if(i <= a.liveInterval.end && a.liveInterval.end <= position) a.liveInterval.end = position;
		}
		return;
	}
}
