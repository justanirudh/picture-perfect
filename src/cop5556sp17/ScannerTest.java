package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Scanner.*;

public class ScannerTest {
	// TODO: Also write 'exception' based tests: Token.IntValue

	private static void isValidToken(Token t, Kind expKind, int expPos, String expText, int expLen) {
		assertEquals(expKind, t.kind);
		assertEquals(expPos, t.pos);
		assertEquals(expLen, t.length);
		assertEquals(expText, t.getText());
	}

	private static void isValidLinePos(LinePos lp, int expLine, int expPosInLine) {
		assertEquals(expLine, lp.line);
		assertEquals(expPosInLine, lp.posInLine);
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEmpty() throws IllegalCharException, IllegalNumberException {
		String input = "System";
		Scanner scanner = new Scanner(input);
		scanner.scan();
	}

	@Test
	public void testSemiConcat() throws IllegalCharException, IllegalNumberException {
		// input string
		String input = ";;;";
		// create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		// get the first token and check its kind, position, and contents (length and text)
		Token token = scanner.nextToken();
		isValidToken(token, SEMI, 0, SEMI.getText(), SEMI.getText().length());

		// get the next token and check its kind, position, and contents
		Scanner.Token token1 = scanner.nextToken();
		isValidToken(token1, SEMI, 1, SEMI.getText(), SEMI.getText().length());

		Scanner.Token token2 = scanner.nextToken();
		isValidToken(token2, SEMI, 2, SEMI.getText(), SEMI.getText().length());

		// check that the scanner has inserted an EOF token at the end
		Scanner.Token token3 = scanner.nextToken();
		isValidToken(token3, EOF, 3, "", 0);
	}

	@Test
	public void testSeparators() throws IllegalCharException, IllegalNumberException {
		// input string
		String input = ")}(,{";
		// create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		// get the first token and check its kind, position, and contents (length and text)
		Scanner.Token token = scanner.nextToken();
		isValidToken(token, RPAREN, 0, RPAREN.getText(), RPAREN.getText().length());

		Scanner.Token token1 = scanner.nextToken();
		isValidToken(token1, RBRACE, 1, RBRACE.getText(), RBRACE.getText().length());

		Scanner.Token token2 = scanner.nextToken();
		isValidToken(token2, LPAREN, 2, LPAREN.getText(), LPAREN.getText().length());

		Scanner.Token token3 = scanner.nextToken();
		isValidToken(token3, COMMA, 3, COMMA.getText(), COMMA.getText().length());

		Scanner.Token token4 = scanner.nextToken();
		isValidToken(token4, LBRACE, 4, LBRACE.getText(), LBRACE.getText().length());

		Scanner.Token token5 = scanner.nextToken();
		isValidToken(token5, EOF, 5, "", 0);
	}

	/**
	 * This test illustrates how to check that the Scanner detects errors properly. In this test, the input contains an int literal with a value that
	 * exceeds the range of an int. The scanner should detect this and throw and IllegalNumberException.
	 * 
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	@Test
	public void testIntOverflowError() throws IllegalCharException, IllegalNumberException {
		String input = "99999999999999999";
		Scanner scanner = new Scanner(input);
		thrown.expect(IllegalNumberException.class);
		scanner.scan();
	}

	@Test
	public void testEOF() throws IllegalCharException, IllegalNumberException {
		String input = "";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(1, scanner.tokens.size()); // only EOF
		Scanner.Token token = scanner.nextToken();
		assertEquals(EOF, token.kind);
		assertEquals(0, token.pos);
		assertEquals(0, token.length);
		assertEquals("", token.getText());
	}

	@Test
	public void testWhiteSpacesAndEOF() throws IllegalCharException, IllegalNumberException {
		// test: tokens array is empty, lineStarts has 2 entries
		String input = "       \n     \t    \r";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(1, scanner.tokens.size()); // only EOF
		Scanner.Token token = scanner.nextToken();
		assertEquals(EOF, token.kind);
		assertEquals(19, token.pos);
		assertEquals(0, token.length);
		assertEquals("", token.getText());
	}

	@Test
	public void testNewLine() throws IllegalCharException, IllegalNumberException {
		// test: tokens array is empty, lineStarts has 2 entries
		String input = "   \n  \n   \n";
		// create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(3, scanner.newLines.get(0).intValue());
		assertEquals(6, scanner.newLines.get(1).intValue());
		assertEquals(10, scanner.newLines.get(2).intValue());
	}

	@Test
	public void testWhiteSpacesWithOperators() throws IllegalCharException, IllegalNumberException {
		// test: tokens array is empty, lineStarts has 2 entries
		String input = "  \n  \r* + & % + \n";
		// create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(6, scanner.tokens.size());
		// get the first token and check its kind, position, and contents (length and text)
		Scanner.Token token = scanner.nextToken();
		isValidToken(token, TIMES, 6, "*", 1);

		Scanner.Token token1 = scanner.nextToken();
		isValidToken(token1, PLUS, 8, "+", 1);

		Scanner.Token token2 = scanner.nextToken();
		isValidToken(token2, AND, 10, "&", 1);

		Scanner.Token token3 = scanner.nextToken();
		isValidToken(token3, MOD, 12, "%", 1);

		Scanner.Token token4 = scanner.nextToken();
		isValidToken(token4, PLUS, 14, "+", 1);
	}

	@Test
	public void testTokenGetLinePos() throws IllegalCharException, IllegalNumberException {
		// test: tokens array is empty, lineStarts has 2 entries
		String input = "+ % \n*   &\n +\r\n %";
		// create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(7, scanner.tokens.size());
		Scanner.Token token0 = scanner.nextToken();
		isValidLinePos(token0.getLinePos(), 0, 0);

		Scanner.Token token1 = scanner.nextToken();
		isValidLinePos(token1.getLinePos(), 0, 2);

		Scanner.Token token2 = scanner.nextToken();
		isValidLinePos(token2.getLinePos(), 1, 0);

		Scanner.Token token3 = scanner.nextToken();
		isValidLinePos(token3.getLinePos(), 1, 4);

		Scanner.Token token4 = scanner.nextToken();
		isValidLinePos(token4.getLinePos(), 2, 1);

		Scanner.Token token5 = scanner.nextToken();
		isValidLinePos(token5.getLinePos(), 3, 1);
	}

	@Test
	public void testIdentifiers() throws IllegalCharException, IllegalNumberException {
		// test: tokens array is empty, lineStarts has 2 entries
		String input = "K AA Bb C$ D_ E0 g aA bb c$ d_ e9 $ $Z $f $$ $_ $7 _ _D _s _$$ ___ _65";
		// create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		assertEquals(25, scanner.tokens.size());
		Scanner.Token token = scanner.nextToken();
		isValidToken(token, IDENT, 0, "K", 1);

		Scanner.Token token1 = scanner.nextToken();
		isValidToken(token1, IDENT, 2, "AA", 2);

		Scanner.Token token2 = scanner.nextToken();
		isValidToken(token2, IDENT, 5, "Bb", 2);

		Scanner.Token token3 = scanner.nextToken();
		isValidToken(token3, IDENT, 8, "C$", 2);

		Scanner.Token token4 = scanner.nextToken();
		isValidToken(token4, IDENT, 11, "D_", 2);
		
		Scanner.Token token5 = scanner.nextToken();
		isValidToken(token5, IDENT, 14, "E0", 2);

		Scanner.Token token6 = scanner.nextToken();
		isValidToken(token6, IDENT, 17, "g", 1);

		Scanner.Token token7 = scanner.nextToken();
		isValidToken(token7, IDENT, 19, "aA", 2);

		Scanner.Token token8 = scanner.nextToken();
		isValidToken(token8, IDENT, 22, "bb", 2);

		Scanner.Token token9 = scanner.nextToken();
		isValidToken(token9, IDENT, 25, "c$", 2); 
		
		Scanner.Token token10 = scanner.nextToken();
		isValidToken(token10, IDENT, 28, "d_", 2);

		Scanner.Token token11 = scanner.nextToken();
		isValidToken(token11, IDENT, 31, "e9", 2);

		Scanner.Token token12 = scanner.nextToken();
		isValidToken(token12, IDENT, 34, "$", 1);

		Scanner.Token token13 = scanner.nextToken();
		isValidToken(token13, IDENT, 36, "$Z", 2);

		Scanner.Token token14 = scanner.nextToken();
		isValidToken(token14, IDENT, 39, "$f", 2);
		
		Scanner.Token token15 = scanner.nextToken();
		isValidToken(token15, IDENT, 42, "$$", 2);

		Scanner.Token token16 = scanner.nextToken();
		isValidToken(token16, IDENT, 45, "$_", 2);

		Scanner.Token token17 = scanner.nextToken();
		isValidToken(token17, IDENT, 48, "$7", 2);

		Scanner.Token token18 = scanner.nextToken();
		isValidToken(token18, IDENT, 51, "_", 1);

		Scanner.Token token19 = scanner.nextToken();
		isValidToken(token19, IDENT, 53, "_D", 2);
		
		Scanner.Token token20 = scanner.nextToken();
		isValidToken(token20, IDENT, 56, "_s", 2);

		Scanner.Token token21 = scanner.nextToken();
		isValidToken(token21, IDENT, 59, "_$$", 3);

		Scanner.Token token22 = scanner.nextToken();
		isValidToken(token22, IDENT, 63, "___", 3);

		Scanner.Token token23 = scanner.nextToken();
		isValidToken(token23, IDENT, 67, "_65", 3);
	}
	
	@Test
	public void testZero() throws IllegalCharException, IllegalNumberException {
		String input = "0 0";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(3, scanner.tokens.size());

		Scanner.Token token0 = scanner.nextToken();
		isValidToken(token0, INT_LIT, 0, "0", 1);

		Scanner.Token token1 = scanner.nextToken();
		isValidToken(token1, INT_LIT, 2, "0", 1);

		Scanner.Token token2 = scanner.nextToken();
		isValidToken(token2, EOF, 3, "", 0);

		String input2 = "00";
		Scanner scanner2 = new Scanner(input2);
		scanner2.scan();
		assertEquals(3, scanner2.tokens.size());

		Scanner.Token token10 = scanner2.nextToken();
		isValidToken(token10, INT_LIT, 0, "0", 1);

		Scanner.Token token11 = scanner2.nextToken();
		isValidToken(token11, INT_LIT, 1, "0", 1);

		Scanner.Token token12 = scanner2.nextToken();
		isValidToken(token12, EOF, 2, "", 0);
		
	}

	@Test
	public void testIdentiferWithZero() throws IllegalCharException, IllegalNumberException {
		String input = "0 0af 0 af 0"; //0af would be 2 tokens as an ident cannot start with 0
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		assertEquals(7, scanner.tokens.size());
		
		Scanner.Token token = scanner.nextToken();
		isValidToken(token, INT_LIT, 0, "0", 1);
		
		Scanner.Token token0 = scanner.nextToken();
		isValidToken(token0, INT_LIT, 2, "0", 1);
		
		Scanner.Token token1 = scanner.nextToken();
		isValidToken(token1, IDENT, 3, "af", 2);

		Scanner.Token token2 = scanner.nextToken();
		isValidToken(token2, INT_LIT, 6, "0", 1);

		Scanner.Token token3 = scanner.nextToken();
		isValidToken(token3, IDENT, 8, "af", 2);

		Scanner.Token token4 = scanner.nextToken();
		isValidToken(token4, INT_LIT, 11, "0", 1);
	}

	@Test
	public void testDigits() throws IllegalCharException, IllegalNumberException {
		
		String input = "31 40 5 6 09 0 6 0"; //09 would be 2 tokens as an ident cannot start with 0
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		assertEquals(10, scanner.tokens.size());
		
		Scanner.Token token = scanner.nextToken();
		isValidToken(token, INT_LIT, 0, "31", 2);
		
		Scanner.Token token0 = scanner.nextToken();
		isValidToken(token0, INT_LIT, 3, "40", 2);
		
		Scanner.Token token1 = scanner.nextToken();
		isValidToken(token1, INT_LIT, 6, "5", 1);

		Scanner.Token token2 = scanner.nextToken();
		isValidToken(token2, INT_LIT, 8, "6", 1);

		Scanner.Token token3 = scanner.nextToken();
		isValidToken(token3, INT_LIT, 10, "0", 1);

		Scanner.Token token4 = scanner.nextToken();
		isValidToken(token4, INT_LIT, 11, "9", 1);
		
		Scanner.Token token5 = scanner.nextToken();
		isValidToken(token5, INT_LIT, 13, "0", 1);
		
		Scanner.Token token6 = scanner.nextToken();
		isValidToken(token6, INT_LIT, 15, "6", 1);
		
		Scanner.Token token7 = scanner.nextToken();
		isValidToken(token7, INT_LIT, 17, "0", 1);

		Scanner.Token token8 = scanner.nextToken();
		isValidToken(token8, EOF, 18, "", 0);
	}
	
	@Test
	public void testIdentifierDigitZeroSepOpLinePos() throws IllegalCharException, IllegalNumberException {
		
		String input = "_b$c 80 \n 0 ; \n , "; //09 would be 2 tokens as an ident cannot start with 0
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		assertEquals(6, scanner.tokens.size());
		
		Scanner.Token token = scanner.nextToken();
		isValidToken(token, IDENT, 0, "_b$c", 4);
		isValidLinePos(token.getLinePos(), 0, 0);
		
		Scanner.Token token0 = scanner.nextToken();
		isValidToken(token0, INT_LIT, 5, "80", 2);
		isValidLinePos(token0.getLinePos(), 0, 5);
		
		Scanner.Token token1 = scanner.nextToken();
		isValidToken(token1, INT_LIT, 10, "0", 1);
		isValidLinePos(token1.getLinePos(), 1, 1);
		
		Scanner.Token token2 = scanner.nextToken();
		isValidToken(token2, SEMI, 12, ";", 1);
		isValidLinePos(token2.getLinePos(), 1, 3);
		
		Scanner.Token token3 = scanner.nextToken();
		isValidToken(token3, COMMA, 16, ",", 1);
		isValidLinePos(token3.getLinePos(), 2, 1);
		
		Scanner.Token token4 = scanner.nextToken();
		isValidToken(token4, EOF, 18, "", 0);
		isValidLinePos(token4.getLinePos(), 2, 3);
	}
	
	@Test
	public void testReservedWords() throws IllegalCharException, IllegalNumberException {
		
		String input = "if abc true falsely xloc de blur gray fileframe amoveg abif";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		assertEquals(12, scanner.tokens.size());
		
		Scanner.Token token = scanner.nextToken();
		isValidToken(token, KW_IF, 0, token.kind.getText(), token.kind.getText().length());
		
		Scanner.Token token0 = scanner.nextToken();
		isValidToken(token0, IDENT, 3, "abc", 3);
		
		Scanner.Token token1 = scanner.nextToken();
		isValidToken(token1, KW_TRUE, 7, token1.kind.getText(), token1.kind.getText().length());

		Scanner.Token token2 = scanner.nextToken();
		isValidToken(token2, IDENT, 12, "falsely", 7);

		Scanner.Token token3 = scanner.nextToken();
		isValidToken(token3, KW_XLOC, 20, token3.kind.getText(), token3.kind.getText().length());

		Scanner.Token token4 = scanner.nextToken();
		isValidToken(token4, IDENT, 25, "de", 2);
		
		Scanner.Token token5 = scanner.nextToken();
		isValidToken(token5, OP_BLUR, 28, token5.kind.getText(), token5.kind.getText().length());
		
		Scanner.Token token6 = scanner.nextToken();
		isValidToken(token6, OP_GRAY, 33, token6.kind.getText(), token6.kind.getText().length());
		
		Scanner.Token token7 = scanner.nextToken();
		isValidToken(token7, IDENT, 38, "fileframe", 9);
		
		Scanner.Token token17 = scanner.nextToken();
		isValidToken(token17, IDENT, 48, "amoveg", 6);
		
		Scanner.Token token27 = scanner.nextToken();
		isValidToken(token27, IDENT, 55, "abif", 4);

		Scanner.Token token8 = scanner.nextToken();
		isValidToken(token8, EOF, 59, "", 0);
	}

	
	// TODO more tests

}
