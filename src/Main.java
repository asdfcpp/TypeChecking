import java.io.FileInputStream;
import java.io.InputStream;

import syntaxtree.Node;

public class Main {

	public static void main(String[] args) {
		try{
			InputStream in = new FileInputStream(args[0]);
			Node root = new MiniJavaParser(in).Goal();
			root.accept(new MyVisitor());
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (TokenMgrError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
