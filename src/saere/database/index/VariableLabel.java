package saere.database.index;

import saere.Atom;

/**
 * This is {@link Label} representation for an <b>free</b> variable.
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

	//@SuppressWarnings("constructorName")
	@SuppressWarnings("all")
	public static VariableLabel VariableLabel() {
		// This is a singleton, no need to put it into the label cache
		return INSTANCE;
	}
	
	@Override
	public String toString() {
		return "?";
	}
}
