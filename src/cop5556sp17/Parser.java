package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;

import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.Tuple;

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

	void block() throws SyntaxException {
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

	void statement() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case OP_SLEEP : {
				consume();
				expression();
				match(SEMI);
			}
				break;
			case KW_WHILE : {
				whileStatement();
			}
				break;
			case KW_IF : {
				ifStatement();
			}
				break;
			case IDENT : { // can be either assign or chain, hence use peek()
				Token nextToken = scanner.peek();
				if (nextToken.isKind(ASSIGN)) {
					assign();
					match(SEMI);
				} else { // no need for else if and checking arrow operator. if arrow op, we are fine, else
					chain(); // arrow op will throw, which is fine too as there are only 2 options
					match(SEMI);
				}
			}
				break;
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
				chain();
				match(SEMI);
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	void assign() throws SyntaxException {
		match(IDENT);
		match(ASSIGN);
		expression();
	}

	void chain() throws SyntaxException {
		chainElem();
		arrowOp();
		chainElem();
		while (t.isKind(ARROW) || t.isKind(BARARROW)) {
			consume();
			chainElem();
		}
	}

	void whileStatement() throws SyntaxException {
		match(KW_WHILE);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

	void ifStatement() throws SyntaxException {
		match(KW_IF);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
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

	void chainElem() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case IDENT : {
				consume();
			}
				break;
			case OP_BLUR :
			case OP_GRAY :
			case OP_CONVOLVE : {
				filterOp();
				arg();
			}
				break;
			case KW_SHOW :
			case KW_HIDE :
			case KW_MOVE :
			case KW_XLOC :
			case KW_YLOC : {
				frameOp();
				arg();
			}
				break;
			case OP_WIDTH :
			case OP_HEIGHT :
			case KW_SCALE : {
				imageOp();
				arg();
			}
				break;
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
				return null;
			}
		}
	}
//precedence established in the 4 functions: factor before elem (*) before term(+) before expression(>)
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
