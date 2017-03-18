/**  Important to test the error cases in case the
 * AST is not being completely traversed.
 * 
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import static cop5556sp17.Scanner.Kind.*;
import static cop5556sp17.AST.Type.TypeName;
import static cop5556sp17.AST.Type.TypeName.*;

public class TypeCheckVisitorTest {

	private BinaryExpression decorateBinaryExpression(String input) throws Exception {
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		Expression exp = parser.expression();
		BinaryExpression bExp = (BinaryExpression) exp;
		TypeCheckVisitor v = new TypeCheckVisitor();
		v.symtab.insert("ident_img", new Dec(scanner.new Token(KW_IMAGE, 0, 0), scanner.new Token(IDENT,
				0, 0)));
		bExp.visit(v, null);
		return bExp;
	}

	private BinaryChain decorateBinaryChain(String input) throws Exception {
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		Chain c = parser.chain();
		BinaryChain bc = (BinaryChain) c;
		TypeCheckVisitor v = new TypeCheckVisitor();
		v.symtab.insert("ident_url", new ParamDec(scanner.new Token(KW_URL, 0, 0), scanner.new Token(
				IDENT, 0, 0)));
		v.symtab.insert("ident_file", new ParamDec(scanner.new Token(KW_FILE, 0, 0), scanner.new Token(
				IDENT, 0, 0)));
		v.symtab.insert("ident_img", new Dec(scanner.new Token(KW_IMAGE, 0, 0), scanner.new Token(IDENT,
				0, 0)));
		v.symtab.insert("ident_frame", new Dec(scanner.new Token(KW_FRAME, 0, 0), scanner.new Token(
				IDENT, 0, 0)));
		bc.visit(v, null);
		return bc;
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testSymbolTable() throws Exception {
		/*
		 * //scope = 0 { //scope = 1 int x x = 5 //vanilla case { //scope = 2 bool y x = 5 { //scope = 3
		 * img x x = 10 } { //scope = 4 frame y; y = 10; int y; //insert returns false { //scope 5 y =
		 * 15 //should take scope 3's y } } } }
		 */
		Scanner scanner = new Scanner("");
		Dec decInt = new Dec(scanner.new Token(KW_INTEGER, 0, 0), scanner.new Token(IDENT, 0, 0));
		Dec decBool = new Dec(scanner.new Token(KW_BOOLEAN, 0, 0), scanner.new Token(IDENT, 0, 0));
		Dec decImg = new Dec(scanner.new Token(KW_IMAGE, 0, 0), scanner.new Token(IDENT, 0, 0));
		Dec decFrame = new Dec(scanner.new Token(KW_FRAME, 0, 0), scanner.new Token(IDENT, 0, 0));

		boolean ret;
		SymbolTable symtab = new SymbolTable();

		symtab.enterScope(); // scope = 1
		ret = symtab.insert("x", decInt);
		assertEquals(true, ret);
		Dec dec1 = symtab.lookup("x");
		assertEquals(KW_INTEGER, dec1.firstToken.kind);

		symtab.enterScope(); // scope = 2
		ret = symtab.insert("y", decBool);
		assertEquals(true, ret);
		Dec dec2 = symtab.lookup("x");
		assertEquals(KW_INTEGER, dec2.firstToken.kind);

		symtab.enterScope(); // scope 3
		ret = symtab.insert("x", decImg);
		assertEquals(true, ret);
		Dec dec3 = symtab.lookup("x");
		assertEquals(KW_IMAGE, dec3.firstToken.kind);
		symtab.leaveScope();

		symtab.enterScope(); // scope 4
		ret = symtab.insert("y", decFrame);
		assertEquals(true, ret);
		Dec dec4 = symtab.lookup("y");
		assertEquals(KW_FRAME, dec4.firstToken.kind);
		ret = symtab.insert("y", decInt);
		assertEquals(false, ret); // second dec, should be false

		symtab.enterScope(); // scope 5
		Dec dec5 = symtab.lookup("y");
		assertEquals(KW_FRAME, dec5.firstToken.kind);
		symtab.leaveScope();

		symtab.leaveScope();

		symtab.leaveScope();

		symtab.leaveScope();

		// System.out.println(symtab.toString());

	}

	@Test
	public void testSymbolTable2() throws Exception {
		/*
		 * //scope 0 { //scope 1 int x; } {//scope 2 x = 5; //should return null }
		 *
		 */
		Scanner scanner = new Scanner("");
		Dec decInt = new Dec(scanner.new Token(KW_INTEGER, 0, 0), scanner.new Token(IDENT, 0, 0));

		boolean ret;
		SymbolTable symtab = new SymbolTable();

		symtab.enterScope();
		ret = symtab.insert("x", decInt);
		assertEquals(true, ret);
		symtab.leaveScope();

		symtab.enterScope();
		Dec dec = symtab.lookup("x"); // should return null
		assertEquals(null, dec);
		symtab.leaveScope();

		// System.out.println(symtab.toString());

	}

	@Test
	public void testAssignmentBoolLit0() throws Exception {
		String input = "p {\nboolean y \ny <- false;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testIdentExpression() throws Exception {
		String input = "foo";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		Expression exp = parser.factor();
		assertEquals(IdentExpression.class, exp.getClass());
		IdentExpression iExp = (IdentExpression) exp;
		TypeCheckVisitor v = new TypeCheckVisitor();
		// inserting a declaration of false
		v.symtab.insert("foo", new Dec(scanner.new Token(KW_INTEGER, 0, 0), scanner.new Token(IDENT, 0,
				0)));
		iExp.visit(v, null);
		assertEquals(TypeName.INTEGER, iExp.getTypeName());
		assertEquals(KW_INTEGER, iExp.getDec().firstToken.kind);
	}

	@Test
	public void testConstantExpression() throws Exception {
		String input = "screenwidth";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		Expression exp = parser.factor();
		assertEquals(ConstantExpression.class, exp.getClass());
		ConstantExpression cExp = (ConstantExpression) exp;
		TypeCheckVisitor v = new TypeCheckVisitor();
		cExp.visit(v, null);
		assertEquals(TypeName.INTEGER, cExp.getTypeName());
	}

	@Test
	public void testBinaryExpression() throws Exception {
		BinaryExpression b1 = decorateBinaryExpression("1 - 2");
		assertEquals(TypeName.INTEGER, b1.getTypeName());

		BinaryExpression b2 = decorateBinaryExpression("50 * 40");
		assertEquals(TypeName.INTEGER, b2.getTypeName());

		BinaryExpression b3 = decorateBinaryExpression("50 <= 40");
		assertEquals(TypeName.BOOLEAN, b3.getTypeName());

		BinaryExpression b4 = decorateBinaryExpression("false > \n true");
		assertEquals(TypeName.BOOLEAN, b4.getTypeName());

		BinaryExpression b5 = decorateBinaryExpression("false == true");
		assertEquals(TypeName.BOOLEAN, b5.getTypeName());

		BinaryExpression b6 = decorateBinaryExpression("ident_img + ident_img");
		assertEquals(TypeName.IMAGE, b6.getTypeName());

		BinaryExpression b7 = decorateBinaryExpression("5 * ident_img");
		assertEquals(TypeName.IMAGE, b7.getTypeName());

		BinaryExpression b8 = decorateBinaryExpression("ident_img * 4");
		assertEquals(TypeName.IMAGE, b8.getTypeName());

		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		decorateBinaryExpression("false > 5");
	}

	@Test
	public void testBinaryChain() throws Exception {
		BinaryChain b1 = decorateBinaryChain("ident_url -> ident_img");
		assertEquals(TypeName.IMAGE, b1.getTypeName());

		BinaryChain b2 = decorateBinaryChain("ident_file -> ident_img");
		assertEquals(TypeName.IMAGE, b2.getTypeName());

		BinaryChain b3 = decorateBinaryChain("ident_img -> ident_frame");
		assertEquals(TypeName.FRAME, b3.getTypeName());

		BinaryChain b4 = decorateBinaryChain("ident_img -> ident_file");
		assertEquals(TypeName.NONE, b4.getTypeName());

		BinaryChain b5 = decorateBinaryChain("ident_frame -> xloc");
		assertEquals(TypeName.INTEGER, b5.getTypeName());

		BinaryChain b6 = decorateBinaryChain("ident_frame -> hide");
		assertEquals(TypeName.FRAME, b6.getTypeName());

		BinaryChain b7 = decorateBinaryChain("ident_img -> height");
		assertEquals(TypeName.IMAGE, b7.getTypeName());

		BinaryChain b8 = decorateBinaryChain("ident_img |-> convolve");
		assertEquals(TypeName.IMAGE, b8.getTypeName());

		BinaryChain b9 = decorateBinaryChain("ident_img -> scale (5)");
		assertEquals(TypeName.IMAGE, b9.getTypeName());

		BinaryChain b10 = decorateBinaryChain("ident_img -> ident_url");
		assertEquals(TypeName.IMAGE, b10.getTypeName());
	}

	@Test
	public void testTuple() throws Exception {
		String input = "(abc, screenwidth, false > true)";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.arg();
		Tuple tup = (Tuple) ast;
		TypeCheckVisitor v = new TypeCheckVisitor();
		v.symtab.insert("abc", new Dec(scanner.new Token(KW_INTEGER, 0, 0), scanner.new Token(IDENT, 0,
				0)));
		thrown.expect(TypeCheckVisitor.TypeCheckException.class); // throws for false > true as that is
																															// boolean and tuple() expects all
																															// expressions to be integers
		tup.visit(v, null);
	}

	@Test
	public void testIdentChain() throws Exception {
		String input = "foo";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chainElem();
		ChainElem ce = (ChainElem) ast;
		TypeCheckVisitor v = new TypeCheckVisitor();
		v.symtab.insert("foo", new Dec(scanner.new Token(KW_FRAME, 0, 0), scanner.new Token(IDENT, 0,
				0)));
		ce.visit(v, null);
		assertEquals(FRAME, ce.getTypeName());
	}

	@Test
	public void testFilterChain() throws Exception {
		String input = "gray";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chainElem();
		ChainElem ce = (ChainElem) ast;
		TypeCheckVisitor v = new TypeCheckVisitor();
		ce.visit(v, null);
		assertEquals(IMAGE, ce.getTypeName());
	}

	@Test
	public void testFrameOpChain0() throws Exception {
		String input = "show";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chainElem();
		ChainElem ce = (ChainElem) ast;
		TypeCheckVisitor v = new TypeCheckVisitor();
		ce.visit(v, null);
		assertEquals(NONE, ce.getTypeName());
	}

	@Test
	public void testFrameOpChain1() throws Exception {
		String input = "xloc (abc)";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chainElem();
		ChainElem ce = (ChainElem) ast;
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		ce.visit(v, null);
	}

	@Test
	public void testFrameOpChain2() throws Exception {
		String input = "move (abc, screenwidth)";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chainElem();
		ChainElem ce = (ChainElem) ast;
		TypeCheckVisitor v = new TypeCheckVisitor();
		v.symtab.insert("abc", new Dec(scanner.new Token(KW_INTEGER, 0, 0), scanner.new Token(IDENT, 0,
				0)));
		ce.visit(v, null);
		assertEquals(NONE, ce.getTypeName());
	}

	@Test
	public void testImageOpChain0() throws Exception {
		String input = "width";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chainElem();
		ChainElem ce = (ChainElem) ast;
		TypeCheckVisitor v = new TypeCheckVisitor();
		ce.visit(v, null);
		assertEquals(INTEGER, ce.getTypeName());
	}

	@Test
	public void testImageOpChain1() throws Exception {
		String input = "scale";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chainElem();
		ChainElem ce = (ChainElem) ast;
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		ce.visit(v, null);
	}

	@Test
	public void testImageOpChain2() throws Exception {
		String input = "scale (5)";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chainElem();
		ChainElem ce = (ChainElem) ast;
		TypeCheckVisitor v = new TypeCheckVisitor();
		ce.visit(v, null);
		assertEquals(IMAGE, ce.getTypeName());
	}

	@Test
	public void testAssignmentBoolLitError0() throws Exception {
		String input = "p {\nboolean y \ny <- 3;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		// making a visitor and passing it. Designed this way so that I can create different kinds of
		// visitors and do different things with the AST. Awesome,right?
		TypeCheckVisitor v = new TypeCheckVisitor();
		// TODO: Uncomment this when implemented
		// thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		program.visit(v, null);
	}

}
