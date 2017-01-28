//package cop5556sp17;
//
//import static cop5556sp17.Scanner.Kind.*;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertThat;
//
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//
//import cop5556sp17.Scanner.IllegalCharException;
//import cop5556sp17.Scanner.IllegalNumberException;
//import cop5556sp17.Scanner.Kind;
//
//import java.util.HashSet;
//
//
//public class ScannerTest2
//{
//
//    @Rule
//    public ExpectedException thrown = ExpectedException.none();
//
//    @Test
//    public void testEmpty() throws IllegalCharException, IllegalNumberException
//    {
//        String input = "";
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//        Scanner.Token token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testWellFormedComments() throws IllegalCharException, IllegalNumberException
//    {
//        // This program should be read as equivalent to an empty string
//        String input = "/* This is a comment */\n/* Followed *\n by \n* another \n* comment *\n**********************/";
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//        Scanner.Token token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testMalFormedComments() throws IllegalCharException, IllegalNumberException
//    {
//        String input = "/* /* Scanned comment ends here */ IllegalCharException here */";
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//        Scanner.Token[] tokens = new Scanner.Token[4];
//        tokens[0] = scanner.nextToken();
//        tokens[1] = scanner.nextToken();
//        tokens[2] = scanner.nextToken();
//        tokens[3] = scanner.nextToken();
//
//        assertEquals(IDENT, tokens[0].kind);
//        assertEquals("IllegalCharException", tokens[0].getText());
//
//        assertEquals(IDENT, tokens[1].kind);
//        assertEquals("here", tokens[1].getText());
//
//        assertEquals(TIMES, tokens[2].kind);
//        assertEquals("*", tokens[2].getText());
//
//        assertEquals(DIV, tokens[3].kind);
//        assertEquals("/", tokens[3].getText());
//    }
//
//    @Test
//    public void testOpenComments() throws IllegalCharException, IllegalNumberException
//    {
//        String input = "/* This comment was mistakenly left open";
//        Scanner scanner = new Scanner(input);
//        thrown.expect(IllegalCharException.class);
//        scanner.scan();
//    }
//
//    @Test
//    public void testSemiConcat() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = ";;;";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Scanner.Token token;
//        String text = SEMI.text;
//
//        for(int i = 0; i < 3; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(SEMI, token.kind);
//            assertEquals(i, token.pos);
//            assertEquals(text, token.getText());
//            assertEquals(text.length(), token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testCommaConcat() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = ",,,,,";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Scanner.Token token;
//        String text = COMMA.text;
//
//        for(int i = 0; i < 5; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(COMMA, token.kind);
//            assertEquals(i, token.pos);
//            assertEquals(text, token.getText());
//            assertEquals(text.length(), token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testLParenConcat() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = "(((((";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Scanner.Token token;
//        String text = LPAREN.text;
//
//        for(int i = 0; i < 5; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(LPAREN, token.kind);
//            assertEquals(i, token.pos);
//            assertEquals(text, token.getText());
//            assertEquals(text.length(), token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testRParenConcat() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = ")))))";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Scanner.Token token;
//        String text = RPAREN.text;
//
//        for(int i = 0; i < 5; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(RPAREN, token.kind);
//            assertEquals(i, token.pos);
//            assertEquals(text, token.getText());
//            assertEquals(text.length(), token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testLBraceConcat() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = "{{{{{";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Scanner.Token token;
//        String text = LBRACE.text;
//
//        for(int i = 0; i < 5; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(LBRACE, token.kind);
//            assertEquals(i, token.pos);
//            assertEquals(text, token.getText());
//            assertEquals(text.length(), token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testRBraceConcat() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = "}}}}}";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Scanner.Token token;
//        String text = RBRACE.text;
//
//        for(int i = 0; i < 5; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(RBRACE, token.kind);
//            assertEquals(i, token.pos);
//            assertEquals(text, token.getText());
//            assertEquals(text.length(), token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testPipeConcat() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = "|||||";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Scanner.Token token;
//        String text = OR.text;
//
//        for(int i = 0; i < 5; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(OR, token.kind);
//            assertEquals(i, token.pos);
//            assertEquals(text, token.getText());
//            assertEquals(text.length(), token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testAmpConcat() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = "&&&&&";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Scanner.Token token;
//        String text = AND.text;
//
//        for(int i = 0; i < 5; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(AND, token.kind);
//            assertEquals(i, token.pos);
//            assertEquals(text, token.getText());
//            assertEquals(text.length(), token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testEqualsSingle() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = "=";
//
//        Scanner scanner = new Scanner(input);
//        thrown.expect(IllegalCharException.class);
//        scanner.scan();
//    }
//
//    @Test
//    public void testEqualsDouble() throws IllegalCharException, IllegalNumberException
//    {
//
//        String input = "== ==   ==";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Scanner.Token token;
//        String text = EQUAL.text;
//
//        int[] position = {0, 3, 8};
//
//        for(int i = 0; i < 3; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(EQUAL, token.kind);
//            assertEquals(position[i], token.pos);
//            assertEquals(text, token.getText());
//            assertEquals(text.length(), token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testComparators() throws IllegalCharException, IllegalNumberException
//    {
//        // String containing all the comparators
//        String input = "><>=<=!===";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Kind[] kind = {GT, LT, GE, LE, NOTEQUAL, EQUAL};
//        int[] length = {1, 1, 2, 2, 2, 2};
//        int[] position = {0, 1, 2, 4, 6, 8};
//
//        Scanner.Token token;
//
//        for(int i = 0; i < 6; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(kind[i], token.kind);
//            assertEquals(position[i], token.pos);
//            assertEquals(kind[i].getText(), token.getText());
//            assertEquals(length[i], token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testOperators() throws IllegalCharException, IllegalNumberException
//    {
//        // String containing all the comparators
//        String input = "+-*/%";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Kind[] kind = {PLUS, MINUS, TIMES, DIV, MOD};
//        int[] position = {0, 1, 2, 3, 4};
//
//        Scanner.Token token;
//
//        for(int i = 0; i < 5; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(kind[i], token.kind);
//            assertEquals(position[i], token.pos);
//            assertEquals(kind[i].getText(), token.getText());
//            assertEquals(1, token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testArrowOperators() throws IllegalCharException, IllegalNumberException
//    {
//        // String containing all the comparators
//        String input = "-><-|->";
//
//        Scanner scanner = new Scanner(input);
//        scanner.scan();
//
//        Kind[] kind = {ARROW, ASSIGN, BARARROW};
//        int[] position = {0, 2, 4};
//        int[] length = {2, 2, 3};
//
//        Scanner.Token token;
//
//        for(int i = 0; i < 3; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(kind[i], token.kind);
//            assertEquals(position[i], token.pos);
//            assertEquals(kind[i].getText(), token.getText());
//            assertEquals(length[i], token.length);
//        }
//        // Check that EOF was inserted
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    /**
//     * This test illustrates how to check that the Scanner detects errors
//     * properly.
//     * In this test, the input contains an int literal with a value that exceeds
//     * the range of an int.
//     * The scanner should detect this and throw and IllegalNumberException.
//     *
//     * @throws IllegalCharException
//     * @throws IllegalNumberException
//     */
//    @Test
//    public void testIntOverflowError() throws IllegalCharException, IllegalNumberException
//    {
//        String input = "99999999999999999";
//        Scanner scanner = new Scanner(input);
//        thrown.expect(IllegalNumberException.class);
//        scanner.scan();
//    }
//
//    @Test
//    public void testIntegerLiteralZ() throws IllegalCharException, IllegalNumberException
//    {
//        String zero = "0";
//
//        Scanner scanner = new Scanner(zero);
//        scanner.scan();
//        Scanner.Token token = scanner.nextToken();
//        assertEquals(token.kind, INT_LIT);
//        assertEquals("0", token.getText());
//
//        // Confirm presence of an EOF token
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testIntegerLiteral() throws IllegalCharException, IllegalNumberException
//    {
//        String vanillaInt = "1234567890";
//
//        Scanner scanner = new Scanner(vanillaInt);
//        scanner.scan();
//
//        Scanner.Token token = scanner.nextToken();
//
//        assertEquals(INT_LIT, token.kind);
//        assertEquals(vanillaInt, token.getText());
//        assertEquals(10, token.length);
//
//        token = scanner.nextToken();
//
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testIntegerLiteralMixed() throws IllegalCharException, IllegalNumberException
//    {
//        String zeroAndMore = "012345678*3";
//
//        Scanner scanner = new Scanner(zeroAndMore);
//        scanner.scan();
//
//        Scanner.Token token = scanner.nextToken();
//
//        assertEquals(INT_LIT, token.kind);
//        assertEquals("0", token.getText());
//        assertEquals(1, token.length);
//
//        token = scanner.nextToken();
//
//        assertEquals(INT_LIT, token.kind);
//        assertEquals("12345678", token.getText());
//        assertEquals(8, token.length);
//
//        token = scanner.nextToken();
//
//        assertEquals(TIMES, token.kind);
//        assertEquals("*", token.getText());
//        assertEquals(1, token.length);
//
//        token = scanner.nextToken();
//
//        assertEquals(INT_LIT, token.kind);
//        assertEquals("3", token.getText());
//        assertEquals(1, token.length);
//
//        token = scanner.nextToken();
//
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testWhiteSpaceWithSingleCharTokens() throws IllegalCharException, IllegalNumberException
//    {
//        String input = " \n\t; ,   (\t\t)\n{\n }\t\n \t \n + \n*\n%\n&\n\n\n\n";
//        Scanner.Kind[] types = {SEMI, COMMA, LPAREN, RPAREN, LBRACE, RBRACE, PLUS, TIMES, MOD, AND};
//
//        Scanner sc = new Scanner(input);
//        Scanner.Token token;
//        sc.scan();
//
//        for(int i = 0; i < types.length; ++i)
//        {
//            token = sc.nextToken();
//
//            assertEquals(types[i], token.kind);
//            assertEquals(types[i].getText(), token.getText());
//            assertEquals(types[i].getText().length(), token.length);
//        }
//
//        token = sc.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testUnknownCharacters() throws IllegalCharException, IllegalNumberException
//    {
//        // All of these characters are not part of the specification and are handled with an IllegalCharException
//        String[] input = {"\\", ":", "\"", "'", "#", "@", "^", "[", "]", "?"};
//        Scanner scanner;
//
//        for(int i = 0; i < input.length; ++i)
//        {
//            scanner = new Scanner(input[i]);
//            thrown.expect(IllegalCharException.class);
//            scanner.scan();
//        }
//    }
//
//    @Test
//    public void testGEWithEquals() throws IllegalCharException, IllegalNumberException
//    {
//        // Input should be seen as  ==, >=, =
//        String input = "==>==";
//        Scanner s = new Scanner(input);
//        thrown.expect(IllegalCharException.class);
//        s.scan();
//    }
//
//    @Test
//    public void testLEWithEquals() throws IllegalCharException, IllegalNumberException
//    {
//        // Input should be seen as  ==, <=, =
//        String input = "==<==";
//        Scanner s = new Scanner(input);
//        thrown.expect(IllegalCharException.class);
//        s.scan();
//    }
//
//    @Test
//    public void testNotEqualWithEquals() throws IllegalCharException, IllegalNumberException
//    {
//        // Input should be seen as  ==, !=, =
//        String input = "==!==";
//        Scanner s = new Scanner(input);
//        thrown.expect(IllegalCharException.class);
//        s.scan();
//    }
//
//    @Test
//    public void testMixedSymbolsForArrows() throws IllegalCharException, IllegalNumberException
//    {
//        // Input should be seen as |, -, |->
//        String input = "|-|->";
//        Scanner s = new Scanner(input);
//        Scanner.Kind[] types = {OR, MINUS, BARARROW};
//
//        s.scan();
//        Scanner.Token token;
//        for(int i = 0; i < types.length; ++i)
//        {
//            token = s.nextToken();
//
//            assertEquals(types[i], token.kind);
//            assertEquals(types[i].getText(), token.getText());
//            assertEquals(types[i].getText().length(), token.length);
//        }
//
//        token = s.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testMixedSymbolsForLEAndGT() throws IllegalCharException, IllegalNumberException
//    {
//        // Input should be seen as <=, >
//        String input = "<=>";
//        Scanner s = new Scanner(input);
//        Scanner.Kind[] types = {LE, GT};
//
//        s.scan();
//        Scanner.Token token;
//        for(int i = 0; i < types.length; ++i)
//        {
//            token = s.nextToken();
//
//            assertEquals(types[i], token.kind);
//            assertEquals(types[i].getText(), token.getText());
//            assertEquals(types[i].getText().length(), token.length);
//        }
//
//        token = s.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testMixedSymbolsForGEAndLT() throws IllegalCharException, IllegalNumberException
//    {
//        // Input should be seen as <=, >
//        String input = ">=<";
//        Scanner s = new Scanner(input);
//        Scanner.Kind[] types = {GE, LT};
//
//        s.scan();
//        Scanner.Token token;
//        for(int i = 0; i < types.length; ++i)
//        {
//            token = s.nextToken();
//
//            assertEquals(types[i], token.kind);
//            assertEquals(types[i].getText(), token.getText());
//            assertEquals(types[i].getText().length(), token.length);
//        }
//
//        token = s.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testWhiteSpaceAsSeparatorForIllegals() throws IllegalCharException, IllegalNumberException
//    {
//        // Input should be seen as <, =
//        String input = "< = =";
//        Scanner s = new Scanner(input);
//        thrown.expect(IllegalCharException.class);
//        s.scan();
//    }
//
//    @Test
//    public void testWhiteSpaceAsSeparatorForLegals() throws IllegalCharException, IllegalNumberException
//    {
//        // Input should be seen as <, ==, >=, !, ==, |, -, >, ->, |->, /, *, *, *, /
//        String input = "< == >= ! != | - > -> |-> / ** \n * /";
//        Scanner.Kind[] types = {
//            LT, EQUAL, GE, NOT, NOTEQUAL, OR, MINUS, GT, ARROW, BARARROW, DIV, TIMES, TIMES, TIMES, DIV
//        };
//        Scanner s = new Scanner(input);
//
//        s.scan();
//        Scanner.Token token;
//
//        for(int i = 0; i < types.length; ++i)
//        {
//            token = s.nextToken();
//
//            assertEquals(types[i], token.kind);
//            assertEquals(types[i].getText(), token.getText());
//            assertEquals(types[i].getText().length(), token.length);
//        }
//
//        token = s.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//
//    @Test
//    public void testKeywords() throws IllegalCharException, IllegalNumberException
//    {
//        String input = "integer \n boolean \n image \n url \n file \n frame \n while \n if \n sleep \n " +
//            "screenheight \n screenwidth gray \n convolve \n blur \n scale width \n height xloc \n yloc " +
//            "\n hide \n show \n move";
//        Scanner.Kind[] types = {
//            KW_INTEGER, KW_BOOLEAN, KW_IMAGE, KW_URL, KW_FILE, KW_FRAME, KW_WHILE, KW_IF, OP_SLEEP, KW_SCREENHEIGHT,
//            KW_SCREENWIDTH, OP_GRAY, OP_CONVOLVE, OP_BLUR, KW_SCALE, OP_WIDTH, OP_HEIGHT, KW_XLOC, KW_YLOC, KW_HIDE,
//            KW_SHOW, KW_MOVE
//        };
////        HashSet<Scanner.Kind> keywordTypes = new HashSet<>((new Scanner("")).keyWordMap.values());
//
//        Scanner scanner = new Scanner(input);
//
//        scanner.scan();
//        Scanner.Token token;
//
//        for(int i = 0; i < types.length; ++i)
//        {
//            token = scanner.nextToken();
//
//            assertEquals(types[i], token.kind);
//            assertEquals(types[i].getText(), token.getText());
//            assertEquals(types[i].getText().length(), token.length);
//
////            assertEquals(true, keywordTypes.contains(token.kind));
//        }
//
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//
//    }
//
//    @Test
//    public void testIdentsWithKeywords() throws IllegalCharException, IllegalNumberException
//    {
//        String input = "integer \n booleans \n images \n url \n file \n frame \n while \n if \n sleep \n " +
//            "screenheight \n screenwidth gray \n convolve \n blur \n scale width \n height xlocsz \n yloc " +
//            "\n hide \n show \n moves";
//        Scanner.Kind[] types = {
//            KW_INTEGER, IDENT, IDENT, KW_URL, KW_FILE, KW_FRAME, KW_WHILE, KW_IF, OP_SLEEP, KW_SCREENHEIGHT,
//            KW_SCREENWIDTH, OP_GRAY, OP_CONVOLVE, OP_BLUR, KW_SCALE, OP_WIDTH, OP_HEIGHT, IDENT, KW_YLOC, KW_HIDE,
//            KW_SHOW, IDENT
//        };
//
//        Scanner scanner = new Scanner(input);
//
//        scanner.scan();
//        Scanner.Token token;
//
//        for(int i = 0; i < types.length; ++i)
//        {
//            token = scanner.nextToken();
//            assertEquals(types[i], token.kind);
//        }
//
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//
//    }
//
//    @Test
//    public void testIntegersWithIdents() throws IllegalCharException, IllegalNumberException
//    {
//        String input = "abc12301234 012345";
//        Scanner.Kind[] types = {IDENT, INT_LIT, INT_LIT};
//
//        Scanner scanner = new Scanner(input);
//
//        scanner.scan();
//        Scanner.Token token;
//
//        for(int i = 0; i < types.length; ++i)
//        {
//            token = scanner.nextToken();
//            assertEquals(types[i], token.kind);
//        }
//
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//
//    }
//    @Test
//    public void testLinePos() throws IllegalCharException, IllegalNumberException
//    {
//        // Tokens at (row, column) (0,0), (1,1), (2,2), (3,4), (4,8)
//        String input = "*\n\t*\n\t\t*\n\t\t\t\t*\n\t\t\t\t\t\t\t\t*\n";
//        Scanner.LinePos[] linepos = {
//            new Scanner.LinePos(0, 0),
//            new Scanner.LinePos(1, 1),
//            new Scanner.LinePos(2, 2),
//            new Scanner.LinePos(3, 4),
//            new Scanner.LinePos(4, 8)
//        };
//
//        Scanner.Kind type = TIMES;
//
//        Scanner scanner = new Scanner(input);
//
//        scanner.scan();
//        Scanner.Token token;
//
//        for(int i = 0; i < linepos.length; ++i)
//        {
//            token = scanner.nextToken();
//            assertEquals(type, token.kind);
//            // assertEquals(linepos[i], token.getLinePos());
//            assertEquals(linepos[i].line, token.getLinePos().line);
//            assertEquals(linepos[i].posInLine, token.getLinePos().posInLine);
//        }
//
//        token = scanner.nextToken();
//        assertEquals(EOF, token.kind);
//    }
//}