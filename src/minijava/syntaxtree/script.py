import os
for p in os.walk(r'D:\pku\编译实习\minijava\src\minijava\syntaxtree'):
	files=p[2]
for s in files:
	f=open(s,"r")
	#print(s)
	java=f.read()
	java=java.replace("package ","package minijava.")
	f.write(java)
	f.close()
	#print(java)
	#break
	