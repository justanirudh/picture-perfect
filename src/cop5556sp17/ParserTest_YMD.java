package cop5556sp17;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.junit.Assert.assertNull;


public class ParserTest_YMD
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static class ParserTestCase
    {
        private String input;
        private String method;

        public ParserTestCase(String input, String method)
        {
            this.input = input;
            this.method = method;
        }

        public ParserTestCase(String input)
        {
            this(input, "parse");
        }

        public void test() throws IllegalCharException, IllegalNumberException, SyntaxException
        {
            Scanner scanner = new Scanner(this.input);
            scanner.scan();
            Parser parser = new Parser(scanner);
            try
            {
                Parser.class.getDeclaredMethod(this.method).invoke(parser);
                Scanner.Token t = scanner.nextToken();
                System.out.println(t == null ? "null" : t.kind);
//                assertNull(t);
            }
            catch( SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e )
            {
                e.printStackTrace();
            }
            catch( java.lang.reflect.InvocationTargetException e )
            {
                Exception innerException = ( Exception ) e.getCause();
                if( innerException.getClass() == SyntaxException.class )
                {
                    throw ( SyntaxException ) innerException;
                }
                else if( innerException.getClass() == IllegalCharException.class )
                {
                    throw ( IllegalCharException ) innerException;
                }
                else if( innerException.getClass() == IllegalNumberException.class )
                {
                    throw ( IllegalNumberException ) innerException;
                }
                else
                {
                    innerException.printStackTrace();
                }
            }
        }

        public static void test(
            String input, String method
        ) throws IllegalCharException, IllegalNumberException, SyntaxException
        {
            (new ParserTestCase(input, method)).test();
        }
    }

    @Test
    public void testFactors() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] tests = {"abc", "123", "true", "false", "screenwidth", "screenheight"};
        for( String test : tests )
        {
            ParserTestCase.test(test, "factor");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testElemSingle() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        // The code below runs all single level nestings of the form factor strongOp factor
        // Multi level
        String[] elems = {"a * b", "gamma / beta", "truthy & falsey", "screenwidth % intvar2"};

        for( String elem : elems )
        {
            ParserTestCase.test(elem, "elem");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testElemGeneral() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] elems = {
            "a * b & gamma / beta",
            "truthy & falsey * screenwidth % screenheight"
        };

        for( String elem : elems )
        {
            ParserTestCase.test(elem, "elem");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testTerm() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] terms = {
            "(a * b & gamma / beta) + (screenwidth % screenheight)"
        };

        for( String term : terms )
        {
            ParserTestCase.test(term, "term");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testExpression() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] exprs = {
            "(abs < 3)",
            "(abc * 123 / true) == g",
            "(123 % screenwidth + screenwidth / screenheight) != 156 / a",
            "(123 % true + screen) >= 15598826 / a"
        };

        for( String expr : exprs )
        {
            ParserTestCase.test(expr, "expression");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testArguments() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] args = {
            "(3)",
            "((2352))",
            "((((3))))",
            "(3,5)",
            "((abc * 123 / true) == g, " +
                "(123 % screenwidth + screenwidth / screenheight) != 156 / a, " +
                "(123 % true + screen) >= 15598826 / a)"
        };

        for( String arg : args )
        {
            ParserTestCase.test(arg, "arg");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testArgumentErrorSimple() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        // error introduced: the hanging comma at the very end of the expression
        String[] args = {"(3,)"};

        for( String arg : args )
        {
            thrown.expect(SyntaxException.class);
            ParserTestCase.test(arg, "arg");
            System.out.println("--------------------------");
        }
    }

    public void testArgumentErrorWithExpressions() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] args = {
            "((1,2), (3,4))",
            "()",
            "((abc * 123 / true) == g, " +
                "(123 % screenwidth + screenwidth / screenheight) != 156 / a, "
        };

        for( String arg : args )
        {
            thrown.expect(SyntaxException.class);
            ParserTestCase.test(arg, "arg");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testChainElemSimple() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] args = {
            "abc_ident",
            "blur(3,4)",
            "gray(0)",
            "convolve(1, 0, 1)",
            "move(0,46)",
            "xloc;",
            "yloc;",
            "width;",
            "height;"
        };

        for( String arg : args )
        {
            ParserTestCase.test(arg, "chainElem");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testChainElemErrors() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] args = {
            "abc_ident;",
            "blu(3,4)",
            "gry(0)",
            "conolve((1, 0, 1))",
            "move(0,46)",
            "xloc",
            "yloc",
            "width",
            "height"
        };

        for( String arg : args )
        {
            thrown.expect(SyntaxException.class);
            ParserTestCase.test(arg, "chainElem");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testChain() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] chains = {
            "a -> b",
            "a -> blur(3) -> convolve(1,2,3) -> xloc;",
            "width -> scale(2) -> a"
        };

        for( String chain : chains )
        {
            ParserTestCase.test(chain, "chain");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testChainErrors() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        thrown.expect(SyntaxException.class);
        ParserTestCase.test("width -> height", "chain");
        System.out.println("--------------------------");

        thrown.expect(SyntaxException.class);
        ParserTestCase.test("", "chain");
        System.out.println("--------------------------");
    }

    @Test
    public void testDeclarations() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] decs = {
            "integer aba_a",
            "boolean f",
            "image img",
            "frame fr"
        };

        for( String dec : decs )
        {
            ParserTestCase.test(dec, "dec");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testparamDeclarations() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] paramDecs = {
            "integer aba_a",
            "boolean f",
            "file something_txt",
            "url users_google_com"
        };

        for( String paramDec : paramDecs )
        {
            ParserTestCase.test(paramDec, "paramDec");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testStatements() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] stmts = {
            "sleep 10;",
            "if(i < 3) {i <- u + 1;}",
            "while(i < 40) { if(i % 2 == 0) { i <- i / 2;} }",
            "xloc -> scale -> move(0,0) -> blur -> convolve(1,1,1);",
            "e <- 1 + (1/2 + (1/6 + (1/24 + (1/120 + (1/720 + (1/5040))))));"
        };

        for( String stmt : stmts )
        {
            ParserTestCase.test(stmt, "statement");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testBlocks() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] blocks = {
            "{}",
            "{integer i \n boolean b\n}",
            "{sleep 10;}",
            "{integer i while(true) {  }}",
            "{integer i while(true) { sleep 10; }}",
            "{integer i while(true) { move(3) -> height; }}",
            "{i <- i + 1;}",
            "{integer i\n i <- 1;\n while(i < 40) { if(i % 2 == 0) { i <- i / 2;} if(i % 2 == 1) {i <- 3*i + 1;} }}",
        };

        for( String block : blocks )
        {
            ParserTestCase.test(block, "block");
            System.out.println("--------------------------");
        }
    }

    @Test
    public void testPrograms() throws IllegalCharException, IllegalNumberException, SyntaxException
    {
        String[] programs = {
            "null_program {}",
            "iden {integer i \n boolean b\n}",
            "sleeper_cell {sleep 10;}",
            "loop_de_loop {integer i while(true) {  }}",
            "an_actual_program_for_testing_collatz_conjecture " +
                "integer i, file img {" +
                "i <- 1; " +
                "img <- 0; " +
                "while(true) { " +
                "   if(i % 2 == 0) { i <- i / 2;} " +
                "   if(i % 2 == 1) {i <- 3*i + 1;} " +
                "   img -> move(0,i) -> blur(i/255) -> move(i, 0);}}"
        };

        for( String program : programs )
        {
            ParserTestCase.test(program, "parse");
            System.out.println("--------------------------");
        }
    }
}
