package slp;

import java.io.FileWriter;
import java.util.HashSet;

import slp.Slp.Exp;
import slp.Slp.Exp.Eseq;
import slp.Slp.Exp.Id;
import slp.Slp.Exp.Num;
import slp.Slp.Exp.Op;
import slp.Slp.ExpList;
import slp.Slp.Stm;
import util.Bug;
import control.Control;


public class Main
{
    // ///////////////////////////////////////////
	//Lab1. Exercise 3: get maximum number of arguments of any print statement.
	//Function maxArgsExplist() and maxArgsExp() are called by maxArgsStm() 
	//maxArgsStm(Stm.T stm) return the maximum number of arguments
	
	private int maxArgs(Stm.T stm)
	{
		return maxArgsStm(stm);
	}
	
	private int maxArgsExplist(ExpList.T explist)
    {
        if (explist instanceof ExpList.Pair)
        {
        	ExpList.Pair pair = (ExpList.Pair)explist;
        	return 1 + maxArgsExplist(pair.list);
        }
        else if (explist instanceof ExpList.Last)
        {
        	return 1;
        }
        else
        {
            new Bug();
        }
        
        return 0;
    }
	
    private int maxArgsExp(Exp.T exp)
    {
    	int count1 = 0;
    	int count2 = 0;
    	
    	if (exp instanceof Exp.Id)
    	{
    		return 0;
    	}
    	else if (exp instanceof Exp.Num)
    	{
    		return 0;
    	}
    	else if (exp instanceof Exp.Op)
    	{
    		Exp.Op op = (Exp.Op)exp;
    		count1 = maxArgsExp(op.left);
    		count2 = maxArgsExp(op.right);
    		
    		return count1 > count2 ? count1 : count2;
    	}
    	else if(exp instanceof Exp.Eseq)
    	{
    		Exp.Eseq eseq = (Exp.Eseq)exp;
    		count1 = maxArgsExp(eseq.exp);
    		count2 = maxArgsStm(eseq.stm);
    		
    		return count1 > count2 ? count1 : count2;
    	}
    	else
    	{
    		new Bug();
    	}
    	
        return 0;
    }

    private int maxArgsStm(Stm.T stm)
    {
        if (stm instanceof Stm.Compound) 
        {
            Stm.Compound s = (Stm.Compound) stm;
            int n1 = maxArgsStm(s.s1);
            int n2 = maxArgsStm(s.s2);

            return n1 >= n2 ? n1 : n2;
        } 
        else if (stm instanceof Stm.Assign) 
        {
            Stm.Assign assign = (Stm.Assign)stm;
            return maxArgsExp(assign.exp);
        } 
        else if (stm instanceof Stm.Print) 
        {
            Stm.Print p = (Stm.Print) stm;
            return maxArgsExplist(p.explist);
        } 
        else
        {
        	new Bug();
        }
        return 0;
    }
    // Lab1. Exercise 3 : end
    
    // ////////////////////////////////////////
    // Lab1. Exercise 4 : interprets program in language SLP
    
    private void interp(Stm.T s)
    {
    	interpStm(s, null);
    }
   
    private Table interpStm(Stm.T stm, Table t)
    {
    	IntAndTable intAndTable;

		if (stm instanceof Stm.Compound)
		{
			Stm.Compound s = (Stm.Compound)stm;
			
			t = interpStm(s.s1, t);
			return interpStm(s.s2, t);
		} 
		else if (stm instanceof Stm.Assign)
		{
			Stm.Assign s = (Stm.Assign)stm;
			
			intAndTable = interpExp(s.exp, t);
			return update(intAndTable.table, s.id, intAndTable.value);
		}
		else if (stm instanceof Stm.Print)
		{
			Stm.Print s = (Stm.Print)stm;
			
			intAndTable = interpExplist(s.explist, t);
			return intAndTable.table;
		}
		else
		{
			new Bug();
		}
		
		return null;
    }
    
    private IntAndTable interpExplist(ExpList.T explist, Table t)
    {
    	IntAndTable intAndTable;
    	
    	if(explist instanceof ExpList.Pair)
    	{
    		ExpList.Pair p = (ExpList.Pair)explist;

    		intAndTable = interpExp(p.exp, t);
    		System.out.print(intAndTable.value + " ");
    		
    		return interpExplist(p.list, t);
    	}
    	else if(explist instanceof ExpList.Last)
    	{
    		ExpList.Last l = (ExpList.Last)explist;

    		intAndTable = interpExp(l.exp, t);
    		System.out.print(intAndTable.value + "\n");
    		
    		return intAndTable;
    	}
    	else
    	{
    		new Bug();
    	}
    	
    	return null;
    }

    
    private IntAndTable interpExp(Exp.T exp, Table t)
    {
    	int value = 0;
    	
        if (exp instanceof Exp.Id)
        {
        	Exp.Id e = (Exp.Id)exp;
        	value = lookup(t, e.id);
        	
        	return new IntAndTable(value, t);
        }
        else if (exp instanceof Exp.Num)
        {
        	Exp.Num e = (Exp.Num)exp;
        	
        	return new IntAndTable(e.num, t);
        }
        else if (exp instanceof Exp.Eseq)
        {
        	Exp.Eseq eseq = (Exp.Eseq)exp;
        	t = interpStm(eseq.stm, t);
        	
        	return interpExp(eseq.exp, t);
        }
        else if (exp instanceof Exp.Op)
        {
        	IntAndTable intAndTableLeft;
         	IntAndTable intAndTableRight;
        	
        	Exp.Op op = (Exp.Op)exp;
        	intAndTableLeft = interpExp(op.left, t);
			intAndTableRight = interpExp(op.right, intAndTableLeft.table);
			
			switch (op.op)
			{
				case ADD:
					value = intAndTableLeft.value + intAndTableRight.value;
					break;
				case SUB:
					value = intAndTableLeft.value - intAndTableRight.value;
					break;
				case TIMES:
					value = intAndTableLeft.value * intAndTableRight.value;
					break;
				case DIVIDE:
					value = intAndTableLeft.value / intAndTableRight.value;
					break;
				default:
					new Bug();
					break;
			}
			return new IntAndTable(value, intAndTableRight.table);
		}
        else
        {
        	new Bug();
        }
        
        return null;
    }  
    
    public class Table
    {
    	String id;
    	int value;
    	Table tail;
    	
    	public Table(String i, int v, Table t)
    	{
    		id = i;
    		value = v;
    		tail = t;
    	}
    	
    }
    
    public class IntAndTable
    {
    	int value;
    	Table table;
    	
    	public IntAndTable(int ii, Table tt)
    	{
    		value = ii;
    		table = tt;
    	}
    }
    
    private int lookup(Table table, String id)
    {
    	Table t = table;
    	
    	while(t != null)
    	{
    		if(t.id == id)
    			return t.value;
    		t = t.tail;
    	}
    	
    	new Bug();
    	return -1;
    }
    
    private Table update(Table table, String id, int value) 
    {
		return new Table(id, value, table);
	}
    
    // Lab1. Exercise 4 : end

    // ////////////////////////////////////////
    // compile
    HashSet<String> ids;
    StringBuffer buf;

    private void emit(String s)
    {
        buf.append(s);
    }

    private void compileExp(Exp.T exp)
    {
        if (exp instanceof Id) {
            Exp.Id e = (Exp.Id) exp;
            String id = e.id;

            emit("\tmovl\t" + id + ", %eax\n");
        } else if (exp instanceof Num) {
            Exp.Num e = (Exp.Num) exp;
            int num = e.num;

            emit("\tmovl\t$" + num + ", %eax\n");
        } else if (exp instanceof Op) {
            Exp.Op e = (Exp.Op) exp;
            Exp.T left = e.left;
            Exp.T right = e.right;
            Exp.OP_T op = e.op;

            switch (op) {
                case ADD:
                    compileExp(left);
                    emit("\tpushl\t%eax\n");
                    compileExp(right);
                    emit("\tpopl\t%edx\n");
                    emit("\taddl\t%edx, %eax\n");
                    break;
                case SUB:
                    compileExp(left);
                    emit("\tpushl\t%eax\n");
                    compileExp(right);
                    emit("\tpopl\t%edx\n");
                    emit("\tsubl\t%eax, %edx\n");
                    emit("\tmovl\t%edx, %eax\n");
                    break;
                case TIMES:
                    compileExp(left);
                    emit("\tpushl\t%eax\n");
                    compileExp(right);
                    emit("\tpopl\t%edx\n");
                    emit("\timul\t%edx\n");
                    break;
                case DIVIDE:
                    compileExp(left);
                    emit("\tpushl\t%eax\n");
                    compileExp(right);
                    //Exercise 5 : Check last movl number is $0
                    if((buf.lastIndexOf("0")) == buf.lastIndexOf("$")+1)
                    {
                    	System.out.println("Divied by zero!!\n");
                    	System.exit(1);
                    }
                    emit("\tpopl\t%edx\n");
                    emit("\tmovl\t%eax, %ecx\n");
                    emit("\tmovl\t%edx, %eax\n");
                    emit("\tcltd\n");
                    emit("\tdiv\t%ecx\n");
                    break;
                default:
                    new Bug();
            }
        } else if (exp instanceof Eseq) {
            Eseq e = (Eseq) exp;
            Stm.T stm = e.stm;
            Exp.T ee = e.exp;

            compileStm(stm);
            compileExp(ee);
        } else
            new Bug();
    }

    private void compileExpList(ExpList.T explist)
    {
        if (explist instanceof ExpList.Pair) {
            ExpList.Pair pair = (ExpList.Pair) explist;
            Exp.T exp = pair.exp;
            ExpList.T list = pair.list;

            compileExp(exp);
            emit("\tpushl\t%eax\n");
            emit("\tpushl\t$slp_format\n");
            emit("\tcall\tprintf\n");
            emit("\taddl\t$4, %esp\n");
            compileExpList(list);
        } else if (explist instanceof ExpList.Last) {
            ExpList.Last last = (ExpList.Last) explist;
            Exp.T exp = last.exp;

            compileExp(exp);
            emit("\tpushl\t%eax\n");
            emit("\tpushl\t$slp_format\n");
            emit("\tcall\tprintf\n");
            emit("\taddl\t$4, %esp\n");
        } else
            new Bug();
    }

    private void compileStm(Stm.T prog)
    {
        if (prog instanceof Stm.Compound) {
            Stm.Compound s = (Stm.Compound) prog;
            Stm.T s1 = s.s1;
            Stm.T s2 = s.s2;

            compileStm(s1);
            compileStm(s2);
        } else if (prog instanceof Stm.Assign) {
            Stm.Assign s = (Stm.Assign) prog;
            String id = s.id;
            Exp.T exp = s.exp;

            ids.add(id);
            compileExp(exp);
            emit("\tmovl\t%eax, " + id + "\n");
        } else if (prog instanceof Stm.Print) {
            Stm.Print s = (Stm.Print) prog;
            ExpList.T explist = s.explist;

            compileExpList(explist);
            emit("\tpushl\t$newline\n");
            emit("\tcall\tprintf\n");
            emit("\taddl\t$4, %esp\n");
        } else
            new Bug();
    }

    // ////////////////////////////////////////
    public void doit(Stm.T prog)
    {
        // return the maximum number of arguments
        if (Control.ConSlp.action == Control.ConSlp.T.ARGS) {
            int numArgs = maxArgs(prog);
            System.out.println(numArgs);
        }

        // interpret a given program
        if (Control.ConSlp.action == Control.ConSlp.T.INTERP) {
            interp(prog);
        }

        // compile a given SLP program to x86
        if (Control.ConSlp.action == Control.ConSlp.T.COMPILE) {
            ids = new HashSet<String>();
            buf = new StringBuffer();

            compileStm(prog);
            try {
                // FileOutputStream out = new FileOutputStream();
                FileWriter writer = new FileWriter("slp_gen.s");
                writer
                    .write("// Automatically generated by the Tiger compiler, do NOT edit.\n\n");
                writer.write("\t.data\n");
                writer.write("slp_format:\n");
                writer.write("\t.string \"%d \"\n");
                writer.write("newline:\n");
                writer.write("\t.string \"\\n\"\n");
                for (String s : this.ids) {
                    writer.write(s + ":\n");
                    writer.write("\t.int 0\n");
                }
                writer.write("\n\n\t.text\n");
                writer.write("\t.globl main\n");
                writer.write("main:\n");
                writer.write("\tpushl\t%ebp\n");
                writer.write("\tmovl\t%esp, %ebp\n");
                writer.write(buf.toString());
                writer.write("\tleave\n\tret\n\n");
                writer.close();
                Process child = Runtime.getRuntime().exec("gcc slp_gen.s");
                child.waitFor();
                if (!Control.ConSlp.keepasm)
                    Runtime.getRuntime().exec("rm -rf slp_gen.s");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
             System.out.println(buf.toString());
        }
    }
}
