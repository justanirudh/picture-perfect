package cop5556sp17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;

public class ParserTest {
	// TODO: make more error tests

	public Parser initializeParser(String str) throws IllegalCharException, IllegalNumberException {
		Scanner scanner = new Scanner(str);
		scanner.scan();
		return new Parser(scanner);
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testStrongOpWeakOpRelOp() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "* /& % /   + - ||  < <= > >= == !=";
		Parser parser = initializeParser(input);
		parser.strongOp();
		parser.strongOp();
		parser.strongOp();
		parser.strongOp();
		parser.strongOp();

		parser.weakOp();
		parser.weakOp();
		parser.weakOp();
		parser.weakOp();

		parser.relOp();
		parser.relOp();
		parser.relOp();
		parser.relOp();
		parser.relOp();
		parser.relOp();
	}

	@Test
	public void testExpressionTermElem() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "abc ";
		Parser parser = initializeParser(input);
		parser.expression();

		String input2 = "34 < false + screenwidth ";
		Parser parser2 = initializeParser(input2);
		parser2.expression();

		String input3 = "  \n screenheight | ((100  + screenwidth)) ";
		Parser parser3 = initializeParser(input3);
		parser3.expression();

		String input4 = "  false & true % 34 * abc / (5 & 6) ";
		Parser parser4 = initializeParser(input4);
		parser4.expression();

		String input5 = " (5) / screenheight * false + true - abc <= 76 == def ";
		Scanner scanner5 = new Scanner(input5);
		scanner5.scan();
		Parser parser5 = new Parser(scanner5);
		parser5.expression();

		String input6 = " (5) / screenheight * false + true - abc <= 76 == ) ";
		Scanner scanner6 = new Scanner(input6);
		scanner6.scan();
		Parser parser6 = new Parser(scanner6);
		thrown.expect(Parser.SyntaxException.class);
		parser6.expression();
	}

	// @Test
	// public void testErrorProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
	// String input = "file abc";
	// Scanner scanner = new Scanner(input);
	// scanner.scan();
	// Parser parser = new Parser(scanner);
	// thrown.expect(Parser.SyntaxException.class);
	// parser.parse();
	// }

	@Test
	public void testFactor() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc 34500 false true screenwidth screenheight (3 +4)";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.factor();
		parser.factor();
		parser.factor();
		parser.factor();
		parser.factor();
		parser.factor();
		parser.factor();
	}

	@Test
	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  -> ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.arg();

		String input2 = "  |->  ";
		Scanner scanner2 = new Scanner(input2);
		scanner2.scan();
		Parser parser2 = new Parser(scanner2);
		parser2.arg();

		String input3 = "  ;  ";
		Scanner scanner3 = new Scanner(input3);
		scanner3.scan();
		Parser parser3 = new Parser(scanner3);
		parser3.arg();

		String input4 = "  (32, 34 + abc, screenwidth / screenheight) ";
		Scanner scanner4 = new Scanner(input4);
		scanner4.scan();
		Parser parser4 = new Parser(scanner4);
		parser4.arg();
		parser4.matchEOF();
	}

	@Test
	public void testArgerror() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " (3,) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		thrown.expect(Parser.SyntaxException.class);
		parser.arg();
	}

	@Test
	public void testFilterOpFrameOpImageOp() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "blur gray convolve show move hide xloc yloc width height scale";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.filterOp();
		parser.filterOp();
		parser.filterOp();
		parser.frameOp();
		parser.frameOp();
		parser.frameOp();
		parser.frameOp();
		parser.frameOp();
		parser.imageOp();
		parser.imageOp();
		parser.imageOp();
		parser.matchEOF();
	}

	@Test
	public void testChainElem() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc \n blur ->\n gray (a + b) \n convolve (6, abc) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.chainElem();
		parser.chainElem();

		String input2 = "\n gray (a + b) \n convolve (6, abc) \n move (abc / 3) \n xloc (abc, def) \n height;";
		Scanner scanner2 = new Scanner(input2);
		scanner2.scan();
		Parser parser2 = new Parser(scanner2);
		parser2.chainElem();
		parser2.chainElem();
		parser2.chainElem();
		parser2.chainElem();
		parser2.chainElem();
	}

	@Test
	public void testArrowOp() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "-> \n |->";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.arrowOp();
		parser.arrowOp();
		parser.matchEOF();
	}

	@Test
	public void testDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "integer abc boolean bool frame fr";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.dec();
		parser.dec();
		parser.dec();
		parser.matchEOF();
	}

	@Test
	public void testChain() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc -> def";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.chain();
		parser.matchEOF();

		String input2 = "abc -> def |-> gray (a + b) -> gray ;";
		Scanner scanner2 = new Scanner(input2);
		scanner2.scan();
		Parser parser2 = new Parser(scanner2);
		parser2.chain();
	}

	@Test
	public void testAssign() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc <- 4 + 5";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.assign();
		parser.matchEOF();

		String input2 = "abc <- (4 + 5)";
		Scanner scanner2 = new Scanner(input2);
		scanner2.scan();
		Parser parser2 = new Parser(scanner2);
		parser2.assign();
		parser2.matchEOF();
	}

	@Test
	public void testIfStatementWhileStatement() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "if(a == b) {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.ifStatement();
		parser.matchEOF();

		Parser parser2 = initializeParser("while( a <= 234) \r\n{integer abc}");
		parser2.whileStatement();
		
		Parser parser3 = initializeParser("if( 23 != 234) \n{sleep 23;}");
		parser3.ifStatement();
		
		Parser parser4 = initializeParser("while( abc/0) \n{ abc <- 3 ;}"); //go to assign
		parser4.whileStatement();
		
		Parser parser5 = initializeParser("if( 0 % 99) \n{ abc -> blur;}"); //go to dec
		parser5.ifStatement();
		
		Parser parser6 = initializeParser("if( ((screenwidth))) \n{ while( a > 42) {image foo}}");
		parser6.ifStatement();
		
		Parser parser7 = initializeParser("while( ((screenwidth))) \n{ if( a > 42) {  blur (3 < 2) -> move (4 < 3, 8 >= 9)  ;}}");
		parser7.whileStatement();
	
	}
	
	@Test
	public void testParamDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "integer abc boolean bool url foo file bar";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.paramDec();
		parser.paramDec();
		parser.paramDec();
		parser.paramDec();
		parser.matchEOF();
	}

	// @Test
	// public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
	// String input = "prog0 {}";
	// Parser parser = new Parser(new Scanner(input).scan());
	// parser.parse();
	// }

}
