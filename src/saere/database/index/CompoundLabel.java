package saere.database.index;

import saere.Atom;
import saere.Term;
import saere.database.Utils;

/**
 * A {@link CompoundLabel} represents a label that stores an ordered array of {@link Atom}s.
 * 
 * @author David Sullivan
 * @version 0.2, 10/14/2010
 */
public final class CompoundLabel extends Label {

	private Atom[] atoms; // XXX Maybe a list would be easier to split? On the other hand this speeds up #asArray()
	
	public CompoundLabel(Atom[] atoms) {
		assert atoms.length > 1 : "length of terms is only 1";
		this.atoms = atoms; // "wrap" only
	}

	@Override
	public Atom getLabel(int index) {
		return atoms[index];
	}

	@Override
	public int length() {
		return atoms.length;
	}
	
	@Override
	public boolean isCompoundLabel() {
		return true;
	}
	
	@Override
	public Label split(int index) {
		assert index >= 0 && index < atoms.length - 1 : "Illegal split index " + index + "(length " + atoms.length + ")";
		
		Atom[] prefix = new Atom[index + 1];
		Atom[] suffix = new Atom[atoms.length - (index + 1)];
		
		System.arraycopy(atoms, 0, prefix, 0, prefix.length);
		System.arraycopy(atoms, index + 1, suffix, 0, suffix.length);
		
		atoms = prefix;
		return new CompoundLabel(suffix);
	}
	
	@Override
	public String toString() {
		String s = "Label=[";
		for (Term term : atoms) {
			s += Utils.termToString(term) + " ";
		}
		return s + "]"; 
	}
	
	@Override
	public Atom[] asArray() {
		return atoms;
	}
}
