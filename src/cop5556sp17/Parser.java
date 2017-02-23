package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;

import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input. You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner. Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	// TODO: change this.
	// Yes. The parse method is the one that will be called in the actual compiler and it returns the AST (ASTNode).
	// (You could also have it return a Program, but either one works.)
	void parse() throws SyntaxException {
		program();
		matchEOF();
		return;
	}

	void program() throws SyntaxException {
		match(IDENT);
		program_tail();
	}

	void program_tail() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case LBRACE : {
				block();
			}
				break;
			case KW_URL :
			case KW_FILE :
			case KW_INTEGER :
			case KW_BOOLEAN : {
				paramDec();
				while (t.isKind(COMMA)) {
					consume();
					paramDec();
				}
				block();
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	void paramDec() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case KW_URL :
			case KW_FILE :
			case KW_INTEGER :
			case KW_BOOLEAN : {
				consume();
				match(IDENT);
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	Block block() throws SyntaxException {
		match(LBRACE);
		// union (first(dec), first(statement))
		while (t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(Kind.KW_IMAGE) || t.isKind(
				KW_FRAME) || t.isKind(OP_SLEEP) || t.isKind(KW_WHILE) || t.isKind(KW_IF) || t.isKind(IDENT)
				|| t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE) || t.isKind(KW_SHOW) || t
						.isKind(KW_HIDE) || t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC) || t
								.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)) {
			if (t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(Kind.KW_IMAGE) || t.isKind(
					KW_FRAME))
				dec();
			else
				statement();
		}
		match(RBRACE);
		// TODO implement this
		return null;
	}

	void dec() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case KW_INTEGER :
			case KW_BOOLEAN :
			case KW_IMAGE :
			case KW_FRAME : {
				consume();
				match(IDENT);
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	Statement statement() throws SyntaxException {
		Token firstToken = t;
		Kind kind = t.kind;
		switch (kind) {
			case OP_SLEEP : {
				consume();
				Expression e = expression();
				match(SEMI);
				return new SleepStatement(firstToken, e);
			}
			case KW_WHILE : {
				return whileStatement();
			}
			case KW_IF : {
				return ifStatement();
			}
			case IDENT : { // can be either assign or chain, hence use peek()
				Token nextToken = scanner.peek();
				if (nextToken.isKind(ASSIGN)) {
					AssignmentStatement a = assign();
					match(SEMI);
					return a;
				} else { // no need for else if and checking arrow operator. if arrow op, we are fine, else
					Chain c = chain(); // arrow op will throw, which is fine too as there are only 2 options
					match(SEMI);
					return c;
				}
			}
			case OP_BLUR :
			case OP_GRAY :
			case OP_CONVOLVE :
			case KW_SHOW :
			case KW_HIDE :
			case KW_MOVE :
			case KW_XLOC :
			case KW_YLOC :
			case OP_WIDTH :
			case OP_HEIGHT :
			case KW_SCALE : {
				Chain c = chain();
				match(SEMI);
				return c;
			}
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	AssignmentStatement assign() throws SyntaxException {
		Token firstToken = t; // also the Lvalue
		IdentLValue var = new IdentLValue(firstToken);
		match(IDENT);
		match(ASSIGN);
		Expression e = expression();
		return new AssignmentStatement(firstToken, var, e);
	}

	Chain chain() throws SyntaxException {
		Token firstToken = t;
		ChainElem ce0 = chainElem();
		Token op = t;
		arrowOp();
		ChainElem ce1 = chainElem();
		BinaryChain bc = new BinaryChain(firstToken, ce0, op, ce1); // ChainElem (ce0) in parameter abstracted to Chain
		while (t.isKind(ARROW) || t.isKind(BARARROW)) {
			op = t;
			consume();
			ChainElem ce = chainElem();
			bc = new BinaryChain(firstToken, bc, op, ce); // BinaryChain (bc) in parameter abstracted to Chain
		}
		return bc;
	}

	WhileStatement whileStatement() throws SyntaxException {
		Token firstToken = t;
		match(KW_WHILE);
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		return new WhileStatement(firstToken, e, b);
	}

	IfStatement ifStatement() throws SyntaxException {
		Token firstToken = t;
		match(KW_IF);
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		return new IfStatement(firstToken, e, b);
	}

	void arrowOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case ARROW :
			case BARARROW : {
				consume();
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	ChainElem chainElem() throws SyntaxException {
		Token firstToken = t;
		Kind kind = t.kind;
		switch (kind) {
			case IDENT : {
				consume();
				return new IdentChain(firstToken);
			}
			case OP_BLUR :
			case OP_GRAY :
			case OP_CONVOLVE : {
				filterOp();
				Tuple arg = arg();
				return new FilterOpChain(firstToken, arg);
			}
			case KW_SHOW :
			case KW_HIDE :
			case KW_MOVE :
			case KW_XLOC :
			case KW_YLOC : {
				frameOp();
				Tuple arg = arg();
				return new FrameOpChain(firstToken, arg);
			}
			case OP_WIDTH :
			case OP_HEIGHT :
			case KW_SCALE : {
				imageOp();
				Tuple arg = arg();
				return new ImageOpChain(firstToken, arg);
			}
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	void filterOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case OP_BLUR :
			case OP_GRAY :
			case OP_CONVOLVE : {
				consume();
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	void frameOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case KW_SHOW :
			case KW_HIDE :
			case KW_MOVE :
			case KW_XLOC :
			case KW_YLOC : {
				consume();
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	void imageOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case OP_WIDTH :
			case OP_HEIGHT :
			case KW_SCALE : {
				consume();
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	Tuple arg() throws SyntaxException {
		Token firstToken = t;
		Kind kind = t.kind;
		switch (kind) {
			case LPAREN : {
				consume();
				ArrayList<Expression> expArr = new ArrayList<>();
				expArr.add(expression());
				while (t.isKind(COMMA)) {
					consume();
					expArr.add(expression());
				}
				match(RPAREN);
				return new Tuple(firstToken, expArr);
			}
			default : {
				return new Tuple(firstToken, new ArrayList<>()); // return token with empty expression array
			}
		}
	}
	// precedence established in the 4 functions: factor before elem (*) before term(+) before expression(>)
	Expression expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = term();
		Expression e1;
		while (t.isKind(LT) || t.isKind(LE) || t.isKind(GT) || t.isKind(GE) || t.isKind(EQUAL) || t
				.isKind(NOTEQUAL)) {
			Token op = t;
			consume();
			e1 = term();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		return e0;
	}

	Expression term() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = elem();
		Expression e1;
		while (t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR)) {
			Token op = t;
			consume();
			e1 = elem();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		return e0;
	}

	Expression elem() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = factor();
		Expression e1;
		while (t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)) {
			Token op = t;
			consume();
			e1 = factor();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		return e0;
	}

	Expression factor() throws SyntaxException {
		Token firstToken = t;
		Kind kind = t.kind;
		switch (kind) {
			case IDENT : {
				consume();
				return new IdentExpression(firstToken);
			}
			case INT_LIT : {
				consume();
				return new IntLitExpression(firstToken);
			}
			case KW_TRUE :
			case KW_FALSE : {
				consume();
				return new BooleanLitExpression(firstToken);
			}
			case KW_SCREENWIDTH :
			case KW_SCREENHEIGHT : {
				consume();
				return new ConstantExpression(firstToken);
			}
			case LPAREN : {
				consume();
				Expression e = expression();
				match(RPAREN);
				return e;
			}
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	void relOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case LT :
			case LE :
			case GT :
			case GE :
			case EQUAL :
			case NOTEQUAL : {
				consume();
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	void weakOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case PLUS :
			case MINUS :
			case OR : {
				consume();
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	void strongOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case TIMES :
			case DIV :
			case AND :
			case MOD : {
				consume();
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	public Token matchEOFForTest() throws SyntaxException {
		return matchEOF();
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		LinePos lp = t.getLinePos();
		throw new SyntaxException("Expected EOF. Illegal token '" + t.getText() + "' of kind " + t.kind
				+ " at line " + lp.line + " and at pos " + lp.posInLine);
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		LinePos lp = t.getLinePos();
		throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind + " at line "
				+ lp.line + " and at pos " + lp.posInLine);
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
