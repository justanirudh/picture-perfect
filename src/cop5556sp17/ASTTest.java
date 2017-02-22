package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IntLitExpression;

public class ASTTest {

	public Parser initParser(String str) throws IllegalCharException, IllegalNumberException {
		Scanner scanner = new Scanner(str);
		scanner.scan();
		return new Parser(scanner);
	}

	static final boolean doPrint = true;
	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testIdentExpression() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testIntLitExpression() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.term();
		assertEquals(IntLitExpression.class, ast.getClass());
	}

	@Test
	public void testBooleanLitExpression() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "false";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.elem();
		assertEquals(BooleanLitExpression.class, ast.getClass());
	}

	@Test
	public void testConstantExpression() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "screenwidth";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.factor();
		assertEquals(ConstantExpression.class, ast.getClass());
	}

	@Test
	public void testBinaryExpression() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}

	@Test
	public void testBinaryExpression2() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		String input = "1 + abc * screenheight > 42";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(BinaryExpression.class, be.getE0().getClass());
		assertEquals(IntLitExpression.class, be.getE1().getClass());
		assertEquals(GT, be.getOp().kind);

		// reverse here due to precedence. * (deeper) then + then >
		BinaryExpression be2 = (BinaryExpression) be.getE0();
		assertEquals(IntLitExpression.class, be2.getE0().getClass());
		assertEquals(BinaryExpression.class, be2.getE1().getClass());
		assertEquals(PLUS, be2.getOp().kind);

		BinaryExpression be3 = (BinaryExpression) be2.getE1();
		assertEquals(IdentExpression.class, be3.getE0().getClass());
		assertEquals(ConstantExpression.class, be3.getE1().getClass());
		assertEquals(TIMES, be3.getOp().kind);
	}

}
