package spiglet.s2k.Aze_RegisterAllocation;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import spiglet.s2k.Gen_interval.*;

public class RegisterAllocator{ // �Եõ��Ļ��������������ɨ�裬����Ĵ���
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
	
	Hashtable<Integer, Temp_location> RegAllocTable; //�Ĵ��������
	public int max_args = 0;
	public int usedRegNum = 0;
	public int stackPos = 0; //ջָ���λ��
	int position = 0; //��ǰ����λ��
	
	public LinkedList<AllocateTable> tables = new LinkedList<AllocateTable>();
	RegisterManager regMan = new RegisterManager();
	public int[] usedRegsArray = new int[24];
	
	final static int REGTOTAL = 24;
	final static int GENERALREG = 18;
	
	void initialize(int formalParam) {
		AllocateTable a = new AllocateTable(0);
		int i;
		for(i=4;i<formalParam;i++) { //��temp4��formalParam���βθ���������λ�����ڴ���
			++stackPos;
			Temp_location l = new Temp_location(false, i-4);
			a.regTable.put(new Integer(i), l);
		}
		tables.add(a);
		return;		
	}
	
	// ��������Ϊ�ù��̵Ĳ����������ù��̵�ÿһ�������Ļ������䡢�ù��̵��õ�����������
	public RegisterAllocator(int formalParam, TreeSet<Temp_interval> t, int Funcmaxarg) {
		max_args = Funcmaxarg;
		inter_tree = t;
		stackPos = 0;
		initialize(formalParam);
	}
	
	//ÿһ�仰�еı�������һ��������У����еķ������һ���������
	public void RegisterAllocation() { //���мĴ����������Ҫ������ÿһ�������������һ������
		active.clear();
		Iterator<Temp_interval> itr1 = inter_tree.iterator(); //��������ĵ�����
		AllocateTable newt = tables.getFirst(); //tables��ΪAllocateTable����ʼλ�õ���˳����ɵ�����
		while(itr1.hasNext()) { //��liveInterval��ÿ����Ծ����
			Temp_interval a = itr1.next(); 
			boolean flag = false;
			//�������a�Ŀ�ʼλ�ô����˱�newt�Ŀ�ʼλ�ã�����Ҫһ���±��ˡ��±�Ŀ�ʼλ��Ϊa�Ŀ�ʼλ�ã��±�ı���������Ϣֱ�ӴӾɱ���
			if(newt.start < a.liveInterval.begin) {
				newt = new AllocateTable(a.liveInterval.begin, tables.getLast().regTable, tables.getLast().spillToMem);
				flag = true;
			}
			ExpireOldIntervals(a, newt); //��̭ʧ��ı���
			if(active.size() == GENERALREG) SpillAtInterval(a, newt); //����Ĵ���������ѡ��ĳ����Ծ����������ڴ���
			else DistributeReg(a, newt); //�������һ���Ĵ���
			if(flag == true) tables.add(newt); //��������±�������뵽tables��
		}	
		stackPos += usedRegNum;
		return;
	}
	
	// Ϊ����a����һ���ռĴ���
	void DistributeReg(Temp_interval a, AllocateTable newt) {
		int regNum = regMan.distributeGeneral(a.tempnum);
		int i;
		for(i=0;i<usedRegNum;i++) { //������Ĵ�������Ŀ
			if(usedRegsArray[i] == regNum) break;
		}
		if(i == usedRegNum) usedRegsArray[usedRegNum++] = regNum; //usedRegsArrayΪ����Ĵ�������
		active.add(a); //��ǰa�������˼Ĵ���
		newt.regTable.put(new Integer(a.tempnum), new Temp_location(true, regNum)); //������Ϣ�ڷ������
		return;
	}

	// ѡ��һ����Ծ����������ڴ棨�ѻ�Ծ���ڽ���ʱ��������Ǹ���Ծ����������ڴ棩
	void SpillAtInterval(Temp_interval a, AllocateTable newt) {
		Temp_interval b = active.last();
		if(b.liveInterval.end > a.liveInterval.end) {
			// ��b�ӼĴ����к�������ط��ø�a
			int regLoc = newt.regTable.get(new Integer(b.tempnum)).loc;
			regMan.assignReg(a.tempnum, regLoc);
			// ��b���
			int t = stackPos;
			newt.regTable.get(new Integer(b.tempnum)).setState(false, t);
			newt.spillToMem.put(new Integer(b.tempnum), new Temp_location(false, t));
			++stackPos;
			// ��a����Ĵ���
			newt.regTable.put(new Integer(a.tempnum), new Temp_location(true, regLoc));
			active.remove(b);
			active.add(a);
		}
		//�����a���
		else newt.regTable.put(new Integer(a.tempnum), new Temp_location(false, stackPos++));
	}
	
	// Ϊ����a��̭��һ��ʧ��ı���
	void ExpireOldIntervals(Temp_interval a, AllocateTable newt) {
		//active�ǰ������������˳���С�������еģ����ĳ��active�л�������Ľ���λ�ô�����a�Ŀ�ʼλ�ã���û�������ɨ��ı�Ҫ��
		Iterator<Temp_interval> itr1 = active.iterator();
		while(itr1.hasNext()) {
			Temp_interval b = itr1.next();
			if(b.liveInterval.end >= a.liveInterval.begin) return;
			else { //����b�Ļ��������Ѿ�����		
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
