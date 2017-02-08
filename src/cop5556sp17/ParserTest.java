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
	public void testErrorProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "file abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		thrown.expect(Parser.SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testErrorProgram1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		thrown.expect(Parser.SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.factor();
	}

	@Test
	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,5) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.arg();
	}

	@Test
	public void testArgerror() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		thrown.expect(Parser.SyntaxException.class);
		parser.arg();
	}

	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "prog0 {}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

}
