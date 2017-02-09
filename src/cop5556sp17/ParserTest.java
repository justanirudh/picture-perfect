package cop5556sp17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;

public class ParserTest {
	//TODO: make more error tests
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testStrongOpWeakOpRelOp() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "* /& % /   + - ||  < <= > >= == !=";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
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
	public void testExpressionTermElem() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.expression();

		String input2 = "34 < false + screenwidth ";
		Scanner scanner2 = new Scanner(input2);
		scanner2.scan();
		Parser parser2 = new Parser(scanner2);
		parser2.expression();

		String input3 = "  \n screenheight | ((100  + screenwidth)) ";
		Scanner scanner3 = new Scanner(input3);
		scanner3.scan();
		Parser parser3 = new Parser(scanner3);
		parser3.expression();

		String input4 = "  false & true % 34 * abc / (5 & 6) ";
		Scanner scanner4 = new Scanner(input4);
		scanner4.scan();
		Parser parser4 = new Parser(scanner4);
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
	public void testFilterOpFrameOpImageOp() throws IllegalCharException, IllegalNumberException, SyntaxException {
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
	
	public void testArrowOp() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "-> \n |->";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.arrowOp();
		parser.arrowOp();
		parser.matchEOF();
	}
	
	
	// @Test
	// public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
	// String input = "prog0 {}";
	// Parser parser = new Parser(new Scanner(input).scan());
	// parser.parse();
	// }

}
