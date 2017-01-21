package cop5556sp17;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Scanner {
	/**
	 * Kind enum
	 */
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), KW_WHILE("while"), KW_IF("if"), KW_TRUE(
				"true"), KW_FALSE("false"), SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), EQUAL("=="), NOTEQUAL("!="), LT(
						"<"), GT(">"), LE("<="), GE(">="), PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE(
								"convolve"), KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), KW_HIDE("hide"), KW_SHOW(
										"show"), KW_MOVE("move"), OP_SLEEP("sleep"), KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}

	public static enum State {
		START, AFTER_DIV, AFTER_DIV_AST, IN_IDENT, IN_DIGIT, AFTER_BAR, AFTER_BAR_MINUS, AFTER_BANG, AFTER_LT, AFTER_GT, AFTER_EQ, AFTER_MINUS
	}

	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message) {
			super(message);
		}
	}

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}

	public class Token {
		public final Kind kind;
		public final int pos; // position in input array
		public final int length;

		// returns the text of this Token
		public String getText() {
			// TODO IMPLEMENT THIS: return actual text
			return chars.substring(pos, pos + length);
		}

		// returns a LinePos object representing the line and column of this
		// Token
		/*
		 * To Search an element of Java ArrayList using binary search algorithm use, static int binarySearch(List list, Object element) method of Collections class. This method returns the index of the
		 * value to be searched, if found in the ArrayList. Otherwise it returns (- (X) - 1) where X is the index where the the search value would be inserted. i.e. index of first element that is grater
		 * than the search value or ArrayList.size()
		 */
		LinePos getLinePos() {
			// TODO IMPLEMENT THIS
			int ret = Collections.binarySearch(newLines, pos); // ret should never be positive as a token can't have the same position as a newline
			int line = -1 * (ret + 1);
			int posInLine;
			if (line == 0)
				posInLine = pos;
			else
				posInLine = pos - newLines.get(line - 1) - 1;
			return new LinePos(line, posInLine);
		}

		/**
		 * Precondition: kind = Kind.INT_LIT, the text can be represented with a Java int. Note that the validity of the input should have been checked when the Token was created. So the exception should
		 * never be thrown.
		 * 
		 * @return int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException {
			// TODO IMPLEMENT THIS
			return Integer.parseInt(getText());
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

	}

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;
	ArrayList<Integer> newLines; // record the positions of newline characters
	final HashMap<String, Kind> reservedWords;

	Scanner(String chars) { // PRIMARY Constructor
		this.chars = chars;
		tokenNum = 0;
		tokens = new ArrayList<Token>();
		newLines = new ArrayList<>();
		reservedWords = populateReservedWords();
	}

	private int skipWhiteSpaces(int pos) {
		while (pos < chars.length() && Character.isWhitespace(chars.charAt(pos))) {
			if (chars.charAt(pos) == '\n')
				newLines.add(pos);
			++pos;
		}
		return pos;
	}

	private HashMap<String, Kind> populateReservedWords() {
		HashMap<String, Kind> map = new HashMap<>();
		map.put("integer", Kind.KW_INTEGER);
		map.put("boolean", Kind.KW_BOOLEAN);
		map.put("image", Kind.KW_IMAGE);
		map.put("url", Kind.KW_URL);
		map.put("file", Kind.KW_FILE);
		map.put("frame", Kind.KW_FRAME);
		map.put("while", Kind.KW_WHILE);
		map.put("if", Kind.KW_IF);
		map.put("true", Kind.KW_TRUE);
		map.put("false", Kind.KW_FALSE);
		map.put("blur", Kind.OP_BLUR);
		map.put("gray", Kind.OP_GRAY);
		map.put("convolve", Kind.OP_CONVOLVE);
		map.put("screenheight", Kind.KW_SCREENHEIGHT);
		map.put("screenwidth", Kind.KW_SCREENWIDTH);
		map.put("width", Kind.OP_WIDTH);
		map.put("height", Kind.OP_HEIGHT);
		map.put("xloc", Kind.KW_XLOC);
		map.put("yloc", Kind.KW_YLOC);
		map.put("hide", Kind.KW_HIDE);
		map.put("show", Kind.KW_SHOW);
		map.put("move", Kind.KW_MOVE);
		map.put("sleep", Kind.OP_SLEEP);
		map.put("scale", Kind.KW_SCALE);
		return map;
	}

	private boolean isReservedWord(String text) {
		return reservedWords.containsKey(text);
	}

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		// TODO IMPLEMENT THIS!!!!

		int pos = 0;
		int length = chars.length();
		State state = State.START;
		int startPos = 0; // both lines and pos start from 0
		int ch;
		while (pos <= length) { // == also because catching the EOF
			ch = pos < length ? chars.charAt(pos) : -1; // -1 will be handled by EOF
			switch (state) {

				case START : { // IMP: All states except start will need to change there state to START after getting done with their business
					pos = skipWhiteSpaces(pos);
					ch = pos < length ? chars.charAt(pos) : -1; // checking again as skipWhiteSpaces might have changed the pos again
					startPos = pos;
					switch (ch) {
						case -1 : {
							tokens.add(new Token(Kind.EOF, pos, 0));
							pos++; // is probably not required as EOF will be last char. But still no harm as the while will catch
						}
							break;
						case '&' : {
							tokens.add(new Token(Kind.AND, startPos, 1));
							pos++;
						}
							break;
						case '+' : {
							tokens.add(new Token(Kind.PLUS, startPos, 1));
							pos++;
						}
							break;
						case '*' : {
							tokens.add(new Token(Kind.TIMES, startPos, 1));
							pos++;
						}
							break;
						case '%' : {
							tokens.add(new Token(Kind.MOD, startPos, 1));
							pos++;
						}
							break;
						case ';' : {
							tokens.add(new Token(Kind.SEMI, startPos, 1));
							pos++;
						}
							break;
						case ',' : {
							tokens.add(new Token(Kind.COMMA, startPos, 1));
							pos++;
						}
							break;
						case '(' : {
							tokens.add(new Token(Kind.LPAREN, startPos, 1));
							pos++;
						}
							break;
						case ')' : {
							tokens.add(new Token(Kind.RPAREN, startPos, 1));
							pos++;
						}
							break;
						case '{' : {
							tokens.add(new Token(Kind.LBRACE, startPos, 1));
							pos++;
						}
							break;
						case '}' : {
							tokens.add(new Token(Kind.RBRACE, startPos, 1));
							pos++;
						}
							break;
						case '0' : {
							tokens.add(new Token(Kind.INT_LIT, startPos, 1));
							pos++;
						}
							break;
						case '!' : {
							pos++;
							state = State.AFTER_BANG;
						}
							break;
						// case '=': {state = State.AFTER_EQ;pos++;}break;
						default : { // this should come after the '0' state else isDigit would catch that
							if (Character.isDigit(ch)) {
								state = State.IN_DIGIT;
								pos++;
							} else if (Character.isJavaIdentifierStart(ch)) {
								state = State.IN_IDENT;
								pos++;
							} else {
								throw new IllegalCharException("Illegal character " + ch + " at position " + pos);
							}
						}
					} // switch (ch)
				}
					break;
				case IN_DIGIT : {
					if (Character.isDigit(ch)) { // keep incrementing pos until we get a non-digit
						pos++;
					} else {
						Token potentialToken = new Token(Kind.INT_LIT, startPos, pos - startPos);
						try {
							Integer.parseInt(potentialToken.getText()); // at this point, test is definitely a number. Hence, numformatExp will only be thrown if it is to big
						} catch (NumberFormatException n) {
							throw new IllegalNumberException(potentialToken.getText() + " at position " + startPos + " is too big. Cannot be represented as a Java Integer.");
						}
						tokens.add(potentialToken);
						state = State.START;
					}
				}
					break;
				case IN_IDENT : {
					if (Character.isJavaIdentifierPart(ch)) { // keep incrementing pos until we get a non-javaidentifierpart
						pos++;
					} else {
						Token t = new Token(Kind.IDENT, startPos, pos - startPos);
						if (isReservedWord(t.getText()))
							tokens.add(new Token(reservedWords.get(t.getText()), startPos, pos - startPos));
						else
							tokens.add(t);
						state = State.START;
					}
				}
					break;
				case AFTER_EQ : {
				}
					break;
				case AFTER_DIV : {
				}
					break;
				case AFTER_DIV_AST : {
				}
					break;
				case AFTER_BAR : {
				}
					break;
				case AFTER_BAR_MINUS : {
				}
					break;
				case AFTER_BANG : {
					if(ch == '='){ //increment pos, accept != and reset State to START
						pos++;
						tokens.add(new Token(Kind.NOTEQUAL, startPos, pos - startPos));
					}
					else{ //dont increment pos, accept ! and reset state to Start
						tokens.add(new Token(Kind.NOT, startPos, pos - startPos));
					}
					state = State.START;
				}
					break;
				case AFTER_LT : {
				}
					break;
				case AFTER_GT : {
				}
					break;
				case AFTER_MINUS : {
				}
					break;
				default :
					assert false : state;
			}// switch(state)
		} // while
		return this;
	}

	/*
	 * Return the next token in the token list and update the state so that the next call will return the Token..
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state. (So the following call to next will return the same token.)
	 */
	public Token peek() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum + 1);
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the given token.
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		// TODO IMPLEMENT THIS
		return t.getLinePos();
	}

}
