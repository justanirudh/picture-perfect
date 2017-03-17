package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	private String getFirstTokenInfo(Token firstToken) {
		LinePos lp = firstToken.getLinePos();
		return "Location: Starts with '" + firstToken.getText() + "' at line number " + lp.line
				+ " and pos number " + lp.posInLine;
	}

	private void throwNonMatchingTypeException(Token firstToken, TypeName expected, TypeName obtained)
			throws TypeCheckException {
		throw new TypeCheckException("Expected type: " + expected + ", Found type: " + obtained + ";"
				+ getFirstTokenInfo(firstToken));
	}

	private void throwUndeclaredVariableException(Token firstToken) throws TypeCheckException {
		throw new TypeCheckException(getFirstTokenInfo(firstToken)
				+ " has not been declared for the current scope");
	}

	// Doing a *post-order* traversal

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO: in grammar, ident.type a thing or just a temp variable of representing dec's type?
		Token identToken = identChain.firstToken;
		Dec dec = symtab.lookup(identToken.getText());
		if (dec == null)
			throwUndeclaredVariableException(identToken);
		identChain.setTypeName(dec.getTypeName());
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// refer to children
		Tuple tup = filterOpChain.getArg();

		// decorate children
		tup.visit(this, arg);

		// decorate current node
		if (!tup.getExprList().isEmpty())
			throw new TypeCheckException("Expression list for FilterOp Chain is not empty. "
					+ getFirstTokenInfo(tup.getFirstToken()));
		filterOpChain.setTypeName(IMAGE);

		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {

		Tuple tup = frameOpChain.getArg();

		tup.visit(this, arg);

		List<Expression> expList = tup.getExprList();

		if (frameOpChain.isKind(KW_SHOW) || frameOpChain.isKind(KW_HIDE)) {
			if (!expList.isEmpty())
				throw new TypeCheckException("Expression list for FrameOp Chain is not empty. "
						+ getFirstTokenInfo(tup.getFirstToken()));
			frameOpChain.setTypeName(NONE);
		} else if (frameOpChain.isKind(Kind.KW_XLOC) || frameOpChain.isKind(KW_YLOC)) {
			if (!expList.isEmpty())
				throw new TypeCheckException("Expression list for FrameOp Chain is not empty. "
						+ getFirstTokenInfo(tup.getFirstToken()));
			frameOpChain.setTypeName(INTEGER);
		} else { // KW_MOVE
			if (expList.size() != 2)
				throw new TypeCheckException("Expression list for FrameOp Chain does not have size two. "
						+ getFirstTokenInfo(tup.getFirstToken()));
			frameOpChain.setTypeName(NONE);
		}

		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Token identToken = identExpression.firstToken;
		Dec dec = symtab.lookup(identToken.getText());
		if (dec == null)
			throwUndeclaredVariableException(identToken);
		identExpression.setTypeName(dec.getTypeName());
		identExpression.setDec(dec);
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg)
			throws Exception {
		intLitExpression.setTypeName(INTEGER);
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		booleanLitExpression.setTypeName(TypeName.BOOLEAN);
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setTypeName(INTEGER);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg)
			throws Exception {

		// refer to children
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();

		// decorate children
		e0.visit(this, arg);
		e1.visit(this, arg);

		// get values
		TypeName e0Type = e0.getTypeName();
		Token op = binaryExpression.getOp();
		TypeName e1Type = e1.getTypeName();

		// decorate current node
		if (e0Type.isType(INTEGER) && (op.isKind(PLUS) || op.isKind(MINUS)) && e1Type.isType(INTEGER))
			binaryExpression.setTypeName(INTEGER);
		else if (e0Type.isType(IMAGE) && (op.isKind(PLUS) || op.isKind(MINUS)) && e1Type.isType(IMAGE))
			binaryExpression.setTypeName(IMAGE);
		else if (e0Type.isType(INTEGER) && (op.isKind(TIMES) || op.isKind(DIV)) && e1Type.isType(
				INTEGER))
			binaryExpression.setTypeName(INTEGER);
		else if (e0Type.isType(INTEGER) && op.isKind(TIMES) && e1Type.isType(IMAGE))
			binaryExpression.setTypeName(IMAGE);
		else if (e0Type.isType(IMAGE) && op.isKind(TIMES) && e1Type.isType(INTEGER))
			binaryExpression.setTypeName(IMAGE);
		else if (e0Type.isType(INTEGER) && (op.isKind(LT) || op.isKind(GT) || op.isKind(LE) || op
				.isKind(GE)) && e1Type.isType(INTEGER))
			binaryExpression.setTypeName(BOOLEAN);
		else if (e0Type.isType(BOOLEAN) && (op.isKind(LT) || op.isKind(GT) || op.isKind(LE) || op
				.isKind(GE)) && e1Type.isType(BOOLEAN))
			binaryExpression.setTypeName(BOOLEAN);
		else if ((op.isKind(EQUAL) || op.isKind(NOTEQUAL)) && e0Type.isType(e1Type))
			binaryExpression.setTypeName(BOOLEAN);
		else
			throw new TypeCheckException("Incompatible types for Binary Expression." + getFirstTokenInfo(
					binaryExpression.getFirstToken()));
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression exp : tuple.getExprList()) {
			exp.visit(this, arg); // visit child first
			TypeName expType = exp.getTypeName();
			if (!expType.isType(INTEGER))
				throwNonMatchingTypeException(exp.getFirstToken(), INTEGER, expType);
		}
		return null;
	}

}
