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
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.Statement;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testSymbolTable() throws Exception {
		/*//scope = -1
		 * { //scope = 0
		 * 	int x
		 * 	x = 5 //vanilla case
		 * 	{ //scope = 1
		 * 		bool y
		 * 		x = 5
		 * 		{ //scope = 2
		 * 			img x
		 * 			x = 10
		 * 		}
		 * 		{ //scope = 3
		 * 			frame y;
		 * 			y = 10;
		 * 			int y; //insert returns false
		 * 			{ //scope 4
		 * 				y = 15 //should take scope 3's y
		 * 			}
		 * 		}
		 * 	}
		 * }
		 */
		Scanner scanner = new Scanner("");
		Dec decInt = new Dec(scanner.new Token(KW_INTEGER, 0, 0),scanner.new Token(IDENT, 0, 0) );
		Dec decBool = new Dec(scanner.new Token(KW_BOOLEAN, 0, 0),scanner.new Token(IDENT, 0, 0) );
		Dec decImg = new Dec(scanner.new Token(KW_IMAGE, 0, 0),scanner.new Token(IDENT, 0, 0) );
		Dec decFrame = new Dec(scanner.new Token(KW_FRAME, 0, 0),scanner.new Token(IDENT, 0, 0) );
		
		boolean ret;
		SymbolTable symtab = new SymbolTable();
		
		symtab.enterScope(); //scope = 0
		ret = symtab.insert("x", decInt);
		assertEquals(true, ret);
		Dec dec1 = symtab.lookup("x");
		assertEquals(KW_INTEGER, dec1.firstToken.kind);
		
		symtab.enterScope(); //scope = 1
		ret = symtab.insert("y", decBool);
		assertEquals(true, ret);
		Dec dec2 = symtab.lookup("x");
		assertEquals(KW_INTEGER, dec2.firstToken.kind);
		
		symtab.enterScope(); //scope 2
		ret = symtab.insert("x", decImg);
		assertEquals(true, ret);
		Dec dec3 = symtab.lookup("x");
		assertEquals(KW_IMAGE, dec3.firstToken.kind);
		symtab.leaveScope();
		
		symtab.enterScope(); //scope 3
		ret = symtab.insert("y", decFrame);
		assertEquals(true, ret);
		Dec dec4 = symtab.lookup("y");
		assertEquals(KW_FRAME, dec4.firstToken.kind);
		ret = symtab.insert("y", decInt);
		assertEquals(false, ret); //second dec, should be false
		
		symtab.enterScope(); //scope 4
		Dec dec5 = symtab.lookup("y");
		assertEquals(KW_FRAME, dec5.firstToken.kind);
		symtab.leaveScope();
		
		symtab.leaveScope();
		
		symtab.leaveScope();
		
		symtab.leaveScope();
		
		//System.out.println(symtab.toString());
			
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
	public void testAssignmentBoolLitError0() throws Exception {
		String input = "p {\nboolean y \ny <- 3;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		//making a visitor and passing it. Designed this way so that I can create different kinds of 
		//visitors and do different things with the AST. Awesome,right? 
		TypeCheckVisitor v = new TypeCheckVisitor();
		//TODO: Uncomment this when implemented
//		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		program.visit(v, null);
	}

}
