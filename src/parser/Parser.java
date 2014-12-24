package parser;

import java.util.LinkedList;
import java.lang.Integer;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;
import ast.Ast.Program;
import ast.Ast.Exp;
import ast.Ast.Stm;
import ast.Ast.Class;
import ast.Ast.Dec;
import ast.Ast.MainClass;
import ast.Ast.Method;
import ast.Ast.Type;

public class Parser
{
    Lexer lexer;
    Token current;
    Token next;
    Integer lineNum;
    int errorCount = 0; // Record the error count
    public static final int MAX_ERROR_NUM = 10; // The max error num
    
    public Parser(String fname, java.io.InputStream fstream)
    {
        lexer = new Lexer(fname, fstream);
        current = lexer.nextToken();
        if(current.kind != Kind.TOKEN_EOF)
        	next = lexer.nextToken();
        /*
        Token tem = current;
        while(tem.kind != Kind.TOKEN_EOF)
        {
            System.out.print(tem.kind + " ");
            tem = lexer.nextToken();
        }
        */
    }

    // /////////////////////////////////////////////
    // utility methods to connect the lexer
    // and the parser.

    private void advance()
    {
    	current.kind = next.kind;
    	current.lexeme = next.lexeme;
    	current.lineNum = next.lineNum;
    	
        if(current.kind != Kind.TOKEN_EOF)
        {
        	next = lexer.nextToken();
        }
        lineNum = current.lineNum; 
    }

    // Cheack Token kind,
    // If kind is wrong, print error information and return null
    // If kind is right, return current Token;
    private Token eatToken(Kind kind)
    {
    	Token savedCurrent = new Token(null,null,null);
    	
    	savedCurrent.kind = current.kind;
    	savedCurrent.lexeme = current.lexeme;
    	savedCurrent.lineNum = current.lineNum;
    	
    	advance();
        if (kind != savedCurrent.kind)
        {
        	if(errorCount <= MAX_ERROR_NUM)
        	{
        		errorCount++;
        		System.out.print("Line " + lineNum + " syntax error: ");
        		System.out.print("Expects: " + kind.toString() + ",");
        		System.out.print(" But got: " + savedCurrent.kind.toString() + "\n");
        		return null;
        	}
        	else
        	{	
        		System.exit(1);
        	}
        }
        
        return savedCurrent;
    }
    
    /*
    // Only check Token kind, if error print error information
    private boolean checkTokenKind(Kind kind)
    {
        if (kind != current.kind)
        {
        	if(errorCount <= MAX_ERROR_NUM)
        	{
        		errorCount++;
        		System.out.print("Line " + lineNum + " syntax error: ");
        		System.out.print("Expects: " + kind.toString() + ",");
        		System.out.print(" But got: " + current.kind.toString() + "\n");
        		return false;
        	}
        	else
        	{	
        		System.exit(1);
        	}
        }
        
        return true;
    }
    */
    
    private void error()
    {
    	if(errorCount <= MAX_ERROR_NUM)
    	{
    		errorCount++;
    		System.out.println("Line " + lineNum + " syntax error: compilation aborting...\n");
    	}
    	
    	return;
    }

    // ////////////////////////////////////////////////////////////
    // below are method for parsing.

    // A bunch of parsing methods to parse expressions. The messy
    // parts are to deal with precedence and associativity.

    // ExpList -> Exp ExpRest*
    // ->
    // ExpRest -> , Exp
    
    private LinkedList<Exp.T> parseExpList()
    {
    	Exp.T exp = null;
    	LinkedList<Exp.T> exps = new LinkedList<Exp.T>();
    	
        if (current.kind == Kind.TOKEN_RPAREN)
            return exps;
        
        exp = parseExp();
        exps.addLast(exp);
        
        while (current.kind == Kind.TOKEN_COMMER)
        {
            advance();
            exp = parseExp();
            exps.addLast(exp);
        }

        return exps;
    }

    // AtomExp -> (exp)
    // -> INTEGER_LITERAL
    // -> true
    // -> false
    // -> this
    // -> id
    // -> new int [exp]
    // -> new id ()
    private Exp.T parseAtomExp()
    {
    	Exp.T exp = null;
    	
        switch (current.kind)
        {
            case TOKEN_LPAREN:
                advance();
                exp = parseExp();
                eatToken(Kind.TOKEN_RPAREN);
                return exp;
            case TOKEN_NUM:
            	exp = new Exp.Num(Integer.parseInt(current.lexeme), lineNum);
                advance();
                return exp;
            case TOKEN_TRUE:
            	exp = new Exp.True(lineNum);
                advance();
                return exp;
            case TOKEN_FALSE:
            	exp = new Exp.False(lineNum);
                advance();
                return exp;
            case TOKEN_THIS:
            	exp = new Exp.This(lineNum);
                advance();
                return exp;
            case TOKEN_ID:
            	exp = new Exp.Id(current.lexeme, lineNum);
                advance();
                return exp;
            case TOKEN_NEW:
                {
                    advance();
                    switch (current.kind)
                    {
                        case TOKEN_INT:
                            advance();
                            eatToken(Kind.TOKEN_LBRACK);
                            Exp.T exp_new_int = parseExp();
                            eatToken(Kind.TOKEN_RBRACK);
                            exp = new Exp.NewIntArray(exp_new_int, lineNum);
                            return exp;
                        case TOKEN_ID:
                        	String id_new_id = current.lexeme;
                            advance();
                            eatToken(Kind.TOKEN_LPAREN);
                            eatToken(Kind.TOKEN_RPAREN);
                            exp = new Exp.NewObject(id_new_id, lineNum);
                            return exp;
                        default:
                            error();
                            return exp;
                    }
                }
            default:
                error();
                return exp;
        }
    }

    // NotExp -> AtomExp
    // -> AtomExp .id (expList)
    // -> AtomExp [exp]
    // -> AtomExp .length
    private Exp.T parseNotExp()
    {
        Exp.T exp = null;
        Exp.T atom = null;
        
    	atom = parseAtomExp();
        while ( current.kind == Kind.TOKEN_DOT ||
                current.kind == Kind.TOKEN_LBRACK )
        {
            if (current.kind == Kind.TOKEN_DOT)
            {
                advance();
                if (current.kind == Kind.TOKEN_LENGTH)
                {
                	exp = new Exp.Length(atom, lineNum);
                    advance();
                    return exp;
                }
                else
                {
                	Token temp = null;
                	String id_dot_id = null;
                	LinkedList<Exp.T> args = null;
                	
                	temp = eatToken(Kind.TOKEN_ID);
                	if(temp != null)
                	{
                		id_dot_id = temp.lexeme;
                	}
                	eatToken(Kind.TOKEN_LPAREN);
                	args = parseExpList();
                	eatToken(Kind.TOKEN_RPAREN);
                	exp = new Exp.Call(atom, id_dot_id, args, lineNum);
                	
                	return exp;
                }
            } 
            else
            {
            	Exp.T index = null;
            	
                advance();
                index = parseExp();
                eatToken(Kind.TOKEN_RBRACK);
                exp = new Exp.ArraySelect(atom, index, lineNum);
                
                return exp;
            }
        }

        return atom;
    }

    // TimesExp -> ! TimesExp
    // -> NotExp
    private Exp.T parseTimesExp()
    {
    	Exp.T exp = null;
		int notCount = 0; // Record "!" count
		int i = 0;
		
		while (current.kind == Kind.TOKEN_NOT)
		{
			notCount += 1;
			advance();
		}
		exp = parseNotExp();
		for (i = 0; i < notCount; i++)
			exp = new Exp.Not(exp, lineNum);
		
		return exp;
    }
    
    // AddSubExp -> TimesExp * TimesExp
    // -> TimesExp
    private Exp.T parseAddSubExp()
    {
    	Exp.T exp = null;
    	Exp.T left = null;
    	Exp.T right = null;
    	
        left = parseTimesExp();
        
        while (current.kind == Kind.TOKEN_TIMES)
        {
            advance();
            right = parseTimesExp();
            exp = new Exp.Times(left, right, lineNum);
            return exp;
        }

        return left;
    }

    // LtExp -> AddSubExp + AddSubExp
    // -> AddSubExp - AddSubExp
    // -> AddSubExp
    private Exp.T parseLtExp()
    {
    	Exp.T exp = null;
    	Exp.T left = null;
    	Exp.T right = null;
    	
        left = parseAddSubExp();
        
        while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB)
        {
            if(current.kind == Kind.TOKEN_ADD)
            {
            	advance();
            	right = parseAddSubExp();
            	exp = new Exp.Add(left, right, lineNum);
            }
            else
            {
            	advance();
            	right = parseAddSubExp();
            	exp = new Exp.Sub(left, right, lineNum);
            }
            return exp;
        }

        return left;
    }

    // AndExp -> LtExp < LtExp
    // -> LtExp
    private Exp.T parseAndExp()
    {
    	Exp.T exp = null;
    	Exp.T left = null;
    	Exp.T right = null;
    	
        left = parseLtExp();
        
        while (current.kind == Kind.TOKEN_LT)
        {
            advance();
            right = parseLtExp();
            exp = new Exp.Lt(left, right, lineNum);
            return exp;
        }
        
        return left;
    }

    // Exp -> AndExp && AndExp
    // -> AndExp
    private Exp.T parseExp()
    {
    	Exp.T exp = null;
    	Exp.T left = null;
    	Exp.T right = null;
        
    	left = parseAndExp();
    	
        while (current.kind == Kind.TOKEN_AND)
        {
            advance();
            right = parseAndExp();
            exp = new Exp.And(left, right, lineNum);
            return exp;
        }
        
        return left;
    }

    // Statement -> { Statement* }
    // -> if ( Exp ) Statement else Statement
    // -> while ( Exp ) Statement
    // -> System.out.println ( Exp ) ;
    // -> id = Exp ;
    // -> id [ Exp ]= Exp ;
    private Stm.T parseStatement()
    {
    	Stm.T stm = null;
    	Token temp = null;
    	
        if ( current.kind == Kind.TOKEN_LBRACE )
        {
        	LinkedList<Stm.T> stms = null;
        	
            eatToken(Kind.TOKEN_LBRACE);
            stms = parseStatements();
            eatToken(Kind.TOKEN_RBRACE);
            stm = new Stm.Block(stms, lineNum);
            return stm;
        }
        else if ( current.kind == Kind.TOKEN_IF )
        {
        	Exp.T condition = null;
        	Stm.T thenn = null;
        	Stm.T elsee = null;
        	
            eatToken(Kind.TOKEN_IF);
            eatToken(Kind.TOKEN_LPAREN);
            condition = parseExp();
            eatToken(Kind.TOKEN_RPAREN);
            thenn = parseStatement();
            eatToken(Kind.TOKEN_ELSE);
            elsee = parseStatement();
            stm = new Stm.If(condition, thenn, elsee, lineNum);
            return stm;
        }
        else if ( current.kind == Kind.TOKEN_WHILE)
        {
        	Exp.T condition = null;
        	Stm.T body = null;
        	
            eatToken(Kind.TOKEN_WHILE);
            eatToken(Kind.TOKEN_LPAREN);
            condition = parseExp();
            eatToken(Kind.TOKEN_RPAREN);
            body = parseStatement();
            stm = new Stm.While(condition, body, lineNum);
            return stm;
        }
        else if ( current.kind == Kind.TOKEN_SYSTEM)
        {
        	Exp.T exp_print;
        	
            eatToken(Kind.TOKEN_SYSTEM);
            eatToken(Kind.TOKEN_DOT);
            eatToken(Kind.TOKEN_OUT);
            eatToken(Kind.TOKEN_DOT);
            eatToken(Kind.TOKEN_PRINTLN);
            eatToken(Kind.TOKEN_LPAREN);
            exp_print = parseExp();
            eatToken(Kind.TOKEN_RPAREN);
            eatToken(Kind.TOKEN_SEMI);
            stm = new Stm.Print(exp_print, lineNum);
            return stm;
        }
        else if ( current.kind == Kind.TOKEN_ID)
        {
        	String id = null;
        	
            temp = eatToken(Kind.TOKEN_ID);
            id = temp.lexeme;
            if ( current.kind == Kind.TOKEN_ASSIGN )
            {
            	Exp.T exp_assign = null;
            	
                eatToken(Kind.TOKEN_ASSIGN);
                exp_assign = parseExp();
                eatToken(Kind.TOKEN_SEMI);
                stm = new Stm.Assign(id, exp_assign, lineNum);
                return stm;
            }
            else if ( current.kind == Kind.TOKEN_LBRACK )
            {
                Exp.T index = null;
                Exp.T exp_array = null;
                
            	eatToken(Kind.TOKEN_LBRACK);
                index = parseExp();
                eatToken(Kind.TOKEN_RBRACK);
                eatToken(Kind.TOKEN_ASSIGN);
                exp_array = parseExp();
                eatToken(Kind.TOKEN_SEMI);
                stm = new Stm.AssignArray(id, index, exp_array, lineNum);
                return stm;
            }
            else
            {
                error();
            }
        }
        else
        {
            error();
        }

        return stm;
    }
    

    // Statements -> Statement Statements
    // ->
    private LinkedList<Stm.T> parseStatements()
    {
    	LinkedList<Stm.T> stms = new LinkedList<Stm.T>();
    	Stm.T stm = null;
    	
        while ( current.kind == Kind.TOKEN_LBRACE ||
                current.kind == Kind.TOKEN_IF ||
                current.kind == Kind.TOKEN_WHILE ||
                current.kind == Kind.TOKEN_SYSTEM ||
                current.kind == Kind.TOKEN_ID )
        {
            stm = parseStatement();
            stms.addLast(stm);
        }
        
        return stms;
    }

    // Type -> int []
    // -> boolean
    // -> int
    // -> id
    private Type.T parseType()
    {
    	Type.T type = null;
    	
        if( current.kind == Kind.TOKEN_BOOLEAN)
        {
        	type = new Type.Boolean(lineNum);
            advance();
        }
        else if ( current.kind == Kind.TOKEN_INT )
        {
        	type = new Type.Int(lineNum);
            advance();
            if ( current.kind == Kind.TOKEN_LBRACK)
            {
                type = new Type.IntArray(lineNum);
            	advance();
            	advance();
            }
        }
        else if ( current.kind == Kind.TOKEN_ID)
        {
            String id = current.lexeme;
        	type = new Type.ClassType(id, lineNum);
        	advance();
        }

        return type;
    }

    // VarDecl -> Type id ;
    private Dec.T parseVarDecl()
    {
        // to parse the "Type" nonterminal in this method, instead of writing
        // a fresh one.
    	Dec.T dec = null;
    	Type.T type = null;
    	String id = null;
    	Token temp = null;
    	
        type = parseType();
        temp = eatToken(Kind.TOKEN_ID);
        if(temp != null)
        {
        	id = temp.lexeme;
        }
        eatToken(Kind.TOKEN_SEMI);
        
        dec = new Dec.DecSingle(type, id, lineNum);
        
        return dec;
    }

    // VarDecls -> VarDecl VarDecls
    // ->
    private LinkedList<Dec.T> parseVarDecls()
    {
    	LinkedList<Dec.T> decs = new LinkedList<Dec.T>();
    	Dec.T dec = null;
    	
        while ( current.kind == Kind.TOKEN_INT || 
                current.kind == Kind.TOKEN_BOOLEAN ||
                current.kind == Kind.TOKEN_ID )
        {
        	if(current.kind == Kind.TOKEN_ID && 
        	   next.kind == Kind.TOKEN_ASSIGN)
        	{
        			return decs;
        	}
            dec = parseVarDecl();
            decs.addLast(dec);
        }

        return decs;
    }

    // FormalList -> Type id FormalRest*
    // ->
    // FormalRest -> , Type id
    private LinkedList<Dec.T> parseFormalList()
    {
    	LinkedList<Dec.T> decs = new LinkedList<Dec.T>();
    	Dec.T dec = null;
    	Type.T type = null;
    	String id = null;
    	Token temp = null;
    	
        if (current.kind == Kind.TOKEN_INT || 
                current.kind == Kind.TOKEN_BOOLEAN || 
                current.kind == Kind.TOKEN_ID)
        {
            type = parseType();
            temp = eatToken(Kind.TOKEN_ID);
            if (temp != null)
            {
            	id = temp.lexeme;
            }
            dec = new Dec.DecSingle(type, id, lineNum);
            decs.addLast(dec);
            
            while (current.kind == Kind.TOKEN_COMMER)
            {
                advance();
                type = parseType();
                eatToken(Kind.TOKEN_ID);
                if (temp != null)
                {
                	id = temp.lexeme;
                }
                dec = new Dec.DecSingle(type, id, lineNum);
                decs.addLast(dec);
            }
        }

        return decs;
    }

    // Method -> public Type id ( FormalList )
    // { VarDecl* Statement* return Exp ;}
    private Method.T parseMethod()
    {
    	Method.T method = null;
    	Type.T retType = null;
    	String id = null;
        LinkedList<Dec.T> formals = null;
        LinkedList<Dec.T> locals = null;
        LinkedList<Stm.T> stms = null;
        Exp.T retExp = null;
        Token temp = null;
        
        eatToken(Kind.TOKEN_PUBLIC);
        retType = parseType();
        temp = eatToken(Kind.TOKEN_ID);
        if (temp != null)
        {
        	id = temp.lexeme;
        }
        eatToken(Kind.TOKEN_LPAREN);
        formals = parseFormalList();
        eatToken(Kind.TOKEN_RPAREN);
        eatToken(Kind.TOKEN_LBRACE);
        locals = parseVarDecls();
        stms = parseStatements();
        eatToken(Kind.TOKEN_RETURN);
        retExp = parseExp();
        eatToken(Kind.TOKEN_SEMI);
        eatToken(Kind.TOKEN_RBRACE);

        method = new Method.MethodSingle(retType, id, formals, locals, stms, retExp, lineNum);
        
        return method;
    }

    // MethodDecls -> MethodDecl MethodDecls
    // ->
    private LinkedList<Method.T> parseMethodDecls()
    {
    	LinkedList<Method.T> methods = new LinkedList<Method.T>();
    	Method.T method;
    	
        while (current.kind == Kind.TOKEN_PUBLIC)
        {
            method = parseMethod();
            methods.addLast(method);
        }

        return methods;
    }

    // ClassDecl -> class id { VarDecl* MethodDecl* }
    // -> class id extends id { VarDecl* MethodDecl* }
    private Class.T parseClassDecl()
    {
    	Class.T classe = null;
    	Token temp = null;
    	String id = null;
    	String extendss = null;
        LinkedList<Dec.T> decs = null;
        LinkedList<Method.T> methods = null;
    	
        eatToken(Kind.TOKEN_CLASS);
        temp = eatToken(Kind.TOKEN_ID);
        if(temp != null)
        {
        	id = temp.lexeme;
        }
        if (current.kind == Kind.TOKEN_EXTENDS)
        {
            eatToken(Kind.TOKEN_EXTENDS);
            temp = eatToken(Kind.TOKEN_ID);
            if(temp != null)
            {
            	extendss = temp.lexeme;
            }
        }
        eatToken(Kind.TOKEN_LBRACE);
        decs = parseVarDecls();
        methods = parseMethodDecls();
        eatToken(Kind.TOKEN_RBRACE);
        
        classe = new Class.ClassSingle(id, extendss, decs, methods, lineNum);
        
        return classe;
    }

    // ClassDecls -> ClassDecl ClassDecls
    // ->
    private LinkedList<Class.T> parseClassDecls()
    {
    	LinkedList<Class.T> classes = new LinkedList<Class.T>();
    	Class.T classe = null;
    	
        while (current.kind == Kind.TOKEN_CLASS)
        {
            classe = parseClassDecl();
            classes.addLast(classe);
        }

        return classes;
    }

    // MainClass -> class id
    // {
    // public static void main ( String [] id )
    // {
    // Statement
    // }
    // }
    private MainClass.T parseMainClass()
    {
    	Token temp = null;
    	MainClass.T mainClass;
    	String id = null;
    	String arg = null;
    	Stm.T stm = null;
    	
        eatToken(Kind.TOKEN_CLASS);
        temp = eatToken(Kind.TOKEN_ID);
        if (temp != null)
        {
        	id = temp.lexeme;
        }
        eatToken(Kind.TOKEN_LBRACE);
        eatToken(Kind.TOKEN_PUBLIC);
        eatToken(Kind.TOKEN_STATIC);
        eatToken(Kind.TOKEN_VOID);
        eatToken(Kind.TOKEN_MAIN);
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_STRING);
        eatToken(Kind.TOKEN_LBRACK);
        eatToken(Kind.TOKEN_RBRACK);
        temp = eatToken(Kind.TOKEN_ID);
        if (temp != null)
        {
        	arg = temp.lexeme;
        }
        eatToken(Kind.TOKEN_RPAREN);
        eatToken(Kind.TOKEN_LBRACE);
        stm = parseStatement();
        eatToken(Kind.TOKEN_RBRACE);
        eatToken(Kind.TOKEN_RBRACE);

        mainClass = new MainClass.MainClassSingle(id, arg, stm, lineNum);
        
        return mainClass;
    }

    // Program -> MainClass ClassDecl*
    private Program.T parseProgram()
    {
    	Program.T prog = null;
    	MainClass.T mainClass = null;
        LinkedList<Class.T> classes = null; 
        
        mainClass = parseMainClass();
        classes = parseClassDecls();
        eatToken(Kind.TOKEN_EOF);
        
        prog = new Program.ProgramSingle(mainClass, classes);
        
        return prog;
    }

    public Program.T parse()
    {
    	Program.T prog;
        prog = parseProgram();
        
        return prog;
    }
}
