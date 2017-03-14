package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public abstract class Expression extends ASTNode {

	private TypeName typeName;

	protected Expression(Token firstToken) {
		super(firstToken);
	}

	public TypeName getTypeName() {
		return typeName;
	}

	public void setTypeName(TypeName typeName) {
		this.typeName = typeName;
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
