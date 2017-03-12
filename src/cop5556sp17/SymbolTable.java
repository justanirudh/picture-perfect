package cop5556sp17;

import cop5556sp17.AST.Dec;

public class SymbolTable {

	// TODO add fields

	/**
	 * to be called when block entered
	 */
	public void enterScope() {
		// TODO: IMPLEMENT THIS
	}

	/**
	 * leaves scope
	 */
	public void leaveScope() {
		// TODO: IMPLEMENT THIS
	}

	public boolean insert(String ident, Dec dec) {
		// TODO: IMPLEMENT THIS
		return true;
	}

	public Dec lookup(String ident) {
		// TODO: IMPLEMENT THIS
		return null;
	}

	public SymbolTable() {
		// TODO: IMPLEMENT THIS
	}

	@Override
	public String toString() {
		// TODO: IMPLEMENT THIS
		return "";
	}

}
