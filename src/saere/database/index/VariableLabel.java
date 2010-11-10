package saere.database.index;

import saere.Atom;

/**
 * This is {@link Label} representation for an <b>unbound</b> variable.
 * 
 * @author David Sullivan
 * @version 0.1, 11/9/2010
 */
public final class VariableLabel extends Label {

	private static final VariableLabel INSTANCE = new VariableLabel();

	private VariableLabel() { /* empty */ }
	
	@Override
	public int arity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Atom atom() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SimpleLabel[] labels() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int length() {
		throw new UnsupportedOperationException();
	}

	public static VariableLabel VariableLabel() {
		// This is a singleton, no need to put in the label cache
		return INSTANCE;
	}
	
	@Override
	public String toString() {
		return "_";
	}
}
