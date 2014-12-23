#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    -----------------------------------------
      | vptr | v0 | v1 | ...      | v_{size-1}|                           
      -----------------------------------------
      ^      \                                /
      |       \<------------- size --------->/
      |
      p (returned address)
*/
void *Tiger_new (void *vtable, int size)
{
    // You should write 4 statements for this function.
    // #1: "malloc" a chunk of memory (be careful of the size) :
  
    // #2: clear this chunk of memory (zero off it):
  
    // #3: set up the "vptr" pointer to the value of "vtable":
  
    // #4: return the pointer 
    
    // Lab3 Exercise7 by King
    int *p = NULL;

    p = malloc(sizeof(int) * (size+1));
    if(p == NULL)
	return NULL;

    memset(p, 0x0, size+1);
    *p = (int)vtable;
        
    return (void*) p;
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length.
// This function should return the starting address
// of the array elements, but not the starting address of
// the array chunk. 
/*    ---------------------------------------------
      | length | e0 | e1 | ...      | e_{length-1}|                           
      ---------------------------------------------
               ^
               |
               p (returned address)
*/
void *Tiger_new_array (int length)
{
    // You can use the C "malloc" facilities, as above.
    // Your code here:
    
    // Lab3 Exercise7 by King
    int *q = NULL;
    int *p = NULL;

    q = malloc(sizeof(int) * (length+1));
    if(q == NULL)
        return NULL;
    
    *q = length;
    p = q+1;

    return (void*)p;
}
