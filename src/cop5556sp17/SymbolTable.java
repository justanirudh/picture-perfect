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
			currentScope = -1;
		else
			currentScope = scopeStack.peek();
	}

	public boolean insert(String ident, Dec dec) {
		Attr newAttr = new Attr(currentScope, dec);
		if (table.containsKey(ident)) {
			// this ident already declared. First check whether it has not been declared in current scope.
			for (Attr attr : table.get(ident)) {
				if (attr.scope == currentScope) // found an ident already in the table with the same scope as current scope
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

	public Dec lookup(String ident) {
		if (!table.containsKey(ident))
			return null;
		ArrayList<Attr> declarations = table.get(ident);
		Dec dec = null;
		int minDiff = Integer.MAX_VALUE;
		for (Attr attr : declarations) {
			int currDiff = currentScope - attr.scope; // current scope always >= some previously declared scope
			if (currDiff == 0) // if scope of the declaration of the ident same as the current scope, we got what we wanted
				return attr.dec;
			if (currDiff < minDiff) {
				minDiff = currDiff;
				dec = attr.dec;
			}
		}
		return dec;
	}

	public SymbolTable() {
		currentScope = -1;
		nextScope = 0;
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
