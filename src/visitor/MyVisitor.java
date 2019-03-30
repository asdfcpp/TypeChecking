package visitor;

import visitor.*;
import syntaxtree.*;

class MyVisitor extends DepthFirstVisitor {
	public void visit(VarDeclaration n) {
		Identifier id = (Identifier)n.f1;
		System.out.println("VarName: "+id.f0.toString());
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
	}
}
