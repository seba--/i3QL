package saere.database.index;

import saere.Atom;
import saere.Term;
import saere.Variable;
import saere.database.Utils;

/**
 * An {@link AtomLabel} represents a label that has only one {@link Atom} / free {@link Variable}.
 * 
 * @author David Sullivan
 * @version 0.1, 9/21/2010
 */
public class AtomLabel extends Label {

	private Term term;
	
	// should only be called by Label.makeLabel(..)
	protected AtomLabel(Term term) {
		assert term.isStringAtom() || term.isIntegerAtom() || (term.isVariable() && !term.asVariable().isInstantiated()) : "Invalid parameter";
		this.term = term; // "wrap" only
	}
	
	@Override
	public int length() {
		return 1;
	}

	@Override
	public Term getLabel(int index) {
		assert index == 0 : "Index out of bounds";
		return term;
	}

	@Override
	public boolean isAtomLabel() {
		return true;
	}
	
	@Override
	public Label[] split(int index) {
		throw new UnsupportedOperationException("Cannot split an AtomLabel");
	}
	
	@Override
	public String toString() {
		return Utils.termToString(term); // XXX better no dependency on Utils
	}
	
	@Override
	public Term[] asArray() {
		return new Term[] { term };
	}
}
