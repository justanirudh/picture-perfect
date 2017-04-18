package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;

public class IfStatement extends Statement {


	private final Expression e;
	private final Block b;

	public IfStatement(Token firstToken, Expression e, Block b) {
		super(firstToken);
		this.e = e;
		this.b = b;
	}
	
	public Expression getE() {
		return e;
	}

	public Block getB() {
		return b;
	}

	
	@Override
	public String toString() {
		return "IfStatement [e=" + e + ", b=" + b + "]";
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((e == null) ? 0 : e.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof IfStatement)) {
			return false;
		}
		IfStatement other = (IfStatement) obj;
		if (b == null) {
			if (other.b != null) {
				return false;
			}
		} else if (!b.equals(other.b)) {
			return false;
		}
		if (e == null) {
			if (other.e != null) {
				return false;
			}
		} else if (!e.equals(other.e)) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIfStatement(this,arg);
	}


}
