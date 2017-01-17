package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.*;

public class ScannerTest {

	private static void isValidSimpleToken(Token t, Kind k, int pos) { // Simple meaning: kind text = token text
		assertEquals(k, t.kind);
		assertEquals(pos, t.pos);
		String text = k.getText();
		// TODO: will this be valid for identifiers etc as well?
		assertEquals(text.length(), t.length);
		assertEquals(text, t.getText());
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEmpty() throws IllegalCharException, IllegalNumberException {
		String input = "System";
		Scanner scanner = new Scanner(input);
		scanner.scan();
	}

	// @Test
	// public void testSemiConcat() throws IllegalCharException, IllegalNumberException {
	// //input string
	// String input = ";;;";
	// //create and initialize the scanner
	// Scanner scanner = new Scanner(input);
	// scanner.scan();
	// //get the first token and check its kind, position, and contents (length and text)
	// Scanner.Token token = scanner.nextToken();
	// assertEquals(SEMI, token.kind);
	// assertEquals(0, token.pos);
	// String text = SEMI.getText();
	// assertEquals(text.length(), token.length);
	// assertEquals(text, token.getText());
	// //get the next token and check its kind, position, and contents
	// Scanner.Token token1 = scanner.nextToken();
	// assertEquals(SEMI, token1.kind);
	// assertEquals(1, token1.pos);
	// assertEquals(text.length(), token1.length);
	// assertEquals(text, token1.getText());
	// Scanner.Token token2 = scanner.nextToken();
	// assertEquals(SEMI, token2.kind);
	// assertEquals(2, token2.pos);
	// assertEquals(text.length(), token2.length);
	// assertEquals(text, token2.getText());
	// //check that the scanner has inserted an EOF token at the end
	// Scanner.Token token3 = scanner.nextToken();
	// assertEquals(Scanner.Kind.EOF,token3.kind);
	// }

	/**
	 * This test illustrates how to check that the Scanner detects errors properly. In this test, the input contains an int literal with a value that exceeds the range of an int. The scanner should detect this and throw and IllegalNumberException.
	 * 
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	// @Test
	// public void testIntOverflowError() throws IllegalCharException,
	// IllegalNumberException {
	// String input = "99999999999999999";
	// Scanner scanner = new Scanner(input);
	// thrown.expect(IllegalNumberException.class);
	// scanner.scan();
	// }

	@Test
	public void testEOF() throws IllegalCharException,
			IllegalNumberException {
		String input = "";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(1, scanner.tokens.size()); // only EOF
		Scanner.Token token = scanner.nextToken();
		assertEquals(EOF, token.kind);
		assertEquals(0, token.pos);
		assertEquals(0, token.length);
		assertEquals(EOF.getText(), token.getText());
	}
	
	@Test
	public void testWhiteSpacesAndEOF() throws IllegalCharException,
			IllegalNumberException {
		// test: tokens array is empty, lineStarts has 2 entries
		String input = "       \n     \t    \r";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(1, scanner.tokens.size()); // only EOF
		Scanner.Token token = scanner.nextToken();
		assertEquals(EOF, token.kind);
		assertEquals(19, token.pos);
		assertEquals(0, token.length);
		assertEquals(EOF.getText(), token.getText());
	}

	@Test
	public void testNewLine() throws IllegalCharException,
			IllegalNumberException {
		// test: tokens array is empty, lineStarts has 2 entries
		String input = "   \n  \n   \n";
		// create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(0, scanner.lineStartsList.get(0).intValue()); // 1st line
		assertEquals(4, scanner.lineStartsList.get(1).intValue());
		assertEquals(7, scanner.lineStartsList.get(2).intValue());
		assertEquals(11, scanner.lineStartsList.get(3).intValue());
	}

	@Test
	public void testWhiteSpacesWithOperators() throws IllegalCharException,
			IllegalNumberException {
		// test: tokens array is empty, lineStarts has 2 entries
		String input = "  \n  \r* + & % + \n";
		// create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		assertEquals(6, scanner.tokens.size());
		// get the first token and check its kind, position, and contents (length and text)
		Scanner.Token token = scanner.nextToken();
		isValidSimpleToken(token, TIMES, 6);
		Scanner.Token token1 = scanner.nextToken();
		isValidSimpleToken(token1, PLUS, 8);
		Scanner.Token token2 = scanner.nextToken();
		isValidSimpleToken(token2, AND, 10);
		Scanner.Token token3 = scanner.nextToken();
		isValidSimpleToken(token3, MOD, 12);
		Scanner.Token token4 = scanner.nextToken();
		isValidSimpleToken(token4, PLUS, 14);
	}

	// TODO more tests

}
