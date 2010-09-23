package saere.database;

import saere.Atom;
import saere.Term;
import saere.Variable;

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
	public int match(Label other) {
		if (same(term, other.getLabel(0))) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public boolean match(Term other) {
		return same(term, other);
	}
	
	@Override
	public int match(Term[] others) {
		if (others.length == 1 && match(others[0])) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public boolean same(Label other) {
		if (other.isAtomLabel() && same(term, other.getLabel(0))) {
			return true;
		} else {
			return false;
		}
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
}
