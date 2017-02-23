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
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
//import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.SleepStatement;
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

		Parser parser2 = initParser(
				"height (a + b) -> hide |-> convolve (foo, screenwidth/false) -> bar");

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

	@Test
	public void testAssignmentStmt() throws IllegalCharException, IllegalNumberException,
			SyntaxException {

		String input = "foo <- (1 + 4)";
		Parser parser = initParser(input);
		ASTNode ast = parser.assign();
		assertEquals(AssignmentStatement.class, ast.getClass());

		AssignmentStatement ag = (AssignmentStatement) ast;
		assertEquals(IdentLValue.class, ag.getVar().getClass());
		assertEquals(BinaryExpression.class, ag.getE().getClass());

		Parser parser2 = initParser("bar <- true");
		ASTNode ast2 = parser2.assign();
		assertEquals(AssignmentStatement.class, ast2.getClass());

		AssignmentStatement ag2 = (AssignmentStatement) ast2;
		assertEquals(IdentLValue.class, ag2.getVar().getClass());
		assertEquals(BooleanLitExpression.class, ag2.getE().getClass());
	}

	@Test
	public void testStatement() throws IllegalCharException, IllegalNumberException, SyntaxException {

		Parser parser = initParser("sleep foo;");
		ASTNode ast = parser.statement();
		assertEquals(SleepStatement.class, ast.getClass());

		SleepStatement ss = (SleepStatement) ast;
		assertEquals(IdentExpression.class, ss.getE().getClass());

		Parser parser2 = initParser("while(foo){}");
		ASTNode ast2 = parser2.statement();
		assertEquals(WhileStatement.class, ast2.getClass());

		Parser parser3 = initParser("if(foo){}");
		ASTNode ast3 = parser3.statement();
		assertEquals(IfStatement.class, ast3.getClass());

		Parser parser4 = initParser("blur ((a+b)) -> width;");
		ASTNode ast4 = parser4.statement();
		assertEquals(BinaryChain.class, ast4.getClass());

		Parser parser5 = initParser("foo <- false;");
		ASTNode ast5 = parser5.statement();
		assertEquals(AssignmentStatement.class, ast5.getClass());

		Parser parser6 = initParser("foo -> blur;");
		ASTNode ast6 = parser6.statement();
		assertEquals(BinaryChain.class, ast6.getClass());
	}

	@Test
	public void testDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "frame foo";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.dec();
		assertEquals(Dec.class, ast.getClass());
	}
	
	@Test
	public void testParamDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "boolean foo";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.paramDec();
		assertEquals(ParamDec.class, ast.getClass());
	}

	@Test
	public void testBlock() throws IllegalCharException, IllegalNumberException, SyntaxException {

		Parser parser = initParser("{image foo}");
		ASTNode ast = parser.block();
		assertEquals(Block.class, ast.getClass());
		Block b = (Block) ast;
		assertEquals(Dec.class, b.getDecs().get(0).getClass());

		Parser parser2 = initParser("{while(foo){}}");
		ASTNode ast2 = parser2.block();
		assertEquals(Block.class, ast2.getClass());
		Block b2 = (Block) ast2;
		assertEquals(WhileStatement.class, b2.getStatements().get(0).getClass());

		Parser parser3 = initParser(
				"{ boolean baz \n foo -> baz; \n frame google_dot_com \n barbar <- 1+2;  }");
		ASTNode ast3 = parser3.block();
		assertEquals(Block.class, ast3.getClass());
		Block b3 = (Block) ast3;
		assertEquals(Dec.class, b3.getDecs().get(0).getClass());
		assertEquals(Dec.class, b3.getDecs().get(1).getClass());
		assertEquals(BinaryChain.class, b3.getStatements().get(0).getClass());
		assertEquals(AssignmentStatement.class, b3.getStatements().get(1).getClass());
	}

}
