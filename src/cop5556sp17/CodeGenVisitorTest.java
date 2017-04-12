
package cop5556sp17;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
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

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

	static final boolean doPrint = true;
	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}

	boolean devel = false;
	boolean grade = true;

//	@Before
//	public void initLog() {
//		if (devel || grade)
//			PLPRuntimeLog.initLog();
//	}
//	@After
//	public void printLog() {
//		System.out.println(PLPRuntimeLog.getString());
//	}

	@Test
	public void emptyProg() throws Exception {
		// scan, parse, and type check the program
		PLPRuntimeLog.initLog();
		String progname = "emptyProg";
		String input = progname + "  {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program);

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
		System.out.println(" wrote classfile to " + classFileName);

		// directly execute bytecode
		String[] args = new String[0]; // create command line argument array to initialize params, none
																		// in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		String expOut = "";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithParamDecs() throws Exception {
		// scan, parse, and type check the program
		PLPRuntimeLog.initLog();
		String progname = "progWithParamDecs";
		String input = progname + " integer int_foo, boolean bool_bar {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program);

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
		String expOut = "";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithLocalDecsInit() throws Exception {
		// scan, parse, and type check the program
		PLPRuntimeLog.initLog();
		String progname = "progWithLocalDecs ";
		String input = progname
				+ "{integer local_foo0 \n local_foo0 <- 5; boolean local_bool0 \n local_bool0 <- true;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String expOut = "5true";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithParamDecsInit() throws Exception {
		// scan, parse, and type check the program
		PLPRuntimeLog.initLog();
		String progname = "progWithParamDecsInit ";
		String input = progname
				+ "integer int_foo, boolean bool_bar, file f, url u "+
				"{integer loc_int0 \n loc_int0 <- 5;int_foo <- 42;\n bool_bar <- false; }";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String[] args = new String[4]; // create command line argument array to initialize params, none
																		// in this case
		args[0] = "1";
		args[1] = "true";
		args[2] = "bin/img1.jpg";
		args[3] = "http://hello";
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		String expOut = "542false";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithAllDecsInit() throws Exception {
		// scan, parse, and type check the program
		PLPRuntimeLog.initLog();
		String progname = "progWithAllDecsInit ";
		String input = progname
				+ "integer int_foo, boolean bool_bar {int_foo <- 42;\n bool_bar <- false;integer local_foo0 \n image img\n"
				+ "local_foo0 <- 5; boolean local_bool0 \n local_bool0 <- true;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String expOut = "42false5true";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithImageIdentChain() throws Exception {
		// scan, parse, and type check the program
		PLPRuntimeLog.initLog();
		String progname = "progWithImageIdentChain ";
		String input = progname
				+ "url u{image img\n"
				+ "u -> img;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String[] args = new String[1]; // create command line argument array to initialize params, none
																		// in this case
		args[0] = "https://goo.gl/66ow1x";
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		String expOut = "readFromURL(https://goo.gl/66ow1x)";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	
	@Test
	public void progWithIdentExpr() throws Exception {
		// scan, parse, and type check the program
		// globals by efault initialized
		PLPRuntimeLog.initLog();
		String progname = "progWithAllDecsInit ";
		String input = progname
				+ "integer glob_int0,integer glob_int1 {integer local_int0 \n integer local_int1 \n "
				+ "local_int0 <- 42; " // need to do this as we are assigning it to other vars
				+ "glob_int0 <- local_int0;" + "glob_int0 <- glob_int1;" + "local_int0 <- glob_int0;"
				+ "local_int1 <- local_int0;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		args[1] = "5"; // TODO: change this to int
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		String expOut = "4242555";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithBinExpr1() throws Exception {
		PLPRuntimeLog.initLog();
		String progname = "progWithBinExpr1 ";
		String input = progname + " {integer local_int0 \n integer local_int1 \n integer local_int2\n"
				+ "local_int0 <- 42; " // need to do this as we are assigning it to other vars
				+ "local_int1 <- 43;" + "local_int2 <- local_int0 + local_int1;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String expOut = "424385";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithBinExpr2() throws Exception {
		PLPRuntimeLog.initLog();
		String progname = "progWithBinExpr1 ";
		String input = progname + " {integer local_int0 \n integer local_int1 \n integer local_int2\n"
				+ "local_int0 <- 42; " // need to do this as we are assigning it to other vars
				+ "local_int1 <- 5;" + "local_int2 <- local_int0 / local_int1;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String expOut = "4258";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithBinExpr3() throws Exception {
		PLPRuntimeLog.initLog();
		String progname = "progWithBinExpr1 ";
		String input = progname + " {integer local_int0 \n integer local_int1 \n boolean local_bool0\nboolean local_bool1\n"
				+ "local_int0 <- 42; " // need to do this as we are assigning it to other vars
				+ "local_int1 <- 43;"
				+ "local_int1 <- local_int1 % local_int0;"
				+ "local_bool0 <- local_int0 >= local_int1;"
				+ "local_bool1 <- local_bool0 & false;"
				+ "local_bool1 <- local_bool1 | true;"
				+ "}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String expOut = "42431truefalsetrue";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithBinExpr4() throws Exception {
		PLPRuntimeLog.initLog();
		String progname = "progWithBinExpr1 ";
		String input = progname
				+ " {boolean local_bool0 \n boolean local_bool1 \n boolean local_bool2\n"
				+ "local_bool0 <- true; " // need to do this as we are assigning it to other vars
				+ "local_bool1 <- false;" + "local_bool2 <- local_bool0 <= local_bool1;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String expOut = "truefalsefalse";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithBinExpr5() throws Exception {
		PLPRuntimeLog.initLog();
		String progname = "progWithBinExpr1 ";
		String input = progname
				+ " {boolean local_bool0 \n boolean local_bool1 \n integer local_int0\ninteger local_int1\n"
				+ "local_bool0 <- true; " // need to do this as we are assigning it to other vars
				+ "local_bool1 <- false;" + "local_bool0 <- local_bool0 != local_bool1;"
				+ "local_int0 <- 42;" + "local_int1 <- 43;"
				+ "local_bool0 <- ((local_int0 > local_int1) < true) > false;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String expOut = "truefalsetrue4243true";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}
	
	@Test
	public void progWithBinExpr6() throws Exception {
		PLPRuntimeLog.initLog();
		String progname = "progWithBinExpr6 ";
		String input = progname
				+ "integer glob_int0 {integer local_int0\n"
				+ "local_int0 <- screenwidth;" 
				+ "glob_int0 <- screenheight;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String[] args = new String[1]; // create command line argument array to initialize params, none
																		// in this case
		args[0] = "42";
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		String expOut = "getScreenWidth1280getScreenHeight800";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}


	@Test
	public void progWithIfStmt() throws Exception {
		PLPRuntimeLog.initLog();
		String progname = "progWithIfStmt ";
		String input = progname + " {integer local_int0\ninteger local_int1\n" + "local_int0 <- 42;"
				+ "local_int1 <- 43;"
				+ "if(local_int0 == local_int1){integer local_int2 \n local_int2 <- 44;} "
				+ "if(local_int0 != local_int1){integer local_int2 \n local_int2 <- 45;}"
				+ "if(local_int0 != local_int1){integer local_int3 \n local_int3 <- 46;integer local_int4 \n local_int4 <- 47;}"
				+ "}";
		PLPRuntimeLog.initLog();
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String expOut = "4243454647";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

	@Test
	public void progWithWhileStmt() throws Exception {
		PLPRuntimeLog.initLog();
		String progname = "progWithWhileStmt ";
		String input = progname + " {integer local_int0\ninteger local_int1\n boolean local_exp \n"
				+ "local_int0 <- 40;" + "local_int1 <- 43;" + "local_exp <-local_int0 < local_int1; "
				+ "while(local_int0 != local_int1){integer local_int2 \n local_int2 <- 11; local_int0 <- local_int0 + 1;} "
				+ "while(local_int0 != local_int1 + 5){integer local_int3 \n local_int3 <- 11;"
				+ "local_int0 <- local_int0 + 1;}" + "}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		// show(program); // prints

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
		String expOut = "4043true11411142114311441145114611471148";
		PrintStream oldStream = null;
		if (expOut != null) {
			oldStream = System.out;
			System.setOut(new PrintStream(outContent));
		}
		instance.run();
		if (expOut != null) {
			assertEquals(expOut, PLPRuntimeLog.getString());
			System.setOut(oldStream);
		}
	}

}
