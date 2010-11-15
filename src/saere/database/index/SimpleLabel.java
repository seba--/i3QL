package saere.database.index;

import saere.Atom;


/**
 * Represents a simple label (a single atom/functor).
 * 
 * @author David Sullivan
 * @version 0.1, 11/9/2010
 */
public abstract class SimpleLabel extends Label {
	
	protected final Atom atom;
	
	protected SimpleLabel(Atom atom) {
		this.atom = atom;
	}
	
	@Override
	public int arity() {
		return 0;
	}

	@Override
	public Atom atom() {
		return atom;
	}
	
	@Override
	public int length() {
		return 1;
	}
	
	@Override
	public Label[] labels() {
		throw new UnsupportedOperationException("A simple label has only one atom");
	}
	
	@Override
	public Label split(int index) {
		throw new UnsupportedOperationException("Cannot split a simple label");
	}
	
	@Override
	public int match(Label other) {
		if (this == other) return 1;
		else return 0;
	}
}
