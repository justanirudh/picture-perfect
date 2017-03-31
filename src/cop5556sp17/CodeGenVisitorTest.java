
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

	boolean devel = false;
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
