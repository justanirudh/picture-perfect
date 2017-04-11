package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
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
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.WhileStatement;
import cop5556sp17.AST.Type.TypeName;

import static cop5556sp17.AST.Type.TypeName.BOOLEAN;
import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.INTEGER;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {
	/**
	 * @param DEVEL
	 *          used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *          used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *          name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		localVars = new HashMap<>();
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	FieldVisitor fv; // for visiting field variables

	class Labels {
		private Label startLabel;
		private Label endLabel;
		Labels(Label startL) {
			startLabel = startL;
		}
		public Label getStartLabel() {
			return startLabel;
		}
		public Label getEndLabel() {
			return endLabel;
		}
		public void setEndLabel(Label l) {
			endLabel = l;
		}
	}

	HashMap<Dec, Labels> localVars;

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[]{
				"java/lang/Runnable"});
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (int i = 0; i < params.size(); ++i) {
			params.get(i).visit(this, i);
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0); // String[] args from it's slot number
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, 1); // visit block, start with slotnum 1 as 0 taken by this
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		for (Dec dec : localVars.keySet()) {
			Labels ls = localVars.get(dec);
			mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, ls
					.getStartLabel(), ls.getEndLabel(), dec.getSlotNum());
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method

		cw.visitEnd();// end of class

		// generate classfile and return it
		return cw.toByteArray();
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// Note: For assignment 5, only needs to handle integers and booleans
		String fieldName = paramDec.getIdent().getText(); // name of the field
		String fieldType = null; // type descriptor of field in JVM notation
		Object initValue = null; // Object containing initial value of field

		Integer offset = (Integer) arg;

		TypeName decType = paramDec.getTypeName();
		if (decType.isType(TypeName.INTEGER)) {
			fieldType = "I";
		} else if (decType.isType(TypeName.BOOLEAN)) {
			fieldType = "Z";
		} else
			assert false : "not yet implemented";

		// telling asm to add this field
		fv = cw.visitField(ACC_PUBLIC, fieldName, fieldType, null, initValue);
		fv.visitEnd();

		// populate the field
		mv.visitVarInsn(ALOAD, 0); // this
		mv.visitVarInsn(ALOAD, 1);// args
		mv.visitLdcInsn(new Integer(offset)); // depending upon which index in args array
		mv.visitInsn(AALOAD); // get the arg
		if (decType.isType(TypeName.INTEGER))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",
					false);
		else if (decType.isType(TypeName.BOOLEAN))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z",
					false);
		else
			assert false : "not yet implemented";

		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);

		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// (https://ufl.instructure.com/courses/335277/discussion_topics/1399099)

		int startSlotNum = (Integer) arg; // every block starts with the slot number passed to it

		ArrayList<Dec> decList = block.getDecs();
		for (int i = 0; i < decList.size(); ++i)
			decList.get(i).visit(this, startSlotNum + i);

		ArrayList<Statement> stmtList = block.getStatements();
		for (Statement stmt : stmtList) {
			// passing the startslotNum for the new stmt (if or while)
			stmt.visit(this, startSlotNum + decList.size());
			if (stmt instanceof AssignmentStatement) {
				Label l = new Label();
				Dec currDec = ((AssignmentStatement) stmt).getVar().getDec(); // putting start label
				// if paramdec or if already in map, dont add it
				if (!(currDec instanceof ParamDec) && !localVars.containsKey(currDec)) {
					// put in localvars so as to visit it visitLocalVar in visitProg()
					localVars.put(currDec, new Labels(l));
				}
				mv.visitLabel(l);
			}
		}

		Label endBlock = new Label();
		for (Statement stmt : stmtList) { // putting end Labels
			if (stmt instanceof AssignmentStatement) {
				Dec currDec = ((AssignmentStatement) stmt).getVar().getDec();
				if (!(currDec instanceof ParamDec))
					localVars.get(currDec).setEndLabel(endBlock);
			}
		}
		mv.visitLabel(endBlock);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		int slotNum = (Integer) arg;
		declaration.setSlotNum(slotNum);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg)
			throws Exception {
		// Note: reverse traversal as that of TypeVisitor because need to load before store

		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		// BAS: You can change assignStatement.getE().getType() to whatever works in your compiler.
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg)
			throws Exception {
		int toPush = intLitExpression.getValue();
		mv.visitLdcInsn(new Integer(toPush));
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		Boolean toPush = booleanLitExpression.getValue();
		if (toPush)
			mv.visitInsn(ICONST_1); // push 1
		else
			mv.visitInsn(ICONST_0); // push 0
		return null;
	}
	
	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		//codegenvisitor will always be in the same dir as PLPRuntimeFrame. So, path shouldn't be an issue
		if(constantExpression.getFirstToken().isKind(KW_SCREENWIDTH))
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", "()I", false);
		else
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", "()I", false);
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec dec = identExpression.getDec();
		if (dec instanceof ParamDec) { // global var
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), dec.getTypeName()
					.getJVMTypeDesc());
		} else { // local var
			int slotNum = dec.getSlotNum();
			mv.visitVarInsn(ILOAD, slotNum);
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg)
			throws Exception {
		// NOTE: Commented out if-else cases for next assignment

		// refer children
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();

		// getfield/iload their values on the stack, left to right evaluation
		e0.visit(this, arg);
		e1.visit(this, arg);

		// get types from decorated children
		TypeName e0Type = e0.getTypeName();
		Token op = binaryExpression.getOp();
		TypeName e1Type = e1.getTypeName();

		if (e0Type.isType(TypeName.INTEGER) && (op.isKind(PLUS) || op.isKind(MINUS)) && e1Type.isType(
				TypeName.INTEGER)) {
			if (op.isKind(PLUS))
				mv.visitInsn(IADD);
			else
				mv.visitInsn(ISUB);
		}
		// else if (e0Type.isType(IMAGE) && (op.isKind(PLUS) || op.isKind(MINUS)) &&
		// e1Type.isType(IMAGE))
		// binaryExpression.setTypeName(IMAGE);
		else if (e0Type.isType(TypeName.INTEGER) && (op.isKind(TIMES) || op.isKind(DIV)) && e1Type
				.isType(TypeName.INTEGER)) {
			if (op.isKind(TIMES))
				mv.visitInsn(IMUL);
			else
				mv.visitInsn(IDIV);
		}
		// else if (e0Type.isType(TypeName.INTEGER) && op.isKind(TIMES) && e1Type.isType(IMAGE))
		// binaryExpression.setTypeName(IMAGE);
		// else if (e0Type.isType(IMAGE) && op.isKind(TIMES) && e1Type.isType(TypeName.INTEGER))
		// binaryExpression.setTypeName(IMAGE);
		else if (e0Type.isType(TypeName.INTEGER) && (op.isKind(LT) || op.isKind(GT) || op.isKind(LE)
				|| op.isKind(GE)) && e1Type.isType(TypeName.INTEGER)) {
			if (op.isKind(LT)) {
				Label ge = new Label();
				mv.visitJumpInsn(IF_ICMPGE, ge);
				mv.visitInsn(ICONST_1);
				Label lt = new Label();
				mv.visitJumpInsn(GOTO, lt);
				mv.visitLabel(ge);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(lt);
			} else if (op.isKind(GT)) {
				Label le = new Label();
				mv.visitJumpInsn(IF_ICMPLE, le);
				mv.visitInsn(ICONST_1);
				Label gt = new Label();
				mv.visitJumpInsn(GOTO, gt);
				mv.visitLabel(le);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(gt);
			} else if (op.isKind(LE)) {
				Label gt = new Label();
				mv.visitJumpInsn(IF_ICMPGT, gt);
				mv.visitInsn(ICONST_1);
				Label le = new Label();
				mv.visitJumpInsn(GOTO, le);
				mv.visitLabel(gt);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(le);
			} else { // GE
				Label lt = new Label();
				mv.visitJumpInsn(IF_ICMPLT, lt);
				mv.visitInsn(ICONST_1);
				Label ge = new Label();
				mv.visitJumpInsn(GOTO, ge);
				mv.visitLabel(lt);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(ge);
			}
		} else if (e0Type.isType(BOOLEAN) && (op.isKind(LT) || op.isKind(GT) || op.isKind(LE) || op
				.isKind(GE)) && e1Type.isType(BOOLEAN)) {
			// BAS: false < true. The rest follow.
			// Since false is const_0 and true is const_1, this code should be *same as
			// the integer based comparison code above*
			if (op.isKind(LT)) {
				Label ge = new Label();
				mv.visitJumpInsn(IF_ICMPGE, ge);
				mv.visitInsn(ICONST_1);
				Label lt = new Label();
				mv.visitJumpInsn(GOTO, lt);
				mv.visitLabel(ge);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(lt);
			} else if (op.isKind(GT)) {
				Label le = new Label();
				mv.visitJumpInsn(IF_ICMPLE, le);
				mv.visitInsn(ICONST_1);
				Label gt = new Label();
				mv.visitJumpInsn(GOTO, gt);
				mv.visitLabel(le);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(gt);
			} else if (op.isKind(LE)) {
				Label gt = new Label();
				mv.visitJumpInsn(IF_ICMPGT, gt);
				mv.visitInsn(ICONST_1);
				Label le = new Label();
				mv.visitJumpInsn(GOTO, le);
				mv.visitLabel(gt);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(le);
			} else { // GE
				Label lt = new Label();
				mv.visitJumpInsn(IF_ICMPLT, lt);
				mv.visitInsn(ICONST_1);
				Label ge = new Label();
				mv.visitJumpInsn(GOTO, ge);
				mv.visitLabel(lt);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(ge);
			}
		} else if ((op.isKind(EQUAL) || op.isKind(NOTEQUAL)) && e0Type.isType(e1Type)) {
			if (op.isKind(EQUAL)) {
				Label ne = new Label();
				mv.visitJumpInsn(IF_ICMPNE, ne);
				mv.visitInsn(ICONST_1);
				Label eq = new Label();
				mv.visitJumpInsn(GOTO, eq);
				mv.visitLabel(ne);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(eq);
			} else { // NE
				mv.visitInsn(IXOR);
			}
		} else
		// else if (e0Type.isType(TypeName.INTEGER) && op.isKind(MOD) &&
		// e1Type.isType(TypeName.INTEGER))
		// binaryExpression.setTypeName(TypeName.INTEGER);
		//
		// else if (e0Type.isType(BOOLEAN) && (op.isKind(AND) || op.isKind(OR)) &&
		// e1Type.isType(BOOLEAN))
		// binaryExpression.setTypeName(BOOLEAN);

			assert false : "not yet implemented";

		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		/*
		 * if (dec instanceof ParamDec) { access with GETFIELD or PUTFIELD } else { access with ILOAD or
		 * ISTORE }
		 */
		Dec dec = identX.getDec();
		if (dec instanceof ParamDec) { // field
			String fieldName = dec.getIdent().getText();
			String fieldType = null;
			TypeName decType = dec.getTypeName();
			if (decType.isType(TypeName.INTEGER)) {
				fieldType = "I";
			} else if (decType.isType(TypeName.BOOLEAN)) {
				fieldType = "Z";
			} else //TODO: add for image
				assert false : "not yet implemented";
			mv.visitVarInsn(ALOAD, 0); // pushing 'this'
			mv.visitInsn(SWAP); // swapping as aload 0 needs to come before the pushed value
			mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
		} else { // local variable
			int slotNum = dec.getSlotNum();
			mv.visitVarInsn(ISTORE, slotNum);
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {

		Expression exp = ifStatement.getE();
		Block bl = ifStatement.getB();

		exp.visit(this, arg); // puts it's value to the top of the stack: Either true or false

		Label notTrue = new Label();
		mv.visitJumpInsn(IFEQ, notTrue); // if top of stack equals 0 i.e. false, skip the block in if
		Label startBlock = new Label();
		mv.visitLabel(startBlock);
		bl.visit(this, arg); // passing startSlotNum (arg) to the the block
		mv.visitLabel(notTrue);

		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {

		Expression exp = whileStatement.getE();
		Block bl = whileStatement.getB();

		Label startWhile = new Label();
		mv.visitJumpInsn(GOTO, startWhile); // for the first time while is encountered
		Label continueWhile = new Label();
		mv.visitLabel(continueWhile);
		bl.visit(this, arg); // visit block
		mv.visitLabel(startWhile);
		exp.visit(this, arg); // loading expression to top of stack
		// checking the expression, jump if still true (not equal to 0)
		mv.visitJumpInsn(IFNE, continueWhile);

		Label endWhile = new Label();
		mv.visitLabel(endWhile);

		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

}
