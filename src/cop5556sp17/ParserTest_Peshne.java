package cop5556sp17;

import java.util.Random;
import java.lang.StringBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;


public class ParserTest_Peshne {
  private static final Random rnd = new Random();
  private static final int MAX_DEPTH = 30;
  private static final int MAX_PROG_LEN = 40;
  private static final int MAX_AUTO_GEN_TEST_CASES = 50;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

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
    System.out.println(scanner);
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
  public void testParamDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
    String[] params = {"url", "file", "integer", "boolean"};

    for (String param : params) {
      new Tester(param + " IDENT", "paramDec").test();
    }

    new Tester("goodProg integer abc, file f { abc <- 122; }").test();

    thrown.expect(Parser.SyntaxException.class);
    new Tester("badProg integer abc file f { abc <- 122; }").test();

    thrown.expect(Parser.SyntaxException.class);
    new Tester("badProg1 integer file f { abc <- 122; }").test();
  }

  private interface Generator {
    public StringBuilder next();
    public Boolean hasNext();
  }

  private static class StrongOpGenerator implements Generator {
    private static final String[] STRONG_OPS = {" * ",   " / " ,  " & ", " % "};
    private int index = 0;
    public StringBuilder next() {
      if (index < StrongOpGenerator.STRONG_OPS.length) {
        return new StringBuilder(StrongOpGenerator.STRONG_OPS[index++]);
      } else {
        return new StringBuilder(StrongOpGenerator.STRONG_OPS[ParserTest_Peshne.rnd.nextInt(4)]);
      }
    }

    public Boolean hasNext() {
      return (index < StrongOpGenerator.STRONG_OPS.length);
    }
  }

  private static class WeakOpGenerator implements Generator {
    private static final String[] WEAK_OPS = {" + ", " - ", " | "};
    private int index = 0;
    public StringBuilder next() {
      if (index < WeakOpGenerator.WEAK_OPS.length) {
        return new StringBuilder(WeakOpGenerator.WEAK_OPS[index++]);
      } else {
        return new StringBuilder(WeakOpGenerator.WEAK_OPS[ParserTest_Peshne.rnd.nextInt(3)]);
      }
    }

    public Boolean hasNext() {
      return (index < WeakOpGenerator.WEAK_OPS.length);
    }
  }

  private static class RelOpGenerator implements Generator {
    private static final String[] REL_OPS = {" < ", " <= ", " > ", " >= ", " == ",
                                             " != "};
    private int index = 0;
    public StringBuilder next() {
      if (index < RelOpGenerator.REL_OPS.length) {
        return new StringBuilder(RelOpGenerator.REL_OPS[index++]);
      } else {
        return new StringBuilder(RelOpGenerator.REL_OPS[ParserTest_Peshne.rnd.nextInt(6)]);
      }
    }
    public Boolean hasNext() {
      return (index < RelOpGenerator.REL_OPS.length);
    }
  }

  private static class FactorGenerator implements Generator {
    private static final String[] FACTORS = {" ident "/*IDENT*/, " 123 " /*INT_LIT*/,
                                             " true ", " false ", " screenwidth ",
                                             " screenheight "};
    private int index;
    private ExpressionGenerator expG;

    public FactorGenerator(int curDepth) {
      index = 0;
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        expG = new ExpressionGenerator(curDepth + 1);
      }
    }

    public StringBuilder next() {
      if (index < FactorGenerator.FACTORS.length) {
        return new StringBuilder(FactorGenerator.FACTORS[index++]);
      }
      if (expG != null && expG.hasNext()) {
         return expG.next().insert(0, "(").append(")");
      }
      return new StringBuilder(FactorGenerator.FACTORS[ParserTest_Peshne.rnd.nextInt(6)]);
    }

    public Boolean hasNext() {
      return (index < FactorGenerator.FACTORS.length ||
              (expG != null && expG.hasNext()));
    }
  }

  private static class ElemGenerator implements Generator {
    private FactorGenerator facG;
    private StrongOpGenerator stOpG = new StrongOpGenerator();

    public ElemGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        facG = new FactorGenerator(curDepth + 1);
      }
    }
    public StringBuilder next() {
      if (facG != null && facG.hasNext()) {
        StringBuilder result = facG.next();
        for (int i = 0; i < ParserTest_Peshne.rnd.nextInt(3) && facG.hasNext(); i++) {
          if (!stOpG.hasNext()) {
            stOpG = new StrongOpGenerator();
          }
          result.append(stOpG.next()).append(facG.next());
        }
        return result;
      } else {
        return new StringBuilder(new FactorGenerator(ParserTest_Peshne.MAX_DEPTH).next());
      }
    }

    public Boolean hasNext() {
      return facG != null && facG.hasNext();
    }
  }

  private static class TermGenerator implements Generator {
    private ElemGenerator elemG;
    private WeakOpGenerator wkOpG;

    public TermGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        wkOpG = new WeakOpGenerator();
        elemG = new ElemGenerator(curDepth + 1);
      }
    }

    public StringBuilder next() {
      if (elemG != null && elemG.hasNext()) {
        StringBuilder result = elemG.next();
        for (int i = 0; i < ParserTest_Peshne.rnd.nextInt(3) && elemG.hasNext(); i++) {
          if (!wkOpG.hasNext()) {
            wkOpG = new WeakOpGenerator();
          }
          result.append(wkOpG.next()).append(elemG.next());
        }
        return result;
      } else {
        return new StringBuilder(new ElemGenerator(ParserTest_Peshne.MAX_DEPTH).next());
      }
    }

    public Boolean hasNext() {
      return elemG != null && elemG.hasNext();
    }
  }

  private static class ExpressionGenerator implements Generator {
    private TermGenerator termG;
    private RelOpGenerator rlOpG;

    public ExpressionGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        termG = new TermGenerator(curDepth + 1);
        rlOpG = new RelOpGenerator();
      }
    }

    public StringBuilder next() {
      if (termG != null && termG.hasNext()) {
        StringBuilder result = termG.next();
        for (int i = 0; i < ParserTest_Peshne.rnd.nextInt(3) && termG.hasNext(); i++) {
          if (!rlOpG.hasNext()) {
            rlOpG = new RelOpGenerator();
          }
          result.append(rlOpG.next()).append(termG.next());
        }
        return result;
      } else {
        return new StringBuilder(new TermGenerator(ParserTest_Peshne.MAX_DEPTH).next());
      }
    }

    public Boolean hasNext() {
      return termG != null && termG.hasNext();
    }
  }

  private static class ArgGenerator implements Generator {
    private ExpressionGenerator expG;

    public ArgGenerator() {
      this(ParserTest_Peshne.MAX_DEPTH - 1);
    }

    public ArgGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        expG = new ExpressionGenerator(curDepth + 1);
      }
    }

    public StringBuilder next() {
      if (expG != null && expG.hasNext()) {
        if ((ParserTest_Peshne.rnd.nextInt(2)) > 0) {
          StringBuilder result = new StringBuilder("(");
          result.append(expG.next());
          for (int i = 0; i < (ParserTest_Peshne.rnd.nextInt(3)) && expG.hasNext(); i++) {
            result.append(",").append(expG.next());
          }
          result.append(")");
          return result;
        }
      }
      return new StringBuilder(); // return epsilon
    }

    public Boolean hasNext() {
      return expG != null && expG.hasNext(); // I know, we can return epsilon, this is to make it finite
    }
  }

  private static class ImageOpGenerator implements Generator {
    private static final String[] IMAGE_OPS = {" width ", " height ", " scale "};
    private int index = 0;
    public StringBuilder next() {
      if (index < ImageOpGenerator.IMAGE_OPS.length) {
        return new StringBuilder(ImageOpGenerator.IMAGE_OPS[index++]);
      } else {
        return new StringBuilder(ImageOpGenerator.IMAGE_OPS[ParserTest_Peshne.rnd.nextInt(3)]);
      }
    }
    public Boolean hasNext() {
      return (index < ImageOpGenerator.IMAGE_OPS.length);
    }
  }


  private static class FrameOpGenerator implements Generator {
    private static final String[] FRAME_OPS = {" show ", " hide ", " move ",
                                               " xloc ", " yloc "};
    private int index = 0;
    public StringBuilder next() {
      if (index < FrameOpGenerator.FRAME_OPS.length) {
        return new StringBuilder(FrameOpGenerator.FRAME_OPS[index++]);
      } else {
        return new StringBuilder(FrameOpGenerator.FRAME_OPS[ParserTest_Peshne.rnd.nextInt(5)]);
      }
    }
    public Boolean hasNext() {
      return (index < FrameOpGenerator.FRAME_OPS.length);
    }
  }


  private static class FilterOpGenerator implements Generator {
    private static final String[] FILTER_OPS = {" blur ", " gray ", " convolve "};
    private int index = 0;
    public StringBuilder next() {
      if (index < FilterOpGenerator.FILTER_OPS.length) {
        return new StringBuilder(FilterOpGenerator.FILTER_OPS[index++]);
      } else {
        return new StringBuilder(FilterOpGenerator.FILTER_OPS[ParserTest_Peshne.rnd.nextInt(3)]);
      }
    }
    public Boolean hasNext() {
      return (index < FilterOpGenerator.FILTER_OPS.length);
    }
  }

  private static class ChainElemGenerator implements Generator {
    private int index;
    private FilterOpGenerator filG;
    private ArgGenerator argG;
    private FrameOpGenerator framG;
    private ImageOpGenerator imageG;

    public ChainElemGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        argG = new ArgGenerator(curDepth + 1);
        index = 0;
        filG = new FilterOpGenerator();
        framG = new FrameOpGenerator();
        imageG = new ImageOpGenerator();
      }
    }
    public StringBuilder next() {
      StringBuilder result = null;
      switch (index) {
      case 0:
        result = new StringBuilder("abc /*random identifier*/");
        index++;
        break;
      case 1:
        if (!argG.hasNext()) {
          argG = new ArgGenerator();
        }
        if (filG != null && filG.hasNext()) {
          result = filG.next();
          result.append(argG.next());
          if (!filG.hasNext()) {
            index++;
          }
        }
        break;
      case 2:
        if (!argG.hasNext()) {
          argG = new ArgGenerator();
        }
        if (framG != null && framG.hasNext()) {
          result = framG.next();
          result.append(argG.next());
          if (!framG.hasNext()) {
            index++;
          }
        }
        break;
      case 3:
        if (!argG.hasNext()) {
          argG = new ArgGenerator();
        }
        if (imageG != null && imageG.hasNext()) {
          result = imageG.next();
          result.append(argG.next());
          if (!imageG.hasNext()) {
            index++;
          }
        }
        break;
      default:
        result = null;
      }
      if (result == null) {
        result = new StringBuilder(" rndIdent ");
      }
      return result;
    }
    public Boolean hasNext() {
      return filG != null && framG != null && imageG != null && (index < 4); // 4 cases in switch case
    }
  }

  private static class ArrowOpGenerator implements Generator {
    private static final String[] ARROW_OPS = {" -> ", " |-> "};
    private int index = 0;
    public StringBuilder next() {
      if (index < ArrowOpGenerator.ARROW_OPS.length) {
        return new StringBuilder(ArrowOpGenerator.ARROW_OPS[index++]);
      } else {
        return new StringBuilder(ArrowOpGenerator.ARROW_OPS[ParserTest_Peshne.rnd.nextInt(2)]);
      }
    }
    public Boolean hasNext() {
      return (index < ArrowOpGenerator.ARROW_OPS.length);
    }
  }

  private static class IfStatementGenerator implements Generator {
    private ExpressionGenerator expG;
    private BlockGenerator blockG;

    public IfStatementGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        expG = new ExpressionGenerator(curDepth + 1);
        blockG = new BlockGenerator(curDepth + 1);
      }
    }

    public StringBuilder next() {
      StringBuilder result = new StringBuilder(" if ( ");
      if (expG != null && expG.hasNext()) {
        result.append(expG.next());
        result.append(" ) ");
        if (!blockG.hasNext()) {
          blockG = new BlockGenerator();
        }
        result.append(blockG.next());
      } else {
        result.append(new ExpressionGenerator(ParserTest_Peshne.MAX_DEPTH).next());
        result.append(" ) ");
        result.append(new BlockGenerator(ParserTest_Peshne.MAX_DEPTH).next());
      }
      return result;
    }

    public Boolean hasNext() {
      return expG != null && expG.hasNext();
    }
  }

  private static class WhileStatementGenerator implements Generator {
    private ExpressionGenerator expG;
    private BlockGenerator blockG;

    public WhileStatementGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        expG = new ExpressionGenerator(curDepth + 1);
        blockG = new BlockGenerator(curDepth + 1);
      }
    }

    public StringBuilder next() {
      StringBuilder result = new StringBuilder(" while ( ");
      if (expG != null && expG.hasNext()) {
        result.append(expG.next());
        result.append(" ) ");
        if (!blockG.hasNext()) {
          blockG = new BlockGenerator();
        }
        result.append(blockG.next());
      } else {
        result.append(new ExpressionGenerator(ParserTest_Peshne.MAX_DEPTH).next());
        result.append(" ) ");
        result.append(new BlockGenerator(ParserTest_Peshne.MAX_DEPTH).next());
      }
      return result;
    }

    public Boolean hasNext() {
      return expG != null && expG.hasNext();
    }
  }

  private static class ChainGenerator implements Generator {
    private ChainElemGenerator chainElemG;
    private ArrowOpGenerator arrowG;

    public ChainGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        chainElemG = new ChainElemGenerator(curDepth + 1);
        arrowG = new ArrowOpGenerator();
      }
    }

    public StringBuilder next() {
      if (chainElemG != null && chainElemG.hasNext()) {
        StringBuilder result = chainElemG.next();
        if (!arrowG.hasNext()) {
          arrowG = new ArrowOpGenerator();
        }
        result.append(arrowG.next());

        if (!chainElemG.hasNext()) {
          result.append(new ChainElemGenerator(ParserTest_Peshne.MAX_DEPTH - 1).next()); //this is cheating
        } else {
          result.append(chainElemG.next());
        }
        for (int i = 0; (i < (ParserTest_Peshne.rnd.nextInt(3))) && chainElemG.hasNext(); i++) {
          if (!arrowG.hasNext()) {
            arrowG = new ArrowOpGenerator();
          }
          result.append(arrowG.next());
          result.append(chainElemG.next());
        }
        return result;
      } else {
        return new StringBuilder(new ChainElemGenerator(ParserTest_Peshne.MAX_DEPTH).next())
          .append(new ArrowOpGenerator().next())
          .append(new ChainElemGenerator(ParserTest_Peshne.MAX_DEPTH).next());
      }
    }
    public Boolean hasNext() {
      return chainElemG != null && chainElemG.hasNext();
    }
  }

  private static class AssignGenerator implements Generator {
    private ExpressionGenerator expG;

    public AssignGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        expG = new ExpressionGenerator(curDepth + 1);
      }
    }

    public StringBuilder next() {
      StringBuilder result = new StringBuilder(" dummyIdent ");
      result.append(" <- "); //assign
      if (expG != null && expG.hasNext()) {
        result.append(expG.next());
      } else {
        result.append(new ExpressionGenerator(ParserTest_Peshne.MAX_DEPTH).next());
      }
      return result;
    }

    public Boolean hasNext() {
      return expG != null && expG.hasNext();
    }
  }

  private static class StatementGenerator implements Generator {
    private ExpressionGenerator expG;
    private WhileStatementGenerator whileG;
    private IfStatementGenerator ifG;
    private ChainGenerator chainG;
    private AssignGenerator assignG;
    private int index = 0;

    public StatementGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        expG = new ExpressionGenerator(curDepth + 1);
        whileG = new WhileStatementGenerator(curDepth + 1);
        ifG = new IfStatementGenerator(curDepth + 1);
        chainG = new ChainGenerator(curDepth + 1);
        assignG = new AssignGenerator(curDepth + 1);
        index = 0;
      } else {
        index = 999;
      }
    }

    public StringBuilder next() {
      StringBuilder result = new StringBuilder();
      switch(index) {
      case 0:
        if (expG != null && expG.hasNext()) {
          result.append(" sleep ");
          result.append(expG.next());
          result.append(";\n");
          if (!expG.hasNext()) {
            index++;
          }
        }
        break;
      case 1:
        if (whileG != null && whileG.hasNext()) {
          result.append(whileG.next());
          if (!whileG.hasNext()) {
            index++;
          }
        }
        break;
      case 2:
        if (ifG != null && ifG.hasNext()) {
          result.append(ifG.next());
          if (!ifG.hasNext()) {
            index++;
          }
        }
        break;
      case 3:
        if (chainG != null && chainG.hasNext()) {
          result.append(chainG.next());
          result.append(";\n");
          if (!chainG.hasNext()) {
            index++;
          }
        }
        break;
      case 4:
        if (assignG != null && assignG.hasNext()) {
          result.append(assignG.next());
          result.append(";\n");
          if (!assignG.hasNext()) {
            index++;
          }
        }
        break;
      default:
        result.append(new AssignGenerator(ParserTest_Peshne.MAX_DEPTH).next())
          .append(";\n");
      }
      return result;
    }

    public Boolean hasNext() {
      return (index < 5);
    }
  }

  private static class DecGenerator implements Generator {
    private static final String[] DEC = {" integer ", " boolean ", " image ", " frame "};
    private int index = 0;

    public StringBuilder next() {
      if (index < DecGenerator.DEC.length) {
        return new StringBuilder(DecGenerator.DEC[index++]).append(" dummyIdent ");
      } else {
        return new StringBuilder(DecGenerator.DEC[ParserTest_Peshne.rnd.nextInt(4)])
          .append(" dummyIdent ");
      }
    }
    public Boolean hasNext() {
      return (index < DecGenerator.DEC.length);
    }
  }

  private static class BlockGenerator implements Generator {
    private DecGenerator decG;
    private StatementGenerator statG;
    //    private Boolean used;

    public BlockGenerator() {
      this(ParserTest_Peshne.MAX_DEPTH - 1);
    }

    public BlockGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        decG = new DecGenerator();
        statG = new StatementGenerator(curDepth + 1);
      }
    }

    public StringBuilder next() {
      StringBuilder result = new StringBuilder("{\n");
      if ((decG != null || statG != null)) {
        for (int i = 0; i < (ParserTest_Peshne.rnd.nextInt(ParserTest_Peshne.MAX_PROG_LEN / 2)); i++) {
          result.append(decG.next());
        }
        for (int i = 0; i < (ParserTest_Peshne.rnd.nextInt(ParserTest_Peshne.MAX_PROG_LEN / 2)); i++) {
          result.append(statG.next());
        }
      } else {
        result.append(new DecGenerator().next());
        result.append(new StatementGenerator(ParserTest_Peshne.MAX_DEPTH).next());
      }
      result.append("}\n");
      return result;
    }

    public Boolean hasNext() {
      return (decG != null || statG != null);
    }
  }


  private static class ParamDecGenerator implements Generator {
    private static final String[] PARAM_DEC = {" url ", " file ", " integer ", " boolean "};
    private int index = 0;
    public StringBuilder next() {
      if (index < ParamDecGenerator.PARAM_DEC.length) {
        return new StringBuilder(ParamDecGenerator.PARAM_DEC[index++]).append(" dummyIdent\n");
      } else {
        return new StringBuilder(ParamDecGenerator.PARAM_DEC[ParserTest_Peshne.rnd.nextInt(4)])
          .append(" dummyIdent\n");
      }
    }
    public Boolean hasNext() {
      return (index < ParamDecGenerator.PARAM_DEC.length);
    }
  }

  private static class ProgramGenerator implements Generator {
    private BlockGenerator blockG;
    private ParamDecGenerator paramG;
    private int index;

    public ProgramGenerator() {
      this(0);
    }
    public ProgramGenerator(int curDepth) {
      if (curDepth < ParserTest_Peshne.MAX_DEPTH) {
        blockG = new BlockGenerator(curDepth + 1);
        paramG = new ParamDecGenerator();
        index = 0;
      }
    }

    public StringBuilder next() {
      StringBuilder result = new StringBuilder(" ident /*random ident*/ ");
      if (index == 0 && blockG.hasNext()) {
        result.append(blockG.next());
        if (!blockG.hasNext()) {
          index++;
          blockG = new BlockGenerator(ParserTest_Peshne.MAX_DEPTH - 1);
        }
      } else if (index == 1 && blockG.hasNext()) {
        if (!paramG.hasNext()) {
          paramG = new ParamDecGenerator();
        }
        result.append(paramG.next());
        for (int i = 0; i < (ParserTest_Peshne.rnd.nextInt(2)) && paramG.hasNext(); i++) {
          result.append(",");
          result.append(paramG.next());
        }
        result.append(blockG.next());
        if (!blockG.hasNext()) {
          index++;
        }
      } else {
        result = null;
      }
      return result;
    }

    public Boolean hasNext() {
      return (index < 2 && !blockG.hasNext());
    }
  }

  @Test
  public void autoGen() throws IllegalCharException, IllegalNumberException, SyntaxException {
    ProgramGenerator pG = new ProgramGenerator();
    for (int i = 0; i < MAX_AUTO_GEN_TEST_CASES; i++) {
      StringBuilder magic = pG.next();
      System.out.println(magic.toString());
      new Tester(magic.toString()).test();
    }
  }

  @Test
  public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException{
    String input = "prog0 {}";
    Parser parser = new Parser(new Scanner(input).scan());
    parser.parse();
  }

  @Test
  public void testProgram1() throws IllegalCharException, IllegalNumberException, SyntaxException {
    new Tester("prog1 { ab <- 12; }", "parse").test();

    thrown.expect(Parser.SyntaxException.class);
    new Tester("prog1 { an <- 12 }").test();
  }

  private class Tester {
    private String input;
    private String method;

    public Tester(String input, String method) {
      this.input = input;
      this.method = method;
    }

    public Tester(String input) {
      this(input, "parse");
    }

    public void test() throws IllegalCharException, IllegalNumberException, SyntaxException {
      Scanner scanner = new Scanner(this.input);
      scanner.scan();
      Parser parser = new Parser(scanner);
      try {
        Parser.class.getDeclaredMethod(this.method).invoke(parser);
      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (java.lang.reflect.InvocationTargetException e) {
        Exception innerException = (Exception) e.getCause();
        if (innerException.getClass() == SyntaxException.class) {
          throw (SyntaxException) innerException;
        } else if (innerException.getClass() == IllegalCharException.class) {
          throw (IllegalCharException) innerException;
        } else if (innerException.getClass() == IllegalNumberException.class) {
          throw (IllegalNumberException) innerException;
        } else {
          innerException.printStackTrace();
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

}
