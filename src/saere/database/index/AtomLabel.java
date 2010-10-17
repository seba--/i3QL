package saere.database.index;

import saere.Atom;
import saere.database.Utils;

/**
 * An {@link AtomLabel} represents a label that has only one {@link Atom}.
 * 
 * @author David Sullivan
 * @version 0.2, 10/14/2010
 */
public final class AtomLabel extends Label {

	private Atom atom;
	
	public AtomLabel(Atom atom) {
		this.atom = atom; // "wrap" only
	}
	
	@Override
	public int length() {
		return 1;
	}

	@Override
	public Atom getLabel(int index) {
		assert index == 0 : "Index out of bounds";
		return atom;
	}

	@Override
	public boolean isAtomLabel() {
		return true;
	}
	
	@Override
	public Label split(int index) {
		throw new UnsupportedOperationException("Cannot split an AtomLabel");
	}
	
	@Override
	public String toString() {
		return atom != null ? "Label=[" + Utils.termToString(atom) + "]" : "Label=[]";
	}
	
	@Override
	public Atom[] asArray() {
		return new Atom[] { atom };
	}
}
