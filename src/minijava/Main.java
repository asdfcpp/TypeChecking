package minijava;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import piglet.PigletParser;
import minijava.symboltable.SymTable;
import minijava.syntaxtree.Node;
import minijava.visitor.pigletVisitor;
import minijava.visitor.BuildSymbolTableVisitor;
import minijava.visitor.TypeCheckVis;
import piglet.visitor.spigletvisitor;

public class Main { 
	public static String getInputMessage() throws IOException {
        System.out.println("JavaÎÄ¼þµØÖ·£º");
        byte buffer[] = new byte[1024];
        int count = System.in.read(buffer);
        char[] ch = new char[count-2];
        for(int i=0;i<count-2;++i) ch[i] = (char)buffer[i];
        String str = new String(ch);
        return str;
    }
    public static void main(String[] args) {
    	try {
    		String file = getInputMessage();
    		File f = new File(file);   
            InputStream in = new FileInputStream(f);
    		Node minijavaroot = new MiniJavaParser(in).Goal();
    		in.close();
    		SymTable newT = new SymTable();
    		BuildSymbolTableVisitor newST = new BuildSymbolTableVisitor();
 		    minijavaroot.accept(newST, newT);
 		    if(newST.flag) System.out.println("This java file has error(s)");
 		    TypeCheckVis p = new TypeCheckVis(newT);
		    minijavaroot.accept(p);
		    if(p.error) System.out.println("This java file has error(s)");
		    
		    pigletVisitor pg = new pigletVisitor(newT,newST.temp,"");
    	    minijavaroot.accept(pg, newT);
    	    //System.out.println(toPiglet.Put.pigletCode());
    	    
    	    int n = minijava.m2p.Temp.getNumber();
		    new piglet.PigletParser(new ByteArrayInputStream(minijava.m2p.Put.pigletCode().getBytes()));
			piglet.syntaxtree.Node pigletroot = PigletParser.Goal();
			spigletvisitor spg = new spigletvisitor("", n);
		    pigletroot.accept(spg, null);
    	    System.out.println(piglet.p2s.Put.spigletCode());
		}
    	catch(Exception e){
	    	e.printStackTrace();
	    }
    }
}