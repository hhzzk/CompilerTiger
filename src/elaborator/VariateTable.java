package elaborator;

import java.util.Iterator;
import java.util.LinkedList;

import ast.Ast.Dec;


public class VariateTable
{
  private java.util.Hashtable<String, Integer> table;

  public VariateTable()
  {
    this.table = new java.util.Hashtable<String, Integer>();
  }

  public void put(String key, Integer val)
  {
	  this.table.put(key, val);
	  return;
  }
  
  public void put(LinkedList<Dec.T> formals,
      LinkedList<Dec.T> locals)
  {
    for (Dec.T dec : formals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("duplicated parameter: " + decc.id);
        System.exit(1);
      }
      this.table.put(decc.id, decc.lineNum);
    }

    for (Dec.T dec : locals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("duplicated variable: " + decc.id);
        System.exit(1);
      }
      this.table.put(decc.id, decc.lineNum);
    }

  }

  // return null for non-existing keys
  public Integer get(String id)
  {
    return this.table.get(id);
  }
  
  public void remove(String id)
  {
	  this.table.remove(id);
	  return;
  }
  
  public void dump()
  {
	  for(Iterator it = table.keySet().iterator(); it.hasNext();)   
	  {   
          String key = (String) it.next();  
          Integer val = (Integer)table.get(key);
          System.out.format("Warning: The variable %s in line %d is not used!\n", key, val);
	  }
  }
}
