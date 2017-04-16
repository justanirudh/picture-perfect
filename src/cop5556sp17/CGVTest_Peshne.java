package cop5556sp17;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Program;
import cop5556sp17.PLPRuntimeLog;

public class CGVTest_Peshne {
  static final boolean doPrint = false;
  static void show(Object s) {
    if (doPrint) {
      System.out.println(s);
    }
  }

  boolean devel = false;
  boolean grade = true;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private String genProg(String paramDec, String block) {
    //http://stackoverflow.com/a/21873525/1291435
    String progName = Thread.currentThread().getStackTrace()[2].getMethodName();
    return (progName + " " + paramDec + block);
  }

  private String genBlock(String... statements) {
    StringBuilder result = new StringBuilder("{");
    for (String statement : statements) {
      result.append(statement + "\n");
    }
    result.append("}");
    return result.toString();
  }

  private void test(String input, String[] args) throws Exception {
    test(input, args, null);
  }

  private void test(String input, String[] args, String expOut) throws Exception {
    //scan, parse, and type check the program
    PLPRuntimeLog.initLog();
    Scanner scanner = new Scanner(input);
    scanner.scan();
    Parser parser = new Parser(scanner);
    ASTNode program = parser.parse();
    TypeCheckVisitor v = new TypeCheckVisitor();
    program.visit(v, null);
    show(program);

    //generate code
    CodeGenVisitor cv = new CodeGenVisitor(devel,grade,null);
    byte[] bytecode = (byte[]) program.visit(cv, null);

    //output the generated bytecode
    if (doPrint) {
      CodeGenUtils.dumpBytecode(bytecode);
    }

    //write byte code to file
    String name = ((Program) program).getName();
    String classFileName = "bin/" + name + ".class";
    OutputStream output = new FileOutputStream(classFileName);
    output.write(bytecode);
    output.close();
    System.out.println("wrote classfile to " + classFileName);

    // directly execute bytecode
    Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
    instance.run();
    if (expOut != null) {
      String log = PLPRuntimeLog.getString();
      assertEquals(expOut, log);
    } else {
      System.out.println("xyzzy");
      System.out.println(PLPRuntimeLog.getString());
    }
  }

  @Test
  public void emptyProg() throws Exception {
    String input = genProg("", "{}");
    String[] args = new String[0];
    test(input, args);
  }

  @Test
  public void paramDecs() throws Exception {
    String input = genProg("integer x, integer y, file f, url u, boolean b",
                           genBlock(""));
    String[] args = {"1", "2", "file", "http://abc.com", "true"};
      test(input, args);
  }

  @Test
  public void paramAdd() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("x <- x + y;"));
    String[] args = {"1", "2"};
    test(input, args, "3");
  }

  @Test
  public void paramSub() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("x <- x - y;"));
    String[] args = {"1", "2"};
    test(input, args, "-1");
  }

  @Test
  public void paramMod() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("integer result",
                                    "result <- x % y;"));
    String[] args = {"15", "7"};
    test(input, args, "1");
  }

  @Test
  public void paramAnd() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("integer result",
                                    "result <- x & y;"));
    String[] args = {"2", "3"};
    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    test(input, args, "2");
  }

  @Test
  public void paramOr() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("integer result",
                                    "result <- x | y;"));
    String[] args = {"2", "3"};
    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    test(input, args, "3");
  }

  @Test
  public void paramDiv() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("x <- x / y;"));
    String[] args = {"12", "2"};
    test(input, args, "6");
  }

  @Test
  public void paramMul() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("x <- x * y;"));
    String[] args = {"112", "2"};
    test(input, args, "224");
  }

  @Test
  public void decIntLitSum() throws Exception {
    String input = genProg("integer x",
                           genBlock("integer x integer y",
                                    "x <- 12;",
                                    "x <- x + 1;",
                                    "x <- x * 2;",
                                    "y <- x - 654;",
                                    "x <- y / 4;"));
    String[] args = {"2"};
    test(input, args, "121326-628-157"); //12 13 26 -628 -157
  }

  @Test
  public void basicAssign() throws Exception {
    String input = genProg("integer x",
                           genBlock("integer y",
                                    "y <- 12;",
                                    "x <- y / 4;"));
    String[] args = {"2"};
    test(input, args, "123"); // 12(12 / 4)
  }

  @Test
  public void testBasicBranching() throws Exception {
    String input = genProg("boolean double, integer x",
                           genBlock("integer result",
                                    "if (double) ",
                                    genBlock("result <- x * 2;"),
                                    "result <- x;"));
    String[] args = {"true", "23"};
    String expOut = "4623";
    test(input, args,expOut);

    String[] args2 = {"false", "23"};
    String expOut2 = "23";
    test(input, args2, expOut2);
  }

  @Test
  public void testBranching1() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("integer result",
                                    "if (x > y) ",
                                    genBlock("result <- x * 2;"),
                                    "result <- x;"));
    String[] args = {"12", "23"};
    String expOut = "12";
    test(input, args,expOut);

    String[] args2 = {"24", "23"};
    String expOut2 = "4824";
    test(input, args2, expOut2);
  }
  @Test
  public void testBranching2() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("integer result",
                                    "if (x >= y) ",
                                    genBlock("result <- x * 2;"),
                                    "result <- x;"));
    String[] args = {"-1", "23"};
    String expOut = "-1";
    test(input, args,expOut);

    String[] args2 = {"23", "23"};
    String expOut2 = "4623";
    test(input, args2, expOut2);
  }

  @Test
  public void testBranching3() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("integer result",
                                    "if (x < y) ",
                                    genBlock("result <- x * 2;"),
                                    "result <- x;"));
    String[] args = {"45", "23"};
    String expOut = "45";
    test(input, args,expOut);

    String[] args2 = {"21", "23"};
    String expOut2 = "4221";
    test(input, args2, expOut2);
  }
  @Test
  public void testBranching4() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("integer result",
                                    "if (x <= y) ",
                                    genBlock("result <- x * 2;"),
                                    "result <- x;"));
    String[] args = {"100", "23"};
    String expOut = "100";
    test(input, args,expOut);

    String[] args2 = {"23", "23"};
    String expOut2 = "4623";
    test(input, args2, expOut2);
  }
  @Test
  public void testBranching5() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("integer result",
                                    "if (x == y) ",
                                    genBlock("result <- x * 2;"),
                                    "result <- x;"));
    String[] args = {"100", "23"};
    String expOut = "100";
    test(input, args,expOut);

    String[] args2 = {"23", "23"};
    String expOut2 = "4623";
    test(input, args2, expOut2);
  }
  @Test
  public void testBranching6() throws Exception {
    String input = genProg("integer x, integer y",
                           genBlock("integer result",
                                    "if (x != y) ",
                                    genBlock("result <- x * 2;"),
                                    "result <- x;"));
    String[] args = {"23", "23"};
    String expOut = "23";
    test(input, args,expOut);

    String[] args2 = {"100", "23"};
    String expOut2 = "200100";
    test(input, args2, expOut2);
  }
  @Test
  public void testBoolBranching1() throws Exception {
    String input = genProg("boolean x, boolean y",
                           genBlock("integer result",
                                    "if (x < y) ",
                                    genBlock("result <- 2;"),
                                    "result <- 3;"));
    String[] args = {"true", "true"};
    String expOut = "3";
    test(input, args,expOut);

    String[] args2 = {"false", "true"};
    String expOut2 = "23";
    test(input, args2, expOut2);
  }
  @Test
  public void testBoolBranching2() throws Exception {
    String input = genProg("boolean x, boolean y",
                           genBlock("integer result",
                                    "if (x > y) ",
                                    genBlock("result <- 2;"),
                                    "result <- 3;"));
    String[] args = {"true", "true"};
    String expOut = "3";
    test(input, args,expOut);

    String[] args2 = {"true", "false"};
    String expOut2 = "23";
    test(input, args2, expOut2);
  }
  @Test
  public void testBoolBranching3() throws Exception {
    String input = genProg("boolean x, boolean y",
                           genBlock("integer result",
                                    "if (x <= y) ",
                                    genBlock("result <- 2;"),
                                    "result <- 3;"));
    String[] args = {"true", "true"};
    String expOut = "23";
    test(input, args,expOut);

    String[] args2 = {"true", "false"};
    String expOut2 = "3";
    test(input, args2, expOut2);
  }
  @Test
  public void testBoolBranching4() throws Exception {
    String input = genProg("boolean x, boolean y",
                           genBlock("integer result",
                                    "if (x >= y) ",
                                    genBlock("result <- 2;"),
                                    "result <- 3;"));
    String[] args = {"true", "true"};
    String expOut = "23";
    test(input, args,expOut);

    String[] args2 = {"true", "false"};
    String expOut2 = "23";
    test(input, args2, expOut2);
  }
  @Test
  public void testBoolBranching5() throws Exception {
    String input = genProg("boolean x, boolean y",
                           genBlock("integer result",
                                    "if (x == y) ",
                                    genBlock("result <- 2;"),
                                    "result <- 3;"));
    String[] args = {"true", "true"};
    String expOut = "23";
    test(input, args,expOut);

    String[] args2 = {"true", "false"};
    String expOut2 = "3";
    test(input, args2, expOut2);

    String[] args3 = {"false", "false"};
    String expOut3 = "23";
    test(input, args3, expOut3);
  }
  @Test
  public void testBoolBranching6() throws Exception {
    String input = genProg("boolean x, boolean y",
                           genBlock("integer result",
                                    "if (x != y) ",
                                    genBlock("result <- 2;"),
                                    "result <- 3;"));
    String[] args = {"true", "true"};
    String expOut = "3";
    test(input, args,expOut);

    String[] args2 = {"true", "false"};
    String expOut2 = "23";
    test(input, args2, expOut2);

    String[] args3 = {"false", "false"};
    String expOut3 = "3";
    test(input, args3, expOut3);
  }
  @Test
  public void testBasicLooping() throws Exception {
    String input = genProg("integer counter, integer x",
                           genBlock("integer step",
                                    "step <- x;",
                                    "while (0 < counter) ",
                                    genBlock("counter <- counter - step;")));
    String[] args = {"10", "2"};
    String expOut = "286420";
    test(input, args,expOut);
  }
  @Test
  public void testStructuredLoop() throws Exception {
    String input = genProg("",
                           genBlock("integer counter",
                                    "boolean cond",
                                    "counter <- 0;",
                                    "cond <- true;",
                                    "while (cond) ",
                                    genBlock("integer maxCount",
                                             "maxCount <- 9;",
                                             "counter <- counter + 1;",
                                             "if (counter >= maxCount)",
                                             genBlock("cond <- false;"))));
    String[] args = {};
    StringBuilder expOut = new StringBuilder();
    expOut.append("0true");
    for (int i = 0; i < 9; i++) {
      expOut.append("9" + (i + 1));
    }
    expOut.append("false");
    test(input, args, expOut.toString());
  }

  @Test
  public void testSlotNum() throws Exception {
    String input = genProg("integer x",
                           genBlock("if (x > 12)",
                                    genBlock("x <- 21;"),
                                    "if (x > 6)",
                                    genBlock("x <- 18;")));
    String[] args = {"13"};
    String expOut = "2118";
    test(input, args,expOut);

    String[] args2 = {"7"};
    String expOut2 = "18";
    test(input, args2, expOut2);
  }

  @Test
  public void testFancyBinExp() throws Exception {
    String input = genProg("integer x",
                           genBlock("if (x > 12 >= true)",
                                    genBlock("x <- 21;"),
                                    "if (x > 6 <= false)",
                                    genBlock("x <- 18;")));
    String[] args = {"13"};
    String expOut = "21";
    test(input, args,expOut);

    String[] args2 = {"3"};
    String expOut2 = "18";
    test(input, args2, expOut2);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testSimpleUrlToImage() throws Exception {
    String input = genProg("integer i, url u",
                           genBlock("image i",
                                    "u -> i;"));
    String[] args = {"1", imgUrl()};
    String expOut = urlRead(args[1]);
    test(input, args, expOut);
  }

  private String urlRead(String url) {
    return "readFromURL(" + url + ")";
  }
  private String frameStr() {
    return "createOrSetFrame";
  }
  private String showImg() {
    return "showImage";
  }

  private String fileWrite(String fileName) {
    return "write(" + fileName + ")";
  }

  private String fileRead(String fileName) {
    return "readFromFile(" + fileName + ")";
  }

  private String imgUrl() {
    return "https://avatars3.githubusercontent.com/u/2340240?v=3&s=460";
    // replacing the above url with your photo will incur an ancient curse:
    // you will be forced to work with Cobol, Fortran or J2EE for rest of your life
  }

  private String filePath() {
    return "/tmp/hotMan.png";
    // on windows change the path, keep name intact,
    // sorry, on windows change the OS to *NIX
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testSimpleUrlToImageToFrame() throws Exception {
    String input = genProg("integer i, url u",
                           genBlock("image i",
                                    "frame f",
                                    "u -> i -> f -> show;"));
    String[] args = {"1", imgUrl()};
    String expOut = urlRead(args[1]) + frameStr() + showImg();
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testBasicSleep() throws Exception {
    String input = genProg("integer i",
                           genBlock("sleep i;"));
    String[] args = {"1000"};
    String expOut = "";
    test(input, args, expOut);
  }

  @Test
  public void testBasicUrlFileWrite() throws Exception {
    String input = genProg("file out, url u",
                           genBlock("image i",
                                    "u -> i -> out;"));
    String[] args = {filePath(), imgUrl()};
    String expOut = urlRead(args[1]) + fileWrite(args[0]);
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testBasicFileImgRead() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "in -> i -> f -> show;"));
    String[] args = {filePath()};
    String expOut = fileRead(args[0]) + frameStr() + showImg();
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyFrameHideShow() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "integer sleepCount",
                                    "sleepCount <- 1000;",
                                    "in -> i -> f -> show;",
                                    "sleep sleepCount;",
                                    "f -> hide;",
                                    "sleep 2 * sleepCount;",
                                    "f -> show;"));
    String[] args = {filePath()};
    String expOut = ("1000" + fileRead(args[0]) + frameStr() + showImg() +
                     "hideImage" + showImg());
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyFrameMove() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "integer sleepCount",
                                    "sleepCount <- 1000;",
                                    "in -> i -> f -> show;",
                                    "sleep sleepCount;",
                                    "f -> hide;",
                                    "sleep 2 * sleepCount;",
                                    "f -> move(0, 0) -> show;"));
    String[] args = {filePath()};
    String expOut = ("1000" + fileRead(args[0]) + frameStr() + showImg() +
                     "hideImage" + "moveFrame" + showImg());
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyImageGray() throws Exception {
    String input = genProg("url uin",
                           genBlock("image i",
                                    "frame f",
                                    "integer sleepCount",
                                    "sleepCount <- 4000;",
                                    "uin -> i -> gray -> f -> show;",
                                    "sleep sleepCount;",
                                    "f -> hide;",
                                    "sleep sleepCount / 2;",
                                    "f -> show;"));
    String[] args = {imgUrl()};
    String expOut = ("4000" + urlRead(args[0]) + "grayOp" + frameStr() + showImg() +
                     "hideImage" + showImg());
    test(input, args, expOut);
  }

  @Test
  public void testFancyImageGrayBARARROW() throws Exception {
    String input = genProg("url uin",
                           genBlock("image i",
                                    "image orig",
                                    "frame f",
                                    "boolean result",
                                    "integer sleepCount",
                                    "sleepCount <- 4000;",
                                    "uin -> i;",
                                    "orig <- i;",
                                    "i |-> gray;",
                                    "result <- (i == orig);",
                                    "i -> f -> show;",
                                    "sleep sleepCount;"));
    String[] args = {imgUrl()};
    String expOut = ("4000" + urlRead(args[0]) + "copyImage" + "grayOp" + "false" +
                     frameStr() + showImg());
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyImageBlur() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "integer sleepCount",
                                    "sleepCount <- 4000;",
                                    "in -> i -> blur -> f -> show;",
                                    "sleep sleepCount;",
                                    "f -> hide;",
                                    "sleep sleepCount / 2;",
                                    "f -> show;"));
    String[] args = {filePath()};
    String expOut = ("4000" + fileRead(args[0]) + "blurOp" + frameStr() + showImg() +
                     "hideImage" + showImg());
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyImageConvolve() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "integer sleepCount",
                                    "sleepCount <- 4000;",
                                    "in -> i -> convolve -> f -> show;",
                                    "sleep sleepCount;",
                                    "f -> hide;",
                                    "sleep sleepCount / 2;",
                                    "f -> show;"));
    String[] args = {filePath()};
    String expOut = ("4000" + fileRead(args[0]) + "convolve" + frameStr() + showImg() +
                     "hideImage" + showImg());
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyImageScale() throws Exception {
    String input = genProg("url uin",
                           genBlock("image i",
                                    "frame f",
                                    "integer sleepCount",
                                    "sleepCount <- 4000;",
                                    "uin -> i -> scale(2) -> f -> show;",
                                    "sleep sleepCount;",
                                    "f -> hide;",
                                    "sleep sleepCount / 2;",
                                    "f -> show;"));
    String[] args = {imgUrl()};
    String expOut = ("4000" + urlRead(args[0]) + "scale" + frameStr() + showImg() +
                     "hideImage" + showImg());
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyFrameLoc() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "integer sleepCount",
                                    "integer x_location",
                                    "integer y_location",
                                    "integer result",
                                    "sleepCount <- 2000;",
                                    "in -> i -> f -> show;",
                                    "sleep sleepCount;",
                                    "f -> move(0, 0) -> xloc -> x_location;",
                                    "f -> yloc -> y_location;",
                                    "result <- x_location;",
                                    "result <- y_location;"));
    String[] args = {filePath()};
    String expOut = ("2000" + fileRead(args[0]) + frameStr() + showImg() +
                     "moveFrame" + "getX" + "getY" + "0" + "0");
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyWidthHeight() throws Exception {
    String input = genProg("url uin",
                           genBlock("image i",
                                    "frame f",
                                    "integer w",
                                    "integer ht",
                                    "integer result",
                                    "uin -> i;",
                                    "i -> height -> ht;",
                                    "i -> width -> w;",
                                    "result <- ht;",
                                    "result <- w;"));
    String[] args = {"http://anuragpeshne.github.io/assets/emacsSpeed/initTime.png"};
    String expOut = (urlRead(args[0]) + "64" + "189");
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyImgMul() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "image result",
                                    "in -> i;",
                                    "result <- 2 * i;",
                                    "result -> f -> show;"));
    String[] args = {filePath()};
    String expOut = (fileRead(args[0]) + "mul" + "copyImage" + frameStr() + showImg());
    test(input, args, expOut);
  }

  @Test
  public void testFancyImgDivIncorrect() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "image result",
                                    "in -> i;",
                                    "result <- 2 / i;",
                                    "result -> f -> show;"));
    String[] args = {filePath()};
    String expOut = ("doesn't matter");
    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    test(input, args, expOut);
  }

  @Test
  public void testFancyImgDiv() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "image result",
                                    "in -> i;",
                                    "result <- i / 2;",
                                    "result -> f -> show;"));
    String[] args = {filePath()};
    String expOut = (fileRead(args[0]) + "div" + "copyImage" + frameStr() + showImg());
    test(input, args, expOut);
  }

  @Test
  public void testFancyImgModIncorrect() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "image result",
                                    "in -> i;",
                                    "result <- 2 % i;",
                                    "result -> f -> show;"));
    String[] args = {filePath()};
    String expOut = ("doesn't matter");
    thrown.expect(TypeCheckVisitor.TypeCheckException.class);
    test(input, args, expOut);
  }

  @Test
  public void testFancyImgMod() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "frame f",
                                    "image result",
                                    "in -> i;",
                                    "result <- i % 2;",
                                    "result -> f -> show;"));
    String[] args = {filePath()};
    String expOut = (fileRead(args[0]) + "mod" + "copyImage" + frameStr() + showImg());
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyImgPlus() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "image out",
                                    "frame f",
                                    "image result",
                                    "integer w",
                                    "integer ht",
                                    "in -> i -> gray -> out;",
                                    "result <- i + out;",
                                    "i -> f -> show;",
                                    "sleep 1000;",
                                    "f -> hide;",
                                    "result -> f -> show;"));
    String[] args = {filePath()};
    String expOut = (fileRead(args[0]) + "grayOp" + "add" + "copyImage" + frameStr()
                     + showImg() + "hideImage" + frameStr() + showImg() + showImg());
    test(input, args, expOut);
  }

  //@Ignore("this slows down the testing")
  @Test
  public void testFancyImgMinus() throws Exception {
    String input = genProg("file in",
                           genBlock("image i",
                                    "image out",
                                    "frame f",
                                    "image result",
                                    "integer w",
                                    "integer ht",
                                    "in -> i -> gray -> out;",
                                    "result <- i - out;",
                                    "i -> f -> show;",
                                    "sleep 1000;",
                                    "f -> hide;",
                                    "result -> f -> show;"));
    String[] args = {filePath()};
    String expOut = (fileRead(args[0]) + "grayOp" + "sub" + "copyImage" + frameStr()
                     + showImg() + "hideImage" + frameStr() + showImg() + showImg());
    test(input, args, expOut);
  }

  @Test
  public void testBasicOr() throws Exception {
    String input = genProg("",
                           genBlock("boolean cond1",
                                    "boolean cond2",
                                    "cond1 <- false;",
                                    "cond2 <- true;",
                                    "if (cond1 | cond2)",
                                    genBlock("cond1 <- false;")));
    String[] args = {};
    String expOut = "falsetruefalse";
    test(input, args, expOut);

    String input2 = genProg("",
                           genBlock("boolean cond1",
                                    "boolean cond2",
                                    "cond1 <- false;",
                                    "cond2 <- false;",
                                    "if (cond1 | cond2)",
                                    genBlock("cond1 <- false;")));
    String[] args2 = {};
    String expOut2 = "falsefalse";
    test(input2, args2, expOut2);
  }

  @Test
  public void testBasicAnd() throws Exception {
    String input = genProg("",
                           genBlock("boolean cond1",
                                    "boolean cond2",
                                    "cond1 <- false;",
                                    "cond2 <- true;",
                                    "if (cond1 & cond2)",
                                    genBlock("cond1 <- false;")));
    String[] args = {};
    String expOut = "falsetrue";
    test(input, args, expOut);

    String input2 = genProg("",
                           genBlock("boolean cond1",
                                    "boolean cond2",
                                    "cond1 <- true;",
                                    "cond2 <- true;",
                                    "if (cond1 | cond2)",
                                    genBlock("cond1 <- false;")));
    String[] args2 = {};
    String expOut2 = "truetruefalse";
    test(input2, args2, expOut2);
  }
}
