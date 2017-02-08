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

	void expression() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
	}

	void term() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
	}

	void elem() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
	}

	void program() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
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
		// TODO
		throw new UnimplementedFeatureException();

	}

	void dec() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
	}

	void statement() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
	}

	void chain() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
	}

	void chainElem() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
	}

	void arg() throws SyntaxException {
		// TODO
		throw new UnimplementedFeatureException();
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
			default :
				LinePos lp = t.getLinePos();
				throw new SyntaxException("Illegal token " + t.getText() + " of kind " + t.kind + " at line " + lp.line + " and at pos " + lp.posInLine);
		}
		throw new UnimplementedFeatureException();
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
				throw new SyntaxException("Illegal token " + t.getText() + " of kind " + t.kind + " at line " + lp.line + " and at pos " + lp.posInLine);
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
				throw new SyntaxException("Illegal token " + t.getText() + " of kind " + t.kind + " at line " + lp.line + " and at pos " + lp.posInLine);
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
				throw new SyntaxException("Illegal token " + t.getText() + " of kind " + t.kind + " at line " + lp.line + " and at pos " + lp.posInLine);
			}
		}
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
		throw new SyntaxException("expected EOF");
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
		throw new SyntaxException("Illegal token '" + t.getText() + "' of kind " + t.kind + " at line " + lp.line + " and at pos " + lp.posInLine);
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
	private Token match(Kind... kinds) throws SyntaxException { // For multple terminal states in a production. check all at onces instead of multiple case statements
		// TODO. Optional but handy
		return null; // replace this statement
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