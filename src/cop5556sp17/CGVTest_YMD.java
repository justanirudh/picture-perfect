package cop5556sp17;


import cop5556sp17.AST.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CGVTest_YMD
{

    private String sleepStatementTemplate(int duration)
    {
        return "sleep " + duration + ";";
    }

    // Duration in milliseconds
    private static final int SHOW_DURATION = 800;
    private static final String ME_IRL_URL = "http://i299.photobucket.com/albums/mm301/Clownboss/orangoutan.jpg";
    private static final String BEAR_PIC_URL = "http://www.sovietrussia.net/wp-content/uploads/2014/02/soviet-russia-13.jpg";
    private static final String PIC_FILE_PATH = "bin/ms.jpg";
    private static final String HELLO_DAVE = "https://avatars3.githubusercontent.com/u/3663231?v=3&s=460";

    static boolean doPrint = true;

    static void show(Object s)
    {
        if( doPrint )
        {
            System.out.println(s);
        }
    }

    boolean devel = false;
    boolean grade = true;

    private static String executeByteCode(byte[] bytecode, String name, String[] args)
        throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        PrintStream oldStream = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));

        Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
        instance.run();

        System.setOut(oldStream);

        return PLPRuntimeLog.getString();
    }

    private static void writeByteCodeToFile(String name, byte[] bytecode) throws IOException
    {
        String classFileName = "bin/" + name + ".class";
        OutputStream output = new FileOutputStream(classFileName);

        output.write(bytecode);
        output.close();
        System.out.println("wrote classfile to " + classFileName);
    }

    /**
     * This class encapsulates the behaviour for mapping programs
     * in the source langugae to Java8 per the specifications given
     * in the assignment, given a parsed program.
     * <p>
     * I'm assuming that there is at least a one-one correspondence between the
     * original source
     * and the java source.
     * <p>
     * This depends on com.google.googlejavaformat:google-java-format:1.3
     * for getting a (more) readable output
     */
    private class JavaTranslator
    {
        private ASTNode program;

        private HashMap<Type.TypeName, String> typeMap = new HashMap<>();

        /**
         * Template for equivalent java source
         */
        private String progTemplate = "public class %1$s implements Runnable {\n"
            + "   // Instance vars generated from List<ParamDec>\n" + "   %2$s\n" + "   // Constructor\n"
            + "   public %1$s(String[] args) {\n" + "       // TODO: Initialize with values from args\n"
            + "       %4$s\n" + "   }\n" + "   public static void main(String[] args) {\n"
            + "       (new %1$s(args)).run();\n" + "   }\n" + "   public void run() %3$s" + "}";

        /**
         * Template for Dec & ParamDec
         */
        private String declarationTemplate = "%1$s %2$s;\n";
        private String instanceDecTemplate = "public %1$s %2$s;\n";

        /**
         * Generic block template
         */
        private String blockTemplate = "{\n" + "   // declaration list\n" + "   %1$s" + "   // statement list\n"
            + "   %2$s" + "}\n";

        /**
         * All statement templates
         */
        private String ifStmtTemplate = "if( %1$s ) %2$s\n";

        private String whileStmtTemplate = "while( %1$s ) %2$s\n";

        private String assignStmtTemplate = "%1$s = %2$s;\n";

        /**
         * Generates equivalent java source for the given program. Poor man's
         * transpiler
         *
         * @param program Program to be transpiled to java
         * @return Source string in pure glorious java
         */
        private String walkProgram(ASTNode program)
        {
            String name = (( Program ) program).getName();
            List<ParamDec> pdecs = (( Program ) program).getParams();
            Block block = (( Program ) program).getB();

            return String.format(progTemplate, name, walkParams(pdecs), walkBlock(block), genCons(pdecs));
        }

        private String genCons(List<ParamDec> paramDecs)
        {
            StringBuffer sb = new StringBuffer();
            for( ParamDec p : paramDecs )
            {
                switch( p.getTypeName() )
                {
                    case INTEGER:
                        sb.append(
                            String.format(
                                "this.%s = Integer.parseInt(args[%d]);\n", p.getIdent().getText(),
                                paramDecs.indexOf(p)
                            )
                        );
                        break;
                    case BOOLEAN:
                        sb.append(
                            String.format(
                                "this.%s = Boolean.parseBoolean(args[%d]);\n", p.getIdent().getText(),
                                paramDecs.indexOf(p)
                            )
                        );
                        break;
                    // For now complex types will not be initialized
                    // But the same assignment pattern applies
                    case IMAGE:
                    case URL:
                    case FILE:
                    case FRAME:
                        break;
                    case NONE:
                        sb.append(String.format("this.%s = null;\n", p.getIdent().getText()));
                        break;
                    default:
                        break;
                }
            }
            return sb.toString();
        }

        /**
         * Generates java variable declarations given a list of ParamDecs
         *
         * @param paramDecs list of declarations to traverse
         * @return equivalent newline seperated java declarations
         */
        private String walkParams(List<ParamDec> paramDecs)
        {
            StringBuffer sb = new StringBuffer();

            if( paramDecs.size() == 0 )
            {
                return "";
            }

            for( ParamDec paramDec : paramDecs )
            {
                sb.append(
                    String.format(
                        instanceDecTemplate, typeMap.get(paramDec.getTypeName()), paramDec.getIdent().getText()
                    )
                );
            }

            return sb.toString();
        }

        /**
         * A healthy method. Walks a block for you. Everytime.
         *
         * @param block Block object to traverse
         * @return Java source for the block passed in
         */
        private String walkBlock(Block block)
        {
            List<Dec> decs = block.getDecs();
            List<Statement> stmts = block.getStatements();

            return String.format(blockTemplate, walkDeclarations(decs), walkStatements(stmts));
        }

        /**
         * Generates java variable declarations given a list of of Decs
         *
         * @param decs list of declarations to traverse
         * @return equivalent newline seperated java declarations
         */
        private String walkDeclarations(List<Dec> decs)
        {
            StringBuffer sb = new StringBuffer();

            if( decs.size() == 0 )
            {
                return "";
            }

            for( Dec dec : decs )
            {
                sb.append(String.format(declarationTemplate, typeMap.get(dec.getTypeName()), dec.getIdent().getText()));
            }

            return sb.toString();
        }

        /**
         * Thou shalt walk
         *
         * @param stmts List of statements to traverse
         * @return Java source representation of the statements
         */
        private String walkStatements(List<Statement> stmts)
        {
            StringBuffer sb = new StringBuffer();

            if( stmts.size() == 0 )
            {
                return "";
            }

            for( Statement stmt : stmts )
            {
                if( stmt instanceof IfStatement )
                {
                    sb.append(
                        String.format(
                            ifStmtTemplate, walkExpression((( IfStatement ) stmt).getE()),
                            walkBlock((( IfStatement ) stmt).getB())
                        )
                    );
                }
                else if( stmt instanceof WhileStatement )
                {
                    sb.append(
                        String.format(
                            whileStmtTemplate, walkExpression((( WhileStatement ) stmt).getE()),
                            walkBlock((( WhileStatement ) stmt).getB())
                        )
                    );
                }
                else if( stmt instanceof AssignmentStatement )
                {
                    sb.append(
                        String.format(
                            assignStmtTemplate, (( AssignmentStatement ) stmt).getVar().getText(),
                            walkExpression((( AssignmentStatement ) stmt).getE())
                        )
                    );
                }
                // TODO: Handle sleep statments (InterruptedException in calling
                // method?)
                // TODO: Handle chains
            }

            return sb.toString();
        }

        /**
         * Walks an expression recursively to generate a string representation
         *
         * @param e Expression to traverse
         * @return Expression represented as a string in infix notation
         */
        private String walkExpression(Expression e)
        {
            if( e instanceof ConstantExpression )
            {
                return e.getFirstToken().getText();
            }
            else if( e instanceof IntLitExpression )
            {
                return e.getFirstToken().getText();
            }
            else if( e instanceof BooleanLitExpression )
            {
                return e.getFirstToken().getText();
            }
            else if( e instanceof IdentExpression )
            {
                return e.getFirstToken().getText();
            }
            else if( e instanceof BinaryExpression )
            {
                Expression left = (( BinaryExpression ) e).getE0();
                Expression right = (( BinaryExpression ) e).getE1();
                Scanner.Token op = (( BinaryExpression ) e).getOp();

                return "(" + walkExpression(left) + " " + op.getText() + " " + walkExpression(right) + ")";
            }

            // This should never happen
            return null;
        }

        public JavaTranslator(ASTNode program)
        {
            this.program = program;

            this.typeMap.put(Type.TypeName.BOOLEAN, "boolean");
            this.typeMap.put(Type.TypeName.INTEGER, "int");
            this.typeMap.put(Type.TypeName.FILE, "java.io.File");
            this.typeMap.put(Type.TypeName.IMAGE, "java.awt.image.BufferedImage");
            this.typeMap.put(Type.TypeName.FRAME, "cop5556sp17.MyFrame");
            this.typeMap.put(Type.TypeName.URL, "java.net.URL");

            // Don't know how to deal with this for now
            this.typeMap.put(Type.TypeName.NONE, "");
        }

        /**
         * @return
         * @see JavaTranslator#walkProgram(Program)
         */
        public String translate()
        {
            return this.walkProgram(this.program);
        }
    }

    private class Gen
    {
        private String prog(String name, String paramDec, String block)
        {
            return (name + " " + paramDec + block);
        }

        private String block(String... statements)
        {
            StringBuffer result = new StringBuffer("{");
            for( String statement : statements )
            {
                result.append(statement + "\n");
            }
            result.append("}");
            return result.toString();
        }
    }

//     private class ASTReconstructor
//     {
//         private byte[] bytecode;
//
//         public ASTReconstructor(byte[] klass)
//         {
//             this.bytecode = klass;
//         }
//
//         private String typeFromJVMTypeDesc(String desc)
//         {
//             for( Type.TypeName t : Type.TypeName.values() )
//             {
//                 if( t.getJVMTypeDesc() != null && t.getJVMTypeDesc().equals(desc) )
//                 {
//                     return t.toString().toLowerCase();
//                 }
//             }
//             return null;
//         }
//
//         private void removePrintTOS(ArrayList<String> instructions)
//         {
//             Scan for a contiguous sequence of statements that start with
//             specific instructions
//             and replace those with null
//             for( int i = 0; i < instructions.size() - 2; ++i )
//             {
//                 if( instructions.get(i).equals("DUP") && instructions.get(i + 1).startsWith("INVOKESTATIC")
//                     && instructions.get(i + 2).equals(
//                     "INVOKESTATIC cop5556sp17/PLPRuntimeLog.globalLogAddEntry (Ljava/lang/String;)V") )
//                 {
//                     // ArrayList#remove's mutating behaviour, folks...
//                     instructions.remove(i);
//                     instructions.remove(i);
//                     instructions.remove(i);
//                 }
//             }
//
//         }
//
//         /**
//          * Scans for expressions greedily
//          *
//          * @param instructions op-codes to scan
//          * @return
//          */
//         private HashMap<Integer, String> replaceExpressionPatterns(ArrayList<String> instructions)
//         {
//             HashMap<Integer, String> map = new HashMap<>();
//
//             for( int i = 0; i < instructions.size(); ++i )
//             {
//                 // assign to local var
//             }
//
//             return map;
//         }
//
//         public Program reconstruct()
//         {
//             ClassNode cl = analyse(this.bytecode);
//             Gen sourceGen = new Gen();
//             String progName = cl.name;
//             StringJoiner buf = new StringJoiner(",");
//
//             // Get paramdecs
//             for( FieldNode field : cl.fields )
//             {
//                 buf.add(typeFromJVMTypeDesc(field.desc) + " " + field.name);
//             }
//
//             String paramDecs = buf.toString();
//
//             // Get block / run method
//             Printer printer = new Textifier();
//             TraceMethodVisitor runMethVisit = new TraceMethodVisitor(printer);
//             MethodNode runMeth = cl.methods.get(2);
//
//             System.out.println(runMeth.name);
//             InsnList instructions = runMeth.instructions;
//             ArrayList<String> instrs = new ArrayList<>();
//
//             for( int i = 0; i < instructions.size(); ++i )
//             {
//                 instructions.get(i).accept(runMethVisit);
//                 StringWriter sw = new StringWriter();
//                 printer.print(new PrintWriter(sw));
//                 printer.getText().clear();
//                 instrs.add(sw.toString().trim());
//             }
//
//             System.out.println(Arrays.toString(instrs.toArray(new String[0])));
//
//             removePrintTOS(instrs);
//             replaceExpressionPatterns(instrs);
//
//             return null;
//         }
//     }

    private byte[] test(String source, String expOut, String[] args, boolean verbose) throws Exception
    {
        boolean oldDoPrint = doPrint;
        doPrint = verbose;
        TypeCheckVisitor typechecker = new TypeCheckVisitor();
        CodeGenVisitor codegen = new CodeGenVisitor(devel, grade, null);

        Scanner scanner = new Scanner(source);
        scanner.scan();

        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        JavaTranslator jt = new JavaTranslator(program);

        // Type-check
        program.visit(typechecker, null);

        // Generate code
        byte[] bytecode = ( byte[] ) program.visit(codegen, null);

        // output the generated bytecode
        show("\n========== BYTECODE ==============\n");

        CodeGenUtils.dumpBytecode(bytecode);

        show("\n========== JAVA SOURCE ===========\n");
        show(jt.translate());

        show("\n========== SOURCE ================\n");
        show(source);

        // write byte code to file
        String name = (( Program ) program).getName();

        CGVTest_YMD.writeByteCodeToFile(name, bytecode);

        show("\n========== EXECUTION ============\n");

        String output = CGVTest_YMD.executeByteCode(bytecode, name, args);

        if( expOut != null )
        {
            assertEquals(expOut, output);
        }

        // Restore old global print state
        doPrint = oldDoPrint;

        return bytecode;
    }

//    private ClassNode analyse(byte[] klass)
//    {
//        ClassReader classReader = new ClassReader(klass);
//        ClassNode classNode = new ClassNode();
//        classReader.accept(classNode, 0);
//        return classNode;
//    }

//    private static void testClassMetaData(ClassNode classNode, String progName, int nFields)
//    {
//        // Fixed, so bundled into method
//        String superName = "java/lang/Object";
//        String[] methNames = {"<init>", "main", "run"};
//        String[] desc = {"([Ljava/lang/String;)V", "([Ljava/lang/String;)V", "()V"};
//
//        assertEquals(progName, classNode.name);
//        assertEquals(superName, classNode.superName);
//        assertEquals(ACC_PUBLIC | ACC_SUPER, classNode.access);
//        assertEquals(nFields, classNode.fields.size());
//        assertEquals(3, classNode.methods.size());
//
//        for( int i = 0; i < classNode.methods.size(); i++ )
//        {
//            MethodNode m = ( MethodNode ) classNode.methods.get(i);
//            assertEquals(methNames[i], m.name);
//            assertEquals(desc[i], m.desc);
//        }
//    }

    @Before
    public void initLog()
    {
        if( devel || grade )
        {
            PLPRuntimeLog.initLog();
        }
    }

    @After
    public void printLog()
    {
        System.out.println(PLPRuntimeLog.getString());
    }

    @Test
    public void testEmptyCompiles() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        String input = (new Gen()).prog(progName, "", "{}");

        // test(input, null, new String[0], false);
        // Extract data from generated bytecode
        byte[] klass = test(input, null, new String[0], false);
//        ClassNode classNode = analyse(klass);

//        testClassMetaData(classNode, progName, 0);

    }

    @Test
    public void testParamInitEmptyBlockCompiles() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        String input = (new Gen()).prog(progName, "integer i0, boolean b0, file f1, url u1", "{}");
        String[] args = {"10", "true", "abc.txt", "https://stackoverflow.com/"};
        test(input, null, args, false);

        // Extract data from generated bytecode
        byte[] klass = test(input, null, args, false);
//        ClassNode classNode = analyse(klass);

//        testClassMetaData(classNode, progName, 4);


    }

    @Test
    public void testParamInitBlockNoStmtCompiles() throws Exception
    {
        Gen gen = new Gen();
        String progName = String.format("prog%d", new Date().getTime());
        String input = gen.prog(progName, "integer i, boolean b, file f, url u", "{}");
        String[] args = {"10", "true", "./CodeGenVisitorTest.java", "http://example.com/"};

        test(input, null, args, false);

        byte[] klass = test(input, null, args, false);
//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 4);


    }

    @Test
    public void testIntLitAssignment() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName), "x:I");
        Gen gen = new Gen();
        String input = gen.prog(progName, "integer a, boolean b", gen.block("integer x", "x <- 1;", "a <- 0;"));
        String[] args = {"12", "true"};
        String expOut = "10";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//
//        testClassMetaData(classNode, progName, 2);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }


    }

    @Test
    public void testIdentExpressionAssignment() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName), "x:I", "y:Z");

        Gen gen = new Gen();
        String input = gen.prog(
            progName, "integer a, boolean b", gen.block("integer x boolean y", "x <- a;", "y <- b;")
        );

        String[] args = {"12", "true"};
        String expOut = "12true";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//
//        testClassMetaData(classNode, progName, 2);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }


    }

    @Test
    public void testBoolExpressionAssignment() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName), "x:Z", "y:Z");

        Gen gen = new Gen();
        String input = gen.prog(
            progName, "boolean a, boolean b", gen.block("boolean x boolean y", "x <- a;", "y <- b;")
        );

        String[] args = {"false", "true"};
        String expOut = "falsetrue";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//
//        testClassMetaData(classNode, progName, 2);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }


    }

    @Test
    public void testBinaryExpressionAssignment() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName));

        Gen gen = new Gen();
        String input = gen.prog(
            progName, "integer x, integer y",
            gen.block("", "x <- x + 1 / 2 + 3 * 4;", "y <- y + 2 / 2 - 6 % 2;")
        );

        String[] args = {"0", "0"};
        String expOut = "121";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//
//        testClassMetaData(classNode, progName, 2);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testIfSimple() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName));

        Gen gen = new Gen();
        String input = gen.prog(progName, "boolean x", gen.block("if(x)", gen.block("x <- false;")));

        String[] args = {"true"};
        String expOut = "false";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//
//        testClassMetaData(classNode, progName, 1);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testLogic() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName), "lb0:Z", "lb1:Z");

        Gen gen = new Gen();

        String input = gen.prog(
            progName, "boolean gb0, boolean gb1",
            gen.block(
                "boolean lb0 boolean lb1",
                "lb0 <- true == true;",
                "lb0 <- true == false;",
                "lb0 <- false == true;",
                "lb0 <- false == false;",

                "lb0 <- true != true;",
                "lb0 <- true != false;",
                "lb0 <- false != true;",
                "lb0 <- false != false;",

                "lb1 <- true > true;",
                "lb1 <- true > false;",
                "lb1 <- false > true;",
                "lb1 <- false > false;",

                "lb0 <- true < true;",
                "lb0 <- true < false;",
                "lb0 <- false < true;",
                "lb0 <- false < false;",

                "lb0 <- true >= true;",
                "lb0 <- true >= false;",
                "lb0 <- false >= true;",
                "lb0 <- false >= false;",

                "lb1 <- true <= true;",
                "lb1 <- true <= false;",
                "lb1 <- false <= true;",
                "lb1 <- false <= false;"
            )
        );
        String[] args = {"true", "false"};
        String expOutShort = "tfft" + "fttf" + "ftff" + "fftf" + "ttft" + "tftt";
        String expOut = Arrays.stream(expOutShort.split("")).map(c -> c.equals("t") ? "true" : "false")
                              .collect(Collectors.joining(""));

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//
//        testClassMetaData(classNode, progName, 2);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            System.out.println((( LocalVariableNode ) runMethod.localVariables.get(
//                i)).name + ":" + (( LocalVariableNode ) runMethod.localVariables.get(i)).desc);
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testLoopingNestedBlocks() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName));

        Gen gen = new Gen();

        String input = gen.prog(
            progName, "integer x", gen.block(
                "while(x < 1000)", gen.block(
                    "if (x % 2 == 0)", gen.block("x <- 2 * x + 1;"), "if (x % 2 == 1)",
                    gen.block("x <- 2 * x;")
                )
            )
        );
        String[] args = {"0"};
        String expOut = "1251021428517034168213652730";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//
//        testClassMetaData(classNode, progName, 1);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testLoopingNestedLoops() throws Exception
    {
        // Sum of all pairs of numbers
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName), "i:I", "j:I", "sum:I");

        Gen gen = new Gen();

        String input = gen.prog(
            progName, "integer r, integer c",
            gen.block(
                "integer i integer j integer sum", "i <- 0;", "while(i < r)",
                gen.block("j <- 0;", "while (j < c)", gen.block("sum <- i + j;", "j <- j + 1;"), "i <- i + 1;")
            )
        );
        String[] args = {"2", "2"};
        String expOut = "0";

        for( int i = 0; i < Integer.parseInt(args[0]); ++i )
        {
            expOut += "0";
            for( int j = 0; j < Integer.parseInt(args[1]); ++j )
            {
                expOut += Integer.valueOf(i + j).toString();
                expOut += Integer.valueOf(j + 1).toString();
            }
            expOut += Integer.valueOf(i + 1).toString();
        }

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//
//        testClassMetaData(classNode, progName, 2);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testSleepStatementsSimple() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName), "a:I", "b:I");
        long sleepDuration = 1000;

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "",
            gen.block(
                "integer a integer b",
                "a <- 2;",
                "sleep " + sleepDuration + ";",
                "b <- 2;"
            )
        );

        String[] args = {};
        String expOut = "22";

        long start = System.currentTimeMillis();
        byte[] klass = test(input, expOut, args, false);
        long fin = System.currentTimeMillis();

        System.out.println("Slept for:" + Long.valueOf(fin - start) + "ms");

        assertTrue("Error: Sleep delay = " + Long.valueOf(fin - start) + "ms", fin > start + sleepDuration);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 0);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testSleepStatementsWithExpressions() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName));
        long sleepDuration = 0;

        Gen gen = new Gen();

        while( sleepDuration < 1000 )
        {
            if( sleepDuration % 2 == 0 )
            {
                sleepDuration = 2 * sleepDuration + 1;
            }
            if( sleepDuration % 2 == 1 )
            {
                sleepDuration = 2 * sleepDuration;
            }
        }

        sleepDuration = Math.floorDiv(
            Math.floorDiv(sleepDuration, 2) +
                Math.floorDiv(sleepDuration, 3) +
                Math.floorDiv(sleepDuration, 4),
            2
        );
        String input = gen.prog(
            progName,
            "integer x",
            gen.block(
                "while(x < 1000)", gen.block(
                    "if (x % 2 == 0)", gen.block("x <- 2 * x + 1;"),
                    "if (x % 2 == 1)", gen.block("x <- 2 * x;")
                ),
                "sleep (x / 2 + x / 3 + x / 4) / 2;"
            )
        );

        String[] args = {"0"};
        String expOut = "1251021428517034168213652730";

        long start = System.currentTimeMillis();
        byte[] klass = test(input, expOut, args, false);
        long fin = System.currentTimeMillis();

        System.out.println("Slept for:" + Long.valueOf(fin - start) + "ms");

        assertTrue("Error: Sleep delay = " + Long.valueOf(fin - start) + "ms", fin > start + sleepDuration);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testConstantExpression() throws Exception
    {
        int w = ( int ) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int h = ( int ) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName), "w:I", "h:I");

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "",
            gen.block(
                "integer w integer h",
                "w <- screenwidth;",
                "h <- screenheight;"
            )
        );

        String[] args = {"0"};
        String expOut = "getScreenWidth" + w + "getScreenHeight" + h;

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 0);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testURLArgCompiles() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName));

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "url u",
            "{}"
        );

        String[] args = {BEAR_PIC_URL};
        String expOut = "getURL(" + args[0] + ")";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testFileArgCompiles() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName));

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "file f",
            "{}"
        );

        String[] args = {"./CodeGenVisitorTest.java"};
        String expOut = null;

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }
    }

    @Test
    public void testReadingImageFromFileShowHide() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "file f",
            gen.block(
                "image im",
                "frame fr",
                "f -> im -> fr -> show;",
                sleepStatementTemplate(SHOW_DURATION),
                "fr -> hide;"
            )
        );

        // Replace this with something relevant, same image included in test dir
        String[] args = {PIC_FILE_PATH};
        String expOut = "readFromFile(" + args[0] + ")createOrSetFrame" + "showImage" + "hideImage";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
    }

    @Test
    public void testReadingImageFromURLShowHide() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "url u",
            gen.block(
                "image im",
                "frame fr",
                "integer x",
                "integer y",
                "u -> im -> fr -> show;",
                sleepStatementTemplate(SHOW_DURATION)
            )
        );

        // Replace this with something relevant, same image included in test dir
        String[] args = {ME_IRL_URL};
        String expOut = "getURL(" + args[0] + ")" + "readFromURL(" + args[0] + ")" + "createOrSetFrame" + "showImage";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
    }

    @Test
    public void testImageMovement() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "url u",
            gen.block(
                "image im",
                "frame fr",
                "integer x",
                "integer y",
                "u -> im -> fr;",
                "fr -> move (0, 0) -> move (100, 100) -> move (600, 600) -> show;",
                sleepStatementTemplate(SHOW_DURATION),
                "fr -> hide;"
            )
        );

        // Replace this with something relevant, same image included in test dir
        String[] args = {HELLO_DAVE};
        String expOut = "getURL(" + args[0] + ")" +
            "readFromURL(" + args[0] + ")" +
            "createOrSetFrame" +
            "moveFrame" + "moveFrame" + "moveFrame" +
            "showImage" +
            "hideImage";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
    }

    @Test
    public void testImageLocation() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "url u",
            gen.block(
                "image im",
                "frame fr",
                "integer x",
                "integer y",
                "integer t",
                "u -> im -> fr;",
                "fr -> move (0, 0) -> move (100, 100) -> move (600, 600);",
                "fr -> xloc -> x;",
                "fr -> yloc -> y;",
                "t <- x;",
                "x <- y;"
            )
        );

        // Replace this with something relevant, same image included in test dir
        String[] args = {HELLO_DAVE};
        String expOut = "getURL(" + args[0] + ")" +
            "readFromURL(" + args[0] + ")" +
            "createOrSetFrame" +
            "moveFrame" + "moveFrame" + "moveFrame" +
            "getX" +
            "getY" +
            "600" +
            "600";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
    }

    @Test
    public void testImageOps() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "url u",
            gen.block(
                "image im",
                "frame fr",
                "integer w",
                "integer h",
                "integer t",
                "u -> im;",
                "im -> width -> w;",
                "im -> height -> h;",
                "t <- w;",
                "w <- h;",
                "im -> scale (2) -> im;",
                "im -> width -> w;",
                "im -> height -> h;",
                "t <- w;",
                "w <- h;"
            )
        );

        // Replace this with something relevant, same image included in test dir
        String[] args = {HELLO_DAVE};
        String expOut = "getURL(" + args[0] + ")" +
            "readFromURL(" + args[0] + ")" +
            "150" + "150" +
            "scale" +
            "300" + "300";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
    }

    @Test
    public void testFilterOps() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "url u",
            gen.block(
                "image im",
//                "frame fr",
                "u -> im -> blur -> convolve -> gray -> im;"
//                "im -> fr -> show;",
            )
        );

        // Replace this with something relevant, same image included in test dir
        String[] args = {BEAR_PIC_URL};
        String expOut = "getURL(" + args[0] + ")" +
            "readFromURL(" + args[0] + ")" +
            "blurOp" +
            "convolve" +
            "grayOp";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
    }

    @Test
    public void testURLArgCopyToFile() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName));

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "url u, file f",
            gen.block(
                "image im",
                "u -> im -> f;"
            )
        );

        String[] args = {BEAR_PIC_URL, "bear.jpg"};
        String expOut =
            "getURL(" + args[0] + ")" +
                "readFromURL(" + args[0] + ")" +
                "write(" + args[1] + ")";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 2);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testFileArgCopyToFile() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());
        List<String> lvars = Arrays.asList(String.format("this:L%s;", progName));

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "file f1, file f2",
            gen.block(
                "image im",
                "f1 -> im -> f2;"
            )
        );

        String[] args = {PIC_FILE_PATH, "ytho.jpg"};
        String expOut =
            "readFromFile(" + args[0] + ")" +
                "write(" + args[1] + ")";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 2);
//
//        // Additional assertions about program structure
//        MethodNode runMethod = ( MethodNode ) classNode.methods.get(2);
//
//        for( int i = 0; i < lvars.size(); ++i )
//        {
//            LocalVariableNode lv = ( LocalVariableNode ) runMethod.localVariables.get(i);
//            assertTrue(lvars.contains(lv.name + ":" + lv.desc));
//        }

    }

    @Test
    public void testFilterOpsBarrow() throws Exception
    {
        String progName = String.format("prog%d", new Date().getTime());

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "url u",
            gen.block(
                "image im",
                "frame fr",
                "u -> im -> fr -> show;",
                sleepStatementTemplate(SHOW_DURATION),
                "im |-> gray;",
                "im -> fr;",
                sleepStatementTemplate(SHOW_DURATION),
                "im |-> blur;",
                "im -> fr;",
                sleepStatementTemplate(SHOW_DURATION),
                "im |-> convolve;",
                "im -> fr;",
                sleepStatementTemplate(SHOW_DURATION)
            )
        );

        // Replace this with something relevant, same image included in test dir
        String[] args = {BEAR_PIC_URL};
        String expOut = "getURL(" + args[0] + ")" +
            "readFromURL(" + args[0] + ")" +
            "createOrSetFrame" +
            "showImage" +
            "grayOp" +
            "createOrSetFrame" + "showImage" +
            "blurOp" +
            "createOrSetFrame" + "showImage" +
            "convolve" +
            "createOrSetFrame" + "showImage";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 1);
    }

    @Ignore("infinite loop")
    public void testBest() throws Exception
    {
        long t = new Date().getTime();
        String progName = String.format("dank%d", t);

        Gen gen = new Gen();

        String input = gen.prog(
            progName,
            "url u, integer x, integer y",
            gen.block(
                "image im",
                "integer w",
                "integer h",
                "integer sw",
                "integer sh",
                "frame fr",
                "sw <- screenwidth;",
                "sh <- screenheight;",
                "u -> im -> fr;",
                "im -> width -> w;",
                "im -> height -> h;",
                "fr -> show;",
                "while(true)",
                gen.block(
                    "fr -> move (x, y);",
                    "x <- x + sw / 100;",
                    "y <- y + sh / 100;",
                    "if((x + w) >= sw)", gen.block("x <- (x * x + w) % sw; im |-> gray; im -> fr;"),
                    "if((y + h) >= sh)", gen.block("y <- (y * y + h) % sh; im |-> blur; im -> fr;"),
                    sleepStatementTemplate(100)
                )
            )
        );

        // Replace this with something relevant, same image included in test dir
        String[] args = {ME_IRL_URL, "" + t % 500, "" + t % 500};
        String expOut = "getURL(" + args[0] + ")" +
            "readFromURL(" + args[0] + ")" +
            "blurOp" +
            "convolve" +
            "grayOp";

        byte[] klass = test(input, expOut, args, false);

//        ClassNode classNode = analyse(klass);
//        testClassMetaData(classNode, progName, 3);
    }
}