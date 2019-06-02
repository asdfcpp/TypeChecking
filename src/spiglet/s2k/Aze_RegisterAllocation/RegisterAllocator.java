package spiglet.s2k.Aze_RegisterAllocation;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import spiglet.s2k.Gen_interval.*;

public class RegisterAllocator{ // 对得到的活性区间进行线性扫描，分配寄存器
	TreeSet<Temp_interval> inter_tree;
	TreeSet<Temp_interval> active = new TreeSet<Temp_interval>(
		new Comparator<Temp_interval>() {
			public int compare(Temp_interval a, Temp_interval b) {
				if(a == b) return 0;
				if(a.liveInterval.end == b.liveInterval.end) return 1;
				else return a.liveInterval.end - b.liveInterval.end;
			}
		}
	);
	
	Hashtable<Integer, Temp_location> RegAllocTable; //寄存器分配表单
	public int max_args = 0;
	public int usedRegNum = 0;
	public int stackPos = 0; //栈指针的位置
	int position = 0; //当前语句的位置
	
	public LinkedList<AllocateTable> tables = new LinkedList<AllocateTable>();
	RegisterManager regMan = new RegisterManager();
	public int[] usedRegsArray = new int[24];
	
	final static int REGTOTAL = 24;
	final static int GENERALREG = 18;
	
	void initialize(int formalParam) {
		AllocateTable a = new AllocateTable(0);
		int i;
		for(i=4;i<formalParam;i++) { //对temp4到formalParam（形参个数），其位置在内存中
			++stackPos;
			Temp_location l = new Temp_location(false, i-4);
			a.regTable.put(new Integer(i), l);
		}
		tables.add(a);
		return;		
	}
	
	// 参数依次为该过程的参数个数、该过程的每一个变量的活性区间、该过程调用的最大参数个数
	public RegisterAllocator(int formalParam, TreeSet<Temp_interval> t, int Funcmaxarg) {
		max_args = Funcmaxarg;
		inter_tree = t;
		stackPos = 0;
		initialize(formalParam);
	}
	
	//每一句话中的变量放在一个分配表中，所有的分配表在一起组成链表
	public void RegisterAllocation() { //进行寄存器分配的主要函数，每一个活性区间代表一个变量
		active.clear();
		Iterator<Temp_interval> itr1 = inter_tree.iterator(); //活性区间的迭代器
		AllocateTable newt = tables.getFirst(); //tables即为AllocateTable按开始位置递增顺序组成的链表
		while(itr1.hasNext()) { //对liveInterval中每个活跃区间
			Temp_interval a = itr1.next(); 
			boolean flag = false;
			//如果区间a的开始位置大于了表newt的开始位置，则需要一张新表了。新表的开始位置为a的开始位置，新表的变量分配信息直接从旧表复制
			if(newt.start < a.liveInterval.begin) {
				newt = new AllocateTable(a.liveInterval.begin, tables.getLast().regTable, tables.getLast().spillToMem);
				flag = true;
			}
			ExpireOldIntervals(a, newt); //淘汰失活的变量
			if(active.size() == GENERALREG) SpillAtInterval(a, newt); //如果寄存器已满，选择某个活跃变量溢出到内存中
			else DistributeReg(a, newt); //否则分配一个寄存器
			if(flag == true) tables.add(newt); //如果有了新表，将其插入到tables中
		}	
		stackPos += usedRegNum;
		return;
	}
	
	// 为变量a分配一个空寄存器
	void DistributeReg(Temp_interval a, AllocateTable newt) {
		int regNum = regMan.distributeGeneral(a.tempnum);
		int i;
		for(i=0;i<usedRegNum;i++) { //被分配寄存器的数目
			if(usedRegsArray[i] == regNum) break;
		}
		if(i == usedRegNum) usedRegsArray[usedRegNum++] = regNum; //usedRegsArray为保存寄存器序列
		active.add(a); //当前a被分配了寄存器
		newt.regTable.put(new Integer(a.tempnum), new Temp_location(true, regNum)); //分配信息在分配表中
		return;
	}

	// 选择一个活跃变量溢出到内存（把活跃周期结束时间最晚的那个活跃变量溢出到内存）
	void SpillAtInterval(Temp_interval a, AllocateTable newt) {
		Temp_interval b = active.last();
		if(b.liveInterval.end > a.liveInterval.end) {
			// 把b从寄存器中轰出来将地方让给a
			int regLoc = newt.regTable.get(new Integer(b.tempnum)).loc;
			regMan.assignReg(a.tempnum, regLoc);
			// 将b溢出
			int t = stackPos;
			newt.regTable.get(new Integer(b.tempnum)).setState(false, t);
			newt.spillToMem.put(new Integer(b.tempnum), new Temp_location(false, t));
			++stackPos;
			// 将a分配寄存器
			newt.regTable.put(new Integer(a.tempnum), new Temp_location(true, regLoc));
			active.remove(b);
			active.add(a);
		}
		//否则把a溢出
		else newt.regTable.put(new Integer(a.tempnum), new Temp_location(false, stackPos++));
	}
	
	// 为区间a淘汰掉一个失活的变量
	void ExpireOldIntervals(Temp_interval a, AllocateTable newt) {
		//active是按活性区间结束顺序从小到大排列的，如果某个active中活性区间的结束位置大于了a的开始位置，就没有再向后扫描的必要了
		Iterator<Temp_interval> itr1 = active.iterator();
		while(itr1.hasNext()) {
			Temp_interval b = itr1.next();
			if(b.liveInterval.end >= a.liveInterval.begin) return;
			else { //变量b的活性区间已经结束		
				if(!newt.regTable.containsKey(new Integer(b.tempnum))) {
					System.out.println("error in expireoldintervals");
					System.out.println(b.tempnum);
					System.exit(1);
				}
				itr1.remove();
				regMan.freeGeneral(newt.regTable.get(b.tempnum).loc);
				newt.regTable.remove(new Integer(b.tempnum));
			}
		}
	}
}
