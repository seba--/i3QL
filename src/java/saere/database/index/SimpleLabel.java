package saere.database.index;

import saere.Atomic;


/**
 * Represents a simple label (a single atom/functor).
 * 
 * @author David Sullivan
 * @version 0.2, 12/20/2010
 */
public abstract class SimpleLabel extends Label {
	
	protected final Atomic atom;
	
	protected SimpleLabel(Atomic atom) {
		this.atom = atom;
	}
	
	@Override
	public int arity() {
		return 0;
	}

	@Override
	public Atomic atom() {
		return atom;
	}
}
