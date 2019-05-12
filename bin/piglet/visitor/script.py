import os
for p in os.walk("."):
	files=p[2]
print(files)
for s in files:
	if s.endswith(".java"):
		flag=False
		try:
			f=open(s,"r")
			java=f.read()
		except:
			f=open(s,"r",encoding='utf-8')
			java=f.read()
			flag=True
		#java=java.replace("package visitor;","package piglet.piglet.;")
		java=java.replace("package piglet.piglet.;","package piglet.visitor;")
		java=java.replace("import syntaxtree.*;","import piglet.syntaxtree.*;")
		#java=java.replace("import syntaxtree.*;","import minijava.syntaxtree.*;")
		#java=java.replace("visitor.","piglet.visitor.")
		f.close()
		if flag:
			f=open(s,"w",encoding='utf-8')
		else:
			f=open(s,"w")
		f.write(java)
		f.close()
		#print(java)
		#break
	