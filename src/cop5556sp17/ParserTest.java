package cop5556sp17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;

public class ParserTest {

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
	public void testExpression() throws IllegalCharException, IllegalNumberException, SyntaxException {
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

//	@Test
//	public void testErrorProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
//		String input = "file abc";
//		Scanner scanner = new Scanner(input);
//		scanner.scan();
//		Parser parser = new Parser(scanner);
//		thrown.expect(Parser.SyntaxException.class);
//		parser.parse();
//	}
	
	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.factor();
	}
//
//	@Test
//	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
//		String input = "  (3,5) ";
//		Scanner scanner = new Scanner(input);
//		scanner.scan();
//		Parser parser = new Parser(scanner);
//		parser.arg();
//	}

//	@Test
//	public void testArgerror() throws IllegalCharException, IllegalNumberException, SyntaxException {
//		String input = "  (3,) ";
//		Scanner scanner = new Scanner(input);
//		scanner.scan();
//		Parser parser = new Parser(scanner);
//		thrown.expect(Parser.SyntaxException.class);
//		parser.arg();
//	}

//	@Test
//	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
//		String input = "prog0 {}";
//		Parser parser = new Parser(new Scanner(input).scan());
//		parser.parse();
//	}

}
