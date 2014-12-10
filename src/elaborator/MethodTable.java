package elaborator;

import java.util.Iterator;
import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;
import util.Todo;

public class MethodTable
{
  private java.util.Hashtable<String, Type.T> table;

  public MethodTable()
  {
    this.table = new java.util.Hashtable<String, Type.T>();
  }

  // Duplication is not allowed
  public void put(LinkedList<Dec.T> formals,
      LinkedList<Dec.T> locals)
  {
    for (Dec.T dec : formals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("duplicated parameter: " + decc.id);
        System.exit(1);
      }
      this.table.put(decc.id, decc.type);
    }

    for (Dec.T dec : locals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("duplicated variable: " + decc.id);
        System.exit(1);
      }
      this.table.put(decc.id, decc.type);
    }

  }

  // return null for non-existing keys
  public Type.T get(String id)
  {
    return this.table.get(id);
  }

  public void dump()
  {
	  System.out.println("####### Method Dump ########");
	  System.out.println("var  :  var type");
	  for(Iterator it_table = table.keySet().iterator(); it_table.hasNext();)   
	  {   
          String key_table = (String) it_table.next(); 
          Type.T val_table = (Type.T)table.get(key_table);
          System.out.format("%s : %s\n", key_table, val_table.toString());
          System.out.println("~~~~~~~~~~~~~~~~~~~");
	  }
  }
  
  @Override
  public String toString()
  {
    return this.table.toString();
  }
}
