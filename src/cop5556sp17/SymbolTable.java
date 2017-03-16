package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import cop5556sp17.AST.Dec;

public class SymbolTable {

	private int currentScope, nextScope;
	private Stack<Integer> scopeStack;
	private class Attr {
		int scope;
		Dec dec;
		Attr(int scope, Dec dec) {
			this.scope = scope;
			this.dec = dec;
		}
	}
	private HashMap<String, ArrayList<Attr>> table;

	/**
	 * to be called when block entered
	 */
	public void enterScope() {
		currentScope = nextScope++;
		scopeStack.push(currentScope);
	}

	/**
	 * leaves scope
	 */
	public void leaveScope() {
		// TODO: Make sure this is correct; diff in slides
		scopeStack.pop();
		if (scopeStack.isEmpty())
			currentScope = 0;
		else
			currentScope = scopeStack.peek();
	}

	public boolean insert(String ident, Dec dec) {
		Attr newAttr = new Attr(currentScope, dec);
		if (table.containsKey(ident)) {
			// this ident already declared. First check whether it has not been declared in current scope.
			for (Attr attr : table.get(ident)) {
				if (attr.scope == currentScope) // found an ident already in the table with the same scope
																				// as current scope
					return false;
			}
			// not declared in current scope, then just add.
			table.get(ident).add(0, newAttr);// prepending to list of attributes
			return true;
		} else {
			// new ident declared in any scope
			ArrayList<Attr> list = new ArrayList<>();
			list.add(newAttr);
			table.put(ident, list);
			return true;
		}
	}
	/*
	 * gets matching entry in hash table; scan chain and return attributes for entry with scope number
	 * closest to the top of the scope stack;
	 */
	public Dec lookup(String ident) {
		// TODO: solve the parallel scope problem:
		/*
		 * This should return null; { //scope 1 int x; } { //scope 2 x = 5; }
		 */
		if (!table.containsKey(ident))
			return null;
		ArrayList<Attr> declarations = table.get(ident);
		Dec dec = null;
		int minDiff = Integer.MAX_VALUE;
		for (Attr attr : declarations) {
			int currDiff = currentScope - attr.scope;
			if (currDiff < 0) // current scope < scope of recorded attr, then I am in outer scope (cannot
												// be a parallel scope as it would be closed then and hence, not in current
												// scope)
				continue;
			if (currDiff == 0) // if scope of the declaration of the ident same as the current scope, we
													// got what we wanted
				return attr.dec;
			if (currDiff < minDiff) { // TODO: curr scope > attr scope, it is definitely nested. attr's
																// scope is
																// parent of current scope. curr scope can be parallel, *so use the
																// stack. Lookup the value in stack before making a decision. For
																// each entry in the Dec array, check the stack and its depth. the
																// entry woth minimum depth is returned. So, instead of minDiff, it
																// is minDepth and each operation is O(depth of stack)*
				minDiff = currDiff;
				dec = attr.dec;
			}
		}
		return dec;
	}

	public SymbolTable() { // for this language, outermost scope can be -1 as everything inside
													// 'ident{}'
		currentScope = 0; // cS and nS can start with 0 and 1, doesn't matter as long as diff of 1
		nextScope = 1;
		scopeStack = new Stack<>();
		table = new HashMap<>();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String ident : table.keySet()) {
			sb.append(ident + " -> ");
			for (Attr attr : table.get(ident)) {
				sb.append("( scope: " + attr.scope + ", " + "type: " + attr.dec.getType().kind + "), ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
