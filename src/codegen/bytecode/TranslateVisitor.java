package codegen.bytecode;

import java.util.Hashtable;
import java.util.LinkedList;

import util.Label;
import codegen.bytecode.Ast.Class;
import codegen.bytecode.Ast.Class.ClassSingle;
import codegen.bytecode.Ast.Dec;
import codegen.bytecode.Ast.Dec.DecSingle;
import codegen.bytecode.Ast.MainClass;
import codegen.bytecode.Ast.MainClass.MainClassSingle;
import codegen.bytecode.Ast.Method;
import codegen.bytecode.Ast.Method.MethodSingle;
import codegen.bytecode.Ast.Program;
import codegen.bytecode.Ast.Program.ProgramSingle;
import codegen.bytecode.Ast.Stm;
import codegen.bytecode.Ast.Stm.Aload;
import codegen.bytecode.Ast.Stm.Areturn;
import codegen.bytecode.Ast.Stm.ArrayLength;
import codegen.bytecode.Ast.Stm.Astore;
import codegen.bytecode.Ast.Stm.Goto;
import codegen.bytecode.Ast.Stm.Iadd;
import codegen.bytecode.Ast.Stm.Iaload;
import codegen.bytecode.Ast.Stm.Iand;
import codegen.bytecode.Ast.Stm.Iastore;
import codegen.bytecode.Ast.Stm.Ificmplt;
import codegen.bytecode.Ast.Stm.Ifne;
import codegen.bytecode.Ast.Stm.Iload;
import codegen.bytecode.Ast.Stm.Imul;
import codegen.bytecode.Ast.Stm.Invokevirtual;
import codegen.bytecode.Ast.Stm.Ireturn;
import codegen.bytecode.Ast.Stm.Istore;
import codegen.bytecode.Ast.Stm.Isub;
import codegen.bytecode.Ast.Stm.LabelJ;
import codegen.bytecode.Ast.Stm.Ldc;
import codegen.bytecode.Ast.Stm.New;
import codegen.bytecode.Ast.Stm.NewIntArra;
import codegen.bytecode.Ast.Stm.Print;
import codegen.bytecode.Ast.Type;
import codegen.bytecode.Ast.Type.ClassType;
import codegen.bytecode.Ast.Type.Int;
import codegen.bytecode.Ast.Type.IntArray;

// Given a Java ast, translate it into Java bytecode.

public class TranslateVisitor implements ast.Visitor {
	private String classId;
	private int index;
	private Hashtable<String, Integer> indexTable;
	private Type.T type; // type after translation
	private Dec.T dec;
	private LinkedList<Stm.T> stms;
	private Method.T method;
	private Class.T classs;
	private MainClass.T mainClass;
	public Program.T program;

	public TranslateVisitor() {
		this.classId = null;
		this.indexTable = null;
		this.type = null;
		this.dec = null;
		this.stms = new LinkedList<Stm.T>();
		this.method = null;
		this.classs = null;
		this.mainClass = null;
		this.program = null;
	}

	private void emit(Stm.T s) {
		this.stms.add(s);
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(ast.Ast.Exp.Add e) {
		
		  // Lab3 exercise11 by king
		  e.left.accept(this);
		  e.right.accept(this);
		  emit(new Iadd());
		  
		  return;
	}

	@Override
	public void visit(ast.Ast.Exp.And e) {
		
		  // Lab3 exercise11 by king
		  e.left.accept(this);
		  e.right.accept(this);
		  emit(new Iand());
		  
		  return;
	}

	@Override
	public void visit(ast.Ast.Exp.ArraySelect e) {
		
		// Lab3 exercise11 by king
		e.array.accept(this);
		e.index.accept(this);
		emit(new Iaload());
		
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.Call e) {
		e.exp.accept(this);
		for (ast.Ast.Exp.T x : e.args) {
			x.accept(this);
		}
		e.rt.accept(this);
		Type.T rt = this.type;
		LinkedList<Type.T> at = new LinkedList<Type.T>();
		for (ast.Ast.Type.T t : e.at) {
			t.accept(this);
			at.add(this.type);
		}
		emit(new Invokevirtual(e.id, e.type, at, rt));
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.False e) {
		
		// Lab3 exercise11 by king
		new Ldc(0);

		return;
	}

	@Override
	public void visit(ast.Ast.Exp.Id e) {
		int index = this.indexTable.get(e.id);
		ast.Ast.Type.T type = e.type;
		if (type.getNum() > 0)// a reference
			emit(new Aload(index));
		else
			emit(new Iload(index));
		// but what about this is a field?
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.Length e) {
		
		  // Lab3 exercise11 by king
		  e.array.accept(this);
		  emit(new ArrayLength());
		  
		  return;
	}

	@Override
	public void visit(ast.Ast.Exp.Lt e) {
		Label tl = new Label(), fl = new Label(), el = new Label();
		e.left.accept(this);
		e.right.accept(this);
		emit(new Ificmplt(tl));
		emit(new LabelJ(fl));
		emit(new Ldc(0));
		emit(new Goto(el));
		emit(new LabelJ(tl));
		emit(new Ldc(1));
		emit(new Goto(el));
		emit(new LabelJ(el));
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.NewIntArray e) {
		
		// Lab3 exercise11 by king
		e.exp.accept(this);
		emit(new NewIntArra());
		
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.NewObject e) {
		emit(new New(e.id));
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.Not e) {

		// Lab3 exercise11 by king
		Label tl = new Label(), el = new Label();
		e.exp.accept(this);
		this.emit(new Ifne(tl));

		this.emit(new Ldc(1));
		this.emit(new Goto(el));

		this.emit(new LabelJ(tl));
		this.emit(new Ldc(0));

		this.emit(new LabelJ(el));
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.Num e) {
		emit(new Ldc(e.num));
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.Sub e) {
		e.left.accept(this);
		e.right.accept(this);
		emit(new Isub());
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.This e) {
		emit(new Aload(0));
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.Times e) {
		e.left.accept(this);
		e.right.accept(this);
		emit(new Imul());
		return;
	}

	@Override
	public void visit(ast.Ast.Exp.True e) {

		// Lab3 exercise11 by king
		new Ldc(1);

		return;
	}

	// ///////////////////////////////////////////////////
	// statements
	@Override
	public void visit(ast.Ast.Stm.Assign s) {
		s.exp.accept(this);
		int index = this.indexTable.get(s.id);
		ast.Ast.Type.T type = s.type;
		if (type.getNum() > 0)
			emit(new Astore(index));
		else
			emit(new Istore(index));

		return;
	}

	@Override
	public void visit(ast.Ast.Stm.AssignArray s) {

		int index = this.indexTable.get(s.id);
		emit(new Aload(index));
		
		s.index.accept(this);
		s.exp.accept(this);
		emit(new Iastore());
		
		return;
	}

	@Override
	public void visit(ast.Ast.Stm.Block s) {

		// Lab3 exercise11 by king
		for (ast.Ast.Stm.T stm : s.stms)
			stm.accept(this);

		return;
	}

	@Override
	public void visit(ast.Ast.Stm.If s) {
		Label tl = new Label(), fl = new Label(), el = new Label();
		s.condition.accept(this);

		emit(new Ifne(tl));
		emit(new LabelJ(fl));
		s.elsee.accept(this);
		emit(new Goto(el));
		emit(new LabelJ(tl));
		s.thenn.accept(this);
		emit(new Goto(el));
		emit(new LabelJ(el));
		return;
	}

	@Override
	public void visit(ast.Ast.Stm.Print s) {
		s.exp.accept(this);
		emit(new Print());
		return;
	}

	@Override
	public void visit(ast.Ast.Stm.While s) {

		// Lab3 exercise11 by king
		Label sl = new Label(), tl = new Label(), fl = new Label(), el = new Label();

		emit(new LabelJ(sl));
		s.condition.accept(this);
		emit(new Ifne(tl));

		emit(new LabelJ(fl));
		emit(new Goto(el));

		emit(new LabelJ(tl));
		s.body.accept(this);
		emit(new Goto(sl));

		emit(new LabelJ(el));

		return;

	}

	// type
	@Override
	public void visit(ast.Ast.Type.Boolean t) {
		this.type = new Int();
		return;
	}

	@Override
	public void visit(ast.Ast.Type.ClassType t) {

		// Lab3 exercise11
		this.type = new ClassType(t.id);
		return;
	}

	@Override
	public void visit(ast.Ast.Type.Int t) {

		// Lab3 exercise11
		this.type = new Int();
		return;
	}

	@Override
	public void visit(ast.Ast.Type.IntArray t) {

		// Lab3 exercise11
		this.type = new IntArray();
	}

	// dec
	@Override
	public void visit(ast.Ast.Dec.DecSingle d) {
		d.type.accept(this);
		this.dec = new DecSingle(this.type, d.id);
		this.indexTable.put(d.id, index++);
		return;
	}

	// method
	@Override
	public void visit(ast.Ast.Method.MethodSingle m) {
		// record, in a hash table, each var's index
		// this index will be used in the load store operation
		this.index = 1;
		this.indexTable = new Hashtable<String, Integer>();

		m.retType.accept(this);
		Type.T newRetType = this.type;
		LinkedList<Dec.T> newFormals = new LinkedList<Dec.T>();
		for (ast.Ast.Dec.T d : m.formals) {
			d.accept(this);
			newFormals.add(this.dec);
		}
		LinkedList<Dec.T> locals = new java.util.LinkedList<Dec.T>();
		for (ast.Ast.Dec.T d : m.locals) {
			d.accept(this);
			locals.add(this.dec);
		}
		this.stms = new LinkedList<Stm.T>();
		for (ast.Ast.Stm.T s : m.stms) {
			s.accept(this);
		}

		// return statement is specially treated
		m.retExp.accept(this);

		if (m.retType.getNum() > 0)
			emit(new Areturn());
		else
			emit(new Ireturn());

		this.method = new MethodSingle(newRetType, m.id, this.classId,
				newFormals, locals, this.stms, 0, this.index);

		return;
	}

	// class
	@Override
	public void visit(ast.Ast.Class.ClassSingle c) {
		this.classId = c.id;
		LinkedList<Dec.T> newDecs = new LinkedList<Dec.T>();
		for (ast.Ast.Dec.T dec : c.decs) {
			dec.accept(this);
			newDecs.add(this.dec);
		}
		LinkedList<Method.T> newMethods = new LinkedList<Method.T>();
		for (ast.Ast.Method.T m : c.methods) {
			m.accept(this);
			newMethods.add(this.method);
		}
		this.classs = new ClassSingle(c.id, c.extendss, newDecs, newMethods);
		return;
	}

	// main class
	@Override
	public void visit(ast.Ast.MainClass.MainClassSingle c) {
		c.stm.accept(this);
		this.mainClass = new MainClassSingle(c.id, c.arg, this.stms);
		this.stms = new LinkedList<Stm.T>();
		return;
	}

	// program
	@Override
	public void visit(ast.Ast.Program.ProgramSingle p) {
		// do translations
		p.mainClass.accept(this);

		LinkedList<Class.T> newClasses = new LinkedList<Class.T>();
		for (ast.Ast.Class.T classes : p.classes) {
			classes.accept(this);
			newClasses.add(this.classs);
		}
		this.program = new ProgramSingle(this.mainClass, newClasses);
		return;
	}
}
