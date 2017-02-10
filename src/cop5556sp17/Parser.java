package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;

import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;

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

	/**
	 * Useful during development to ensure unimplemented routines are not accidentally called during development. Delete it when the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
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
		// TODO
		// throw new UnimplementedFeatureException();
		// match(IDENT);
		// program_tail();
	}

	void program_tail() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
		// Kind kind = t.kind;
		// switch (kind) {
		// case LBRACE : {
		// block();
		// }
		// break;
		// case KW_URL :
		// case KW_FILE :
		// case KW_INTEGER :
		// case KW_BOOLEAN : {
		// paramDec();
		// while (t.isKind(COMMA)) {
		// consume();
		// paramDec();
		// }
		// block();
		// }
		// break;
		// default : {
		// LinePos lp = t.getLinePos();
		// throw new SyntaxException("Illegal token " + t.getText() + " of kind " + t.kind + " at line " + lp.line + " and at pos " + lp.posInLine);
		// }
		// }
	}

	void paramDec() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
		// Kind kind = t.kind;
		// switch (kind) {
		// case KW_URL :
		// case KW_FILE :
		// case KW_INTEGER :
		// case KW_BOOLEAN : {
		// consume();
		// match(IDENT);
		// }
		// break;
		// default : {
		// LinePos lp = t.getLinePos();
		// throw new SyntaxException("Illegal token " + t.getText() + " of kind " + t.kind + " at line " + lp.line + " and at pos " + lp.posInLine);
		// }
		// }
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
				} else { // no need for else if and check arrow operator. if arrow op, we are fine, else arrow op will throw, which is fine too as there are only 2 options
					chain();
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

	void arg() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case ARROW :
			case BARARROW :
			case SEMI : {
				// NOP
			}
				break;
			case LPAREN : {
				consume();
				expression();
				while (t.isKind(COMMA)) {
					consume();
					expression();
				}
				match(RPAREN);
			}
				break;
			default : {
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind
						+ " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
	}

	void expression() throws SyntaxException {
		term();
		while (t.isKind(LT) || t.isKind(LE) || t.isKind(GT) || t.isKind(GE) || t.isKind(EQUAL) || t
				.isKind(NOTEQUAL)) {
			consume();
			term();
		}
	}

	void term() throws SyntaxException {
		elem();
		while (t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR)) {
			consume();
			elem();
		}
	}

	void elem() throws SyntaxException {
		factor();
		while (t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)) {
			consume();
			factor();
		}
	}

	void factor() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
			case IDENT :
			case INT_LIT :
			case KW_TRUE :
			case KW_FALSE :
			case KW_SCREENWIDTH :
			case KW_SCREENHEIGHT : {
				consume();
			}
				break;
			case LPAREN : {
				consume();
				expression();
				match(RPAREN);
			}
				break;
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

	/**
	 * Checks whether the current token is the EOF token. If not, a SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	// TODO: change back to private?
	public Token matchEOF() throws SyntaxException {
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
	 * Checks if the current token has one of the given kinds. If so, the current token is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *          list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	// TODO: Use this?
	// private Token match(Kind... kinds) throws SyntaxException { // For multple terminal states in a production. check all at onces instead of multiple case statements
	// // TODO. Optional but handy
	// return null; // replace this statement
	// }

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
