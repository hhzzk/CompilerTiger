package lexer;

import static control.Control.ConLexer.dump;

import java.io.InputStream;
import java.util.Iterator;

import lexer.Token.Kind;
import util.Bug;

public class Lexer
{
    String fname; // the input file name to be compiled
    InputStream fstream; // input stream for the above file
    int LineNum = 1;

    public Lexer(String fname, InputStream fstream)
    {
        this.fname = fname;
        this.fstream = fstream;
    }
    
    // skip all kinds of "blanks"
    private int skipBlanks(int c) throws Exception
    {
    	while (' ' == c || '\t' == c || '\n' == c || '\r' == c) 
    	{
    		if ('\n' == c || '\r' == c)
    			this.LineNum++;
    		c = this.fstream.read();
    	}
    	return c;
    }
    
    // Skip Comments
    private int skipComments(int c) throws Exception
    {
		c = this.fstream.read();			
        if('/' == c)
        {
        	while(c != '\n' && c != '\r')
        	{
        		c = this.fstream.read();
        	}
        	this.LineNum++;
        }
        else if ('*' == c)
        {
        	int ex = this.fstream.read();
			while (c != '*' || ex != '/')
			{
				c = ex;
				ex = this.fstream.read();
			}
       	}
       	else
       	{
       		new Bug();
       	}
        	
       	return skipBlanks(c);
    }
    
    // When called, return the next token (refer to the code "Token.java")
    // from the input stream.
    // Return TOKEN_EOF when reaching the end of the input stream.
    private Token nextTokenInternal() throws Exception
    {
        int c = this.fstream.read();
        if (-1 == c)
            // The value for "lineNum" is now "null",
            // you should modify this to an appropriate
            // line number for the "EOF" token.
            return new Token(Kind.TOKEN_EOF, null);
        
        // skip blanks
        c = skipBlanks(c);
        
        // skip commendts
        if ('/' == c)
        	c = skipComments(c);

        if (-1 == c)
            return new Token(Kind.TOKEN_EOF, null);
        
        switch (c) 
        {
            case '+':
                return new Token(Kind.TOKEN_ADD, this.LineNum);
            case '=':
                return new Token(Kind.TOKEN_ASSIGN, this.LineNum);
            case ',':
                return new Token(Kind.TOKEN_COMMER, this.LineNum);
            case '.':
                return new Token(Kind.TOKEN_DOT, this.LineNum);
            case '{':
                return new Token(Kind.TOKEN_LBRACE, this.LineNum);
            case '}':
                return new Token(Kind.TOKEN_RBRACE, this.LineNum);
            case '[':
                return new Token(Kind.TOKEN_LBRACK, this.LineNum);
            case ']':
                return new Token(Kind.TOKEN_RBRACK, this.LineNum);
            case '(':
                return new Token(Kind.TOKEN_LPAREN, this.LineNum);
            case ')':
                return new Token(Kind.TOKEN_RPAREN, this.LineNum);
            case '<':
                return new Token(Kind.TOKEN_LT, this.LineNum);
            case '!':
                return new Token(Kind.TOKEN_NOT, this.LineNum);
            case ';':
                return new Token(Kind.TOKEN_SEMI, this.LineNum);
            case '-':
                return new Token(Kind.TOKEN_SUB, this.LineNum);
            case '*':
                return new Token(Kind.TOKEN_TIMES, this.LineNum);
            case '&':
            {
            	c = this.fstream.read();
            	if ( c != '&')
            	{
            		new Bug();
            		return null;
            	}
            	return new Token(Kind.TOKEN_AND, this.LineNum);
            }
            default:
    			StringBuffer strbuf = new StringBuffer();
    			char temp = (char)c;
    			
    			if(Character.isDigit(temp))
    			{
    				this.fstream.mark(0);
    				while(Character.isDigit(temp))
    				{
    					this.fstream.mark(0);
    					strbuf.append(temp);
    					c=this.fstream.read();
    					temp=(char)c;
    				}
    				this.fstream.reset();
    				
    				return new Token(Kind.TOKEN_NUM,this.LineNum,strbuf.toString());
    			}
    			else if(Character.isLetter(temp)|| c == '_')
    			{
    				this.fstream.mark(0);
    				while (Character.isDigit(c) || Character.isLetter(c)|| c == '_')
    				{				
    					this.fstream.mark(0);					
    					strbuf.append(temp);
    					c = this.fstream.read();
    					temp = (char)c;
    				}
    				this.fstream.reset();
    				String str = strbuf.toString();
    				Iterator<String> it = lexer.Token.token_string.keySet().iterator();
    				while(it.hasNext())
    				{
    					if(it.next().equals(str))
    					{
    						Kind kind = lexer.Token.token_string.get(str);
    						return new Token(kind, this.LineNum);
    					}
    				}
    				
    				return new Token(Kind.TOKEN_ID, this.LineNum, str);
    			}
 
                return null;
        }
    }

    public Token nextToken()
    {
        Token t = null;

        try {
            t = this.nextTokenInternal();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (dump)
            System.out.println(t.toString());
        return t;
    }
}
