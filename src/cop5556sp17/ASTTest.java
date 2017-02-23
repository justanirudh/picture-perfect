package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
//import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;

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

	@Test
	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "(abc, 2 + true, screenwidth/false)";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.arg();
		assertEquals(Tuple.class, ast.getClass());
		Tuple tup = (Tuple) ast;
		List<Expression> exps = tup.getExprList();
		assertEquals(IdentExpression.class, exps.get(0).getClass());
		assertEquals(BinaryExpression.class, exps.get(1).getClass());
		assertEquals(BinaryExpression.class, exps.get(2).getClass());
		BinaryExpression be = (BinaryExpression) exps.get(2);
		assertEquals(ConstantExpression.class, be.getE0().getClass());
		assertEquals(BooleanLitExpression.class, be.getE1().getClass());
		assertEquals(DIV, be.getOp().kind);
	}

	@Test
	public void testIfStmtWhileStmt() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		Parser parser = initParser("if (a/0) {integer foo}");

		ASTNode ast = parser.ifStatement();
		assertEquals(IfStatement.class, ast.getClass());

		IfStatement ifStmt = (IfStatement) ast;
		assertEquals(BinaryExpression.class, ifStmt.getE().getClass());
		// TODO:Uncomment this after implementing block
		// assertEquals(Block.class, ifStmt.getB().getClass());
		assertEquals(Token.class, ifStmt.getFirstToken().getClass());

		Parser parser2 = initParser("while (false) {foo <- bar;}");

		ASTNode ast2 = parser2.whileStatement();
		assertEquals(WhileStatement.class, ast2.getClass());

		WhileStatement whileStmt = (WhileStatement) ast2;
		assertEquals(BooleanLitExpression.class, whileStmt.getE().getClass());
		// TODO:Uncomment this after implementing block
		// assertEquals(Block.class, ifStmt.getB().getClass());
		assertEquals(Token.class, whileStmt.getFirstToken().getClass());
	}

	@Test
	public void testChainError() throws IllegalCharException, IllegalNumberException,
			SyntaxException {
		Parser parser0 = initParser("foo");
		thrown.expect(Parser.SyntaxException.class);
		parser0.chain();
	}

	@Test
	public void testChain() throws IllegalCharException, IllegalNumberException, SyntaxException {

		Parser parser = initParser("blur->foo");// also testing null case of Tuple

		ASTNode ast = parser.chain();
		assertEquals(BinaryChain.class, ast.getClass());

		BinaryChain chain = (BinaryChain) ast;
		assertEquals(FilterOpChain.class, chain.getE0().getClass());
		assertEquals(IdentChain.class, chain.getE1().getClass());
		assertEquals(ARROW, chain.getArrow().kind);

		Parser parser2 = initParser("height (a + b) -> hide |-> convolve (foo, screenwidth/false) -> bar");

		ASTNode ast2 = parser2.chain();
		
		assertEquals(BinaryChain.class, ast2.getClass());
		
		BinaryChain bc0 = (BinaryChain) ast2;
		assertEquals(BinaryChain.class, bc0.getE0().getClass());
		assertEquals(IdentChain.class, bc0.getE1().getClass());
		assertEquals(ARROW, bc0.getArrow().kind);
		
		BinaryChain bc1 = (BinaryChain) bc0.getE0();
		assertEquals(BinaryChain.class, bc1.getE0().getClass());
		assertEquals(FilterOpChain.class, bc1.getE1().getClass());
		assertEquals(BARARROW, bc1.getArrow().kind);
		
		BinaryChain bc2 = (BinaryChain) bc1.getE0();
		assertEquals(ImageOpChain.class, bc2.getE0().getClass());
		assertEquals(FrameOpChain.class, bc2.getE1().getClass());
		assertEquals(ARROW, bc2.getArrow().kind);
		
	}

}