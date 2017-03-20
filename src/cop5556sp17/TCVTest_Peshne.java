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
import cop5556sp17.TypeCheckVisitor.TypeCheckException;

public class TCVTest_Peshne {


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testAssignmentBoolLit0() throws Exception{
    String input = "p {\nboolean y \ny <- false;}";
    Scanner scanner = new Scanner(input);
    scanner.scan();
    Parser parser = new Parser(scanner);
    ASTNode program = parser.parse();
    TypeCheckVisitor v = new TypeCheckVisitor();
    program.visit(v, null);
  }

  @Test
  public void testAssignmentBoolLitError0() throws Exception{
    String input = "p {\nboolean y \ny <- 3;}";
    Scanner scanner = new Scanner(input);
    scanner.scan();
    Parser parser = new Parser(scanner);
    ASTNode program = parser.parse();
    TypeCheckVisitor v = new TypeCheckVisitor();
    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    program.visit(v, null);
  }

  @Test
  public void testSameScopeMultiDec() throws Exception {
    String input =
      "Prog1 {\n" +
      "  integer i boolean i\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testSameScopeMultiDec1() throws Exception {
    String input =
      "Prog1 {\n" +
      "  integer i\n" +
      "  i <- 12;" +
      "  integer i\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testTypeMismatch() throws Exception {
    String input =
      "Prog1 {\n" +
      "  integer i\n" +
      "  i <- true;\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testUndefIdent() throws Exception {
    String input =
      "Prog1 {\n" +
      "  i <- 1;\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testUnorderedDeclaration() throws Exception {
    String input =
      "prog1 {\n" +
      "  i <- 12;\n" +
      "  integer i\n" +
      "}";

    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testNestedLookup1() throws Exception {
    String input =
      "Prog1 {\n" +
      "  integer i\n" +
      "  boolean cond\n" +
      "  if (cond) {\n" +
      "    i <- 12;\n" +
      "  }\n" +
      "}";

    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testNestedLookup2() throws Exception {
    String input =
      "Prog1 {\n" +
      "  integer i\n" +
      "  boolean cond\n" +
      "  if (cond) {\n" +
      "    i <- 12;\n" +
      "    boolean i\n" +
      "  }\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);

    input = "Prog1 {\n" +
      "  boolean cond\n" +
      "  while (cond) {\n" +
      "    integer i\n" +
      "    i <- 1;\n" +
      "    if (condIf) {\n" +
      "      i <- i + 1\n" +
      "      cond <- false;\n" +
      "    }\n" +
      "    boolean cond1\n" +
      "  }\n" +
      "}";
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testBinaryChainError1() throws Exception {
    String input =
      "prog2\n" +
      "url u, integer i{\n" +
      "  image img\n" +
      "  frame fr\n" +
      "  fr -> u -> img;\n" +
      "}";
    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testBinaryChainCorrect1() throws Exception {
    String input =
      "prog2\n" +
      "url u, integer i{\n" +
      "  image img\n" +
      "  image img2\n" +
      "  u -> img |-> blur;\n" +
      "}";
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testBinaryChainError2() throws Exception {
    String input =
      "prog2\n" +
      "file fi, integer i{\n" +
      "  image img\n" +
      "  frame fr\n" +
      "  fi -> i;\n" +
      "}";
    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testBinaryChainCorrect2() throws Exception {
    String input =
      "prog2\n" +
      "file fi, integer i{\n" +
      "  image img\n" +
      "  frame fr\n" +
      "  fi -> img;\n" +
      "}";
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testBinaryChainError3() throws Exception {
    String input =
      "prog2\n" +
      "file fi, integer i{\n" +
      "  image img\n" +
      "  frame fr\n" +
      "  img -> fi -> fr;\n" +
      "}";
    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testBinaryChainCorrect3() throws Exception {
    String input =
      "prog2\n" +
      "file fi, integer i, url u {\n" +
      "  image img\n" +
      "  frame fr\n" +
      "  u -> img -> fi;\n" +
      "}";
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testBinaryChainCorrect4() throws Exception {
    String input =
      "prog4\n" +
      "file fi, integer i, url u{\n" +
      "  image img\n" +
      "  frame fr\n" +
      "  img -> i; \n" +
      "}";
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testTupleCorrect() throws Exception {
    String input =
      "testTuple {\n" +
      "  frame fr \n" +
      "  fr -> move (12, 12);\n" +
      "}";

    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testTupleError() throws Exception {
    String input =
      "testTuple\n{" +
      "  frame fr\n" +
      "  fr -> move (12, true);\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testBinExprCorrect() throws Exception {
    String input =
      "binExp\n" +
      "integer i_global {\n" +
      "  integer op2\n" +
      "  integer result\n" +
      "  result <- i_global + op2;\n" +
      "  result <- i_global - op2;\n" +
      "  result <- i_global * op2;\n" +
      "  result <- i_global / op2;\n" +
      "\n" +
      "  image img_op\n" +
      "  image img_result\n" +
      "  img_result <- i_global * img_op;\n" +
      "  img_result <- img_op * i_global;\n" +
      "\n" +
      "  boolean bool_op2\n" +
      "  boolean bool_result\n" +
      "  bool_result <- i_global < op2;\n" +
      "  bool_result <- i_global > op2;\n" +
      "  bool_result <- i_global <= op2;\n" +
      "  bool_result <- i_global >= op2;\n" +
      "  bool_result <- bool_result < bool_op2;\n" +
      "  bool_result <- bool_result > bool_op2;\n" +
      "  bool_result <- bool_result <= bool_op2;\n" +
      "  bool_result <- bool_result >= bool_op2;\n" +
      "\n" +
      "  frame fr1\n" +
      "  frame fr2\n" +
      "  bool_result <- fr1 == fr2;\n" +
      "}";
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testBinExprError() throws Exception {
    String input =
      "binExp\n" +
      "integer i_global {\n" +
      "  integer op2\n" +
      "  boolean result\n" +
      "  result <- i_global + op2;\n" +
      "  result <- i_global - op2;\n" +
      "  result <- i_global * op2;\n" +
      "  result <- i_global / op2;\n" +
      "\n" +
      "  image img_op\n" +
      "  integer img_result\n" +
      "  img_result <- i_global * img_op;\n" +
      "  img_result <- img_op * i_global;\n" +
      "\n" +
      "  boolean bool_op2\n" +
      "  boolean bool_result\n" +
      "  bool_result <- i_global < op2;\n" +
      "  bool_result <- i_global > op2;\n" +
      "  bool_result <- i_global <= op2;\n" +
      "  bool_result <- i_global >= op2;\n" +
      "  bool_result <- bool_result < bool_op2;\n" +
      "  bool_result <- bool_result > bool_op2;\n" +
      "  bool_result <- bool_result <= bool_op2;\n" +
      "  bool_result <- bool_result >= bool_op2;\n" +
      "\n" +
      "  frame fr1\n" +
      "  frame fr2\n" +
      "  bool_result <- fr1 == fr2;\n" +
      "}";
    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testWhileCorrect() throws Exception {
    String input =
      "testIf {\n" +
      "  frame fr\n" +
      "  boolean cond \n" +
      "  integer i\n" +
      "  cond <- 23 > i;" +
      "  while (cond) {\n" +
      "    fr -> move (12, 12);\n" +
      "  }\n" +
      "}";

    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testWhileError() throws Exception {
    String input =
      "testIf {\n" +
      "  frame fr\n" +
      "  integer cond \n" +
      "  integer i\n" +
      "  cond <- 23 + i;" +
      "  while (cond) {\n" +
      "    fr -> move (12, 12);\n" +
      "  }\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testIfCorrect() throws Exception {
    String input =
      "testIf {\n" +
      "  frame fr\n" +
      "  boolean cond \n" +
      "  integer i\n" +
      "  cond <- 23 > i;" +
      "  if (cond) {\n" +
      "    fr -> move (12, 12);\n" +
      "  }\n" +
      "}";

    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testIfError() throws Exception {
    String input =
      "testIf {\n" +
      "  frame fr\n" +
      "  integer cond \n" +
      "  integer i\n" +
      "  cond <- 23 + i;" +
      "  if (cond) {\n" +
      "    fr -> move (12, 12);\n" +
      "  }\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testImageOpError1() throws Exception {
    String input =
      "testImgOp {\n" +
      "  frame fr\n" +
      "  integer i\n" +
      "  image img" +
      "  img -> width(i);\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testFrameOpError1() throws Exception {
    String input =
      "testImgOp {\n" +
      "  frame fr\n" +
      "  integer i\n" +
      "  image img" +
      "  fr -> hide(i);\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testFrameOpError2() throws Exception {
    String input =
      "testImgOp {\n" +
      "  frame fr\n" +
      "  integer i\n" +
      "  image img" +
      "  fr -> xloc(i);\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testFrameOpError3() throws Exception {
    String input =
      "testImgOp {\n" +
      "  frame fr\n" +
      "  integer i\n" +
      "  image img" +
      "  fr -> move(i);\n" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testSleepStatementError() throws Exception {
    String input =
      "testImgOp {\n" +
      "  frame fr\n" +
      "  integer i\n" +
      "  image img" +
      "  sleep img;" +
      "}";

    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    TCVTest_Peshne.test(input, null);
  }

  @Test
  public void testSleepStatementCorrect() throws Exception {
    String input =
      "testImgOp {\n" +
      "  frame fr\n" +
      "  integer i\n" +
      "  image img" +
      "  sleep i;" +
      "}";

    TCVTest_Peshne.test(input, null);
  }
  private static void test(String input, Object arg) throws Exception {
    Scanner scanner = new Scanner(input);
    scanner.scan();
    Parser parser = new Parser(scanner);
    ASTNode program = parser.parse();
    TypeCheckVisitor v = new TypeCheckVisitor();
    program.visit(v, arg);
  }



}
