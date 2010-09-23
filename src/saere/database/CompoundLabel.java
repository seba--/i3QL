package saere.database;

import saere.Atom;
import saere.Term;
import saere.Variable;

/**
 * A {@link CompoundLabel} represents a label that stores an ordered array of {@link Atom}s / free {@link Variable}s.
 * 
 * @author David Sullivan
 * @version 0.1, 9/21/2010
 */
public class CompoundLabel extends Label {

	private Term[] terms; // XXX Maybe a list would be easier to split?
	
	// should only be called by Label.makeLabel(..)
	protected CompoundLabel(Term[] terms) {
		assert terms.length > 1 : "length of terms is only 1"; // actually, also check if the array contains only atoms / free variables
		this.terms = terms; // "wrap" only
	}
	
	@Override
	public Term getLabel(int index) {
		//assert index >= 0 && index < length() - 1 : "Index out of bounds"; // exception suffices
		return terms[index];
	}

	@Override
	public int length() {
		return terms.length;
	}

	@Override
	public int match(Label other) {
		int min = Math.min(terms.length, other.length());
		int matches = 0;
		
		for (int i = 0; i < min; i++) {
			if (same(terms[i], other.getLabel(i))) {
				matches++;
			} else {
				break;
			}
		}
		
		return matches;
	}
	
	@Override
	public int match(Term[] others) {
		int min = Math.min(terms.length, others.length);
		int matches = 0;
		
		for (int i = 0; i < min; i++) {
			if (same(terms[i], others[i])) {
				matches++;
			} else {
				break;
			}
		}
		
		return matches;
	}

	@Override
	public boolean same(Label other) {
		if (terms.length == other.length() && match(other) == terms.length) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean isCompoundLabel() {
		return true;
	}
	
	@Override
	public Label[] split(int index) {
		assert index >= 0 && index < terms.length - 1 : "Illegal split index " + index + "(length " + terms.length + ")";
		
		Label[] result = new Label[2];
		Term[] prefix = new Term[index + 1];
		Term[] suffix = new Term[terms.length - (index + 1)];
		
		System.arraycopy(terms, 0, prefix, 0, prefix.length);
		System.arraycopy(terms, index + 1, suffix, 0, suffix.length);
		
		result[0] = Label.makeLabel(prefix);
		result[1] = Label.makeLabel(suffix);
		return result;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (Term term : terms) {
			s += Utils.termToString(term) + " "; // XXX better no dependency on Utils
		}
		
		return s; 
	}

	@Override
	public boolean match(Term other) {
		return false;
	}
}
