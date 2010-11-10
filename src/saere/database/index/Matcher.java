package saere.database.index;

import saere.Atom;
import saere.Term;
import saere.Unification;

/**
 * This class contains all matching methods (that are otherwise spread over 
 * various classes). Matching is like unification but <b>without binding</b>. 
 * Hence, {@link #match(Atom, Term)} should return the same result 
 * (<tt>true</tt> or <tt>false</tt>) as {@link Unification#unify(Term, Term)} 
 * but without binding.
 * 
 * @author David Sullivan
 * @version 0.3, 10/18/2010
 */
@Deprecated
public final class Matcher {
	
	private Matcher() { /* empty */ }
	
	// In the following the left/first parameter is expected to be from the 
	// factbase (which cannot be a variable anymore).
	
	public static boolean match(Atom atom, Term term) {
		if (atom.isStringAtom() && term.isStringAtom()) { // Assuming we get string atoms more often...
			return atom.asStringAtom().sameAs(term.asStringAtom());
		} else if (atom.isIntegerAtom() && term.isIntegerAtom()) {
			return atom.asIntegerAtom().sameAs(term.asIntegerAtom());
		} else if (term.isVariable()) {
			Term binding = term.asVariable().binding();
			if (binding == null) {
				return true;
			} else {
				// No binding.asAtom(), so do it manually
				if (atom.isIntegerAtom() && binding.isIntegerAtom()) {
					return atom.asIntegerAtom().sameAs(binding.asIntegerAtom());
				} else if (atom.isStringAtom() && binding.isStringAtom()) {
					return atom.asStringAtom().sameAs(binding.asStringAtom());
				}
			}
		}
		
		return false;
	}
	
	public static boolean match(Atom atom0, Atom atom1) {
		if (atom0.isStringAtom() && atom1.isStringAtom()) { // Assuming we get string atoms more often...
			return atom0.asStringAtom().sameAs(atom1.asStringAtom());
		} else if (atom0.isIntegerAtom() && atom1.isIntegerAtom()) {
			return atom0.asIntegerAtom().sameAs(atom1.asIntegerAtom());
		}
		
		return false;
	}
	
	/*
	public static boolean match(AtomLabel label0, AtomLabel label1) {
		return match(label0.atom(), label1.atom());
	}
	
	public static boolean match(SimpleLabel label0, SimpleLabel label1) {
		return label0.arity() == label1.arity() && match(label0.atom(), label1.atom());
	}
	
	public static int match(ComplexLabel label0, ComplexLabel label1) {
		int min = Math.min(label0.length(), label1.length());
		SimpleLabel[] labels0 = label0.labels();
		SimpleLabel[] labels1 = label0.labels();
		int i;
		for (i = 0; i < min; i++) {
			if (!match(labels0[i], labels1[i]))
				break;
		}
		return i;
	}
	
	public static boolean match(Label label0, Label label1) {
		if (label0.length() != label1.length()) {
			return false;
		}
		
		if (label0.length() == 1) { // && label1.length() == 1
			return match(label0.asSimpleLabel(), label1.asSimpleLabel());
		} else { // length() > 1
			return label0.length() == match(label0.asComplexLabel(), label0.asComplexLabel());
		}
	}
	*/
}
