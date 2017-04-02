
package cop5556sp17;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.WhileStatement;

public class CodeGenVisitorTest {

	private class JavaTranslator {
		/**
		 * This class encapsulates the behaviour for mapping programs in the source langugae to Java8
		 * per the specifications given in the assignment, given a parsed program.
		 * <p>
		 * I'm assuming that there is at least a one-one correspondence between the original source and
		 * the java source.
		 * <p>
		 * This depends on com.google.googlejavaformat:google-java-format:1.3 for getting a (more)
		 * readable output
		 */
		private ASTNode program;
		// private com.google.googlejavaformat.java.Formatter formatter;

		private HashMap<Type.TypeName, String> typeMap = new HashMap<>();

		/**
		 * Template for equivalent java source
		 */
		private String progTemplate = "class %1$s implements Runnable {\n"
				+ "   // Instance vars generated from List<ParamDec>\n" + "   %2$s\n"
				+ "   // Constructor\n" + "   public %1$s(String[] args) {\n"
				+ "       // TODO: Initialize with values from args\n" + "   }\n"
				+ "   public static void main(String[] args) {\n"
				+ "       %1$s instance = new %1$s(args);\n" + "       instance.run();\n" + "   }\n"
				+ "   public void run() %3$s" + "}";

		/**
		 * Template for Dec & ParamDec
		 */
		private String declarationTemplate = "%1$s %2$s;\n";

		/**
		 * Generic block template
		 */
		private String blockTemplate = "{\n" + "   // declaration list\n" + "   %1$s"
				+ "   // statement list\n" + "   %2$s" + "}\n";

		/**
		 * All statement templates
		 */
		private String ifStmtTemplate = "if( %1$s ) %2$s\n";

		private String whileStmtTemplate = "while( %1$s ) %2$s\n";

		private String assignStmtTemplate = "%1$s = %2$s;\n";

		/**
		 * Generates equivalent java source for the given program. Poor man's transpiler
		 *
		 * @param program
		 *          Program to be transpiled to java
		 * @return Source string in pure glorious java
		 */
		private String walkProgram(ASTNode program) {
			String name = ((Program) program).getName();
			List<ParamDec> pdecs = ((Program) program).getParams();
			Block block = ((Program) program).getB();

			return String.format(progTemplate, name, walkParams(pdecs), walkBlock(block));
		}

		/**
		 * Generates java variable declarations given a list of ParamDecs
		 *
		 * @param decs
		 *          list of declarations to traverse
		 * @return equivalent newline seperated java declarations
		 */
		private String walkParams(List<ParamDec> paramDecs) {
			StringBuffer sb = new StringBuffer();

			if (paramDecs.size() == 0) {
				return "";
			}

			for (ParamDec paramDec : paramDecs) {
				sb.append(String.format(declarationTemplate, typeMap.get(paramDec.getTypeName()), paramDec
						.getIdent().getText()));
			}

			return sb.toString();
		}

		/**
		 * A healthy method. Walks a block for you. Everytime.
		 *
		 * @param block
		 *          Block object to traverse
		 * @return Java source for the block passed in
		 */
		private String walkBlock(Block block) {
			List<Dec> decs = block.getDecs();
			List<Statement> stmts = block.getStatements();

			return String.format(blockTemplate, walkDeclarations(decs), walkStatements(stmts));
		}

		/**
		 * Generates java variable declarations given a list of of Decs
		 *
		 * @param decs
		 *          list of declarations to traverse
		 * @return equivalent newline seperated java declarations
		 */
		private String walkDeclarations(List<Dec> decs) {
			StringBuffer sb = new StringBuffer();

			if (decs.size() == 0) {
				return "";
			}

			for (Dec dec : decs) {
				sb.append(String.format(declarationTemplate, typeMap.get(dec.getTypeName()), dec.getIdent()
						.getText()));
			}

			return sb.toString();
		}

		/**
		 * Thou shalt walk
		 *
		 * @param stmts
		 *          List of statements to traverse
		 * @return Java source representation of the statements
		 */
		private String walkStatements(List<Statement> stmts) {
			StringBuffer sb = new StringBuffer();

			if (stmts.size() == 0) {
				return "";
			}

			for (Statement stmt : stmts) {
				if (stmt instanceof IfStatement) {
					sb.append(String.format(ifStmtTemplate, walkExpression(((IfStatement) stmt).getE()),
							walkBlock(((IfStatement) stmt).getB())));
				} else if (stmt instanceof WhileStatement) {
					sb.append(String.format(whileStmtTemplate, walkExpression(((WhileStatement) stmt).getE()),
							walkBlock(((WhileStatement) stmt).getB())));
				} else if (stmt instanceof AssignmentStatement) {
					sb.append(String.format(assignStmtTemplate, ((AssignmentStatement) stmt).getVar()
							.getText(), walkExpression(((AssignmentStatement) stmt).getE())));
				}
				// TODO: Handle sleep statments (InterruptedException in calling method?)
				// TODO: Handle chains
			}

			return sb.toString();
		}

		/**
		 * Walks an expression recursively to generate a string representation
		 *
		 * @param e
		 *          Expression to traverse
		 * @return Expression represented as a string in infix notation
		 */
		private String walkExpression(Expression e) {
			if (e instanceof ConstantExpression) {
				return e.getFirstToken().getText();
			} else if (e instanceof IntLitExpression) {
				return e.getFirstToken().getText();
			} else if (e instanceof BooleanLitExpression) {
				return e.getFirstToken().getText();
			} else if (e instanceof IdentExpression) {
				return e.getFirstToken().getText();
			} else if (e instanceof BinaryExpression) {
				Expression left = ((BinaryExpression) e).getE0();
				Expression right = ((BinaryExpression) e).getE1();
				Scanner.Token op = ((BinaryExpression) e).getOp();

				return "(" + walkExpression(left) + " " + op.getText() + " " + walkExpression(right) + ")";
			}

			// This should never happen
			return null;
		}

		public JavaTranslator(ASTNode program) {
			this.program = program;
			// this.formatter = new com.google.googlejavaformat.java.Formatter();

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
		public String translate() // throws FormatterException
		{
			// return this.formatter.formatSource(walkProgram(this.program));
			return walkProgram(this.program);
		}
	}

	static final boolean doPrint = true;
	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}

	boolean devel = true;
	boolean grade = true; 

	@Test
	public void emptyProg() throws Exception {
		// scan, parse, and type check the program
		String progname = "emptyProg";
		String input = progname + "  {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program);

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode);

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName);

		// directly execute bytecode
		String[] args = new String[0]; // create command line argument array to initialize params, none
																		// in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}

	@Test
	public void progWithParamDecs() throws Exception {
		// scan, parse, and type check the program
		String progname = "progWithParamDecs";
		String input = progname + " integer int_foo, boolean bool_bar {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program);

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode);

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName);

		// directly execute bytecode
		String[] args = new String[2]; // create command line argument array to initialize params, none
																		// in this case
		args[0] = "1";
		args[1] = "true";
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}

	@Test
	public void progWithLocalDecsInit() throws Exception {
		// scan, parse, and type check the program
		String progname = "progWithLocalDecs ";
		String input = progname
				+ "{integer local_foo0 \n local_foo0 <- 5; boolean local_bool0 \n local_bool0 <- true;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[0]; // create command line argument array to initialize params, none
																		// in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}

	@Test
	public void progWithParamDecsInit() throws Exception {
		// scan, parse, and type check the program
		String progname = "progWithParamDecsInit ";
		String input = progname
				+ "integer int_foo, boolean bool_bar {int_foo <- 42;\n bool_bar <- false;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[2]; // create command line argument array to initialize params, none
																		// in this case
		args[0] = "1";
		args[1] = "true";
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}

	@Test
	public void progWithAllDecsInit() throws Exception {
		// scan, parse, and type check the program
		String progname = "progWithAllDecsInit ";
		String input = progname
				+ "integer int_foo, boolean bool_bar {int_foo <- 42;\n bool_bar <- false;integer local_foo0 \n "
				+ "local_foo0 <- 5; boolean local_bool0 \n local_bool0 <- true;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		//CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[2]; // create command line argument array to initialize params, none
																		// in this case
		args[0] = "1";
		args[1] = "true";
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}

	@Test
	public void progWithIdentExpr() throws Exception {
		// scan, parse, and type check the program
		//globals by efault initialized
		String progname = "progWithAllDecsInit ";
		String input = progname
				+ "integer glob_int0,integer glob_int1 {integer local_int0 \n integer local_int1 \n "
				+ "local_int0 <- 42; " //need to do this as we are assigning it to other vars
				+ "glob_int0 <- local_int0;"
				+ "glob_int0 <- glob_int1;"
				+ "local_int0 <- glob_int0;"
				+ "local_int1 <- local_int0;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		//CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[2]; // create command line argument array to initialize params, none
																		// in this case
		args[0] = "1";
		args[1] = "5"; //TODO: change this to int
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}

	@Test
	public void progWithBinExpr1() throws Exception {
		String progname = "progWithBinExpr1 ";
		String input = progname
				+ " {integer local_int0 \n integer local_int1 \n integer local_int2\n"
				+ "local_int0 <- 42; " //need to do this as we are assigning it to other vars
				+ "local_int1 <- 43;"
				+ "local_int2 <- local_int0 + local_int1;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[0]; // create command line argument array to initialize params, none
																		// in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}
	
	@Test
	public void progWithBinExpr2() throws Exception {
		String progname = "progWithBinExpr1 ";
		String input = progname
				+ " {integer local_int0 \n integer local_int1 \n integer local_int2\n"
				+ "local_int0 <- 42; " //need to do this as we are assigning it to other vars
				+ "local_int1 <- 5;"
				+ "local_int2 <- local_int0 / local_int1;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[0]; // create command line argument array to initialize params, none
																		// in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}
	
	@Test
	public void progWithBinExpr3() throws Exception {
		String progname = "progWithBinExpr1 ";
		String input = progname
				+ " {integer local_int0 \n integer local_int1 \n boolean local_bool0\n"
				+ "local_int0 <- 42; " //need to do this as we are assigning it to other vars
				+ "local_int1 <- 43;"
				+ "local_bool0 <- local_int0 >= local_int1;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[0]; // create command line argument array to initialize params, none
																		// in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}
	
	@Test
	public void progWithBinExpr4() throws Exception {
		String progname = "progWithBinExpr1 ";
		String input = progname
				+ " {boolean local_bool0 \n boolean local_bool1 \n boolean local_bool2\n"
				+ "local_bool0 <- true; " //need to do this as we are assigning it to other vars
				+ "local_bool1 <- false;"
				+ "local_bool2 <- local_bool0 <= local_bool1;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[0]; // create command line argument array to initialize params, none
																		// in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}

	@Test
	public void progWithBinExpr5() throws Exception {
		String progname = "progWithBinExpr1 ";
		String input = progname
				+ " {boolean local_bool0 \n boolean local_bool1 \n integer local_int0\ninteger local_int1\n"
				+ "local_bool0 <- true; " //need to do this as we are assigning it to other vars
				+ "local_bool1 <- false;"
				+ "local_bool0 <- local_bool0 != local_bool1;"
				+ "local_int0 <- 42;" 
				+ "local_int1 <- 43;"
				+ "local_bool0 <- ((local_int0 > local_int1) < true) > false;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[0]; // create command line argument array to initialize params, none
																		// in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}
	
	@Test
	public void progWithIfStmt() throws Exception {
		String progname = "progWithIfStmt ";
		String input = progname
				+ " {integer local_int0\ninteger local_int1\n"
				+ "local_int0 <- 42;" 
				+ "local_int1 <- 43;"
				+ "if(local_int0 == local_int1){integer local_int2 \n local_int2 <- 44;} "
				+ "if(local_int0 != local_int1){integer local_int2 \n local_int2 <- 45;}"
				+ "if(local_int0 != local_int1){integer local_int3 \n local_int3 <- 46;integer local_int4 \n local_int4 <- 47;}"
				+ "}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program); // prints

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
//		CodeGenUtils.dumpBytecode(bytecode); // prints

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName); // prints

		// directly execute bytecode
		String[] args = new String[0]; // create command line argument array to initialize params, none
																		// in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}
	// @Test
	public void testTranslator() throws Exception {
		// scan, parse, and type check the program
		String progname = "emptyProg";
		String input = progname + "  {y <- 1; \ninteger x\n y <- 0; \ninteger y \n }";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		JavaTranslator jt = new JavaTranslator(program);
		show(jt.translate());
	}

}
