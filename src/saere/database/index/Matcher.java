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
public final class Matcher {
	
	private Matcher() { /* empty */ }
	
	// In the following the left/first parameter is expected to be from the 
	// factbase (which cannot be a variable anymore).
	
	public static boolean match(Atom atom, Term term) {
		assert atom != null && term != null : "Parameter(s) is/are null";
		
		if (atom.isStringAtom() && term.isStringAtom()) { // Assuming we get string atoms more often...
			return atom.asStringAtom().sameAs(term.asStringAtom());
		} else if (atom.isIntegerAtom() && term.isIntegerAtom()) {
			return atom.asIntegerAtom().sameAs(term.asIntegerAtom());
		} else if (term.isVariable()) {
			Term binding = term.asVariable().binding();
			if (binding == null) {
				return true;
			} else {
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
	
	public static int match(Atom[] atoms, Term[] terms) {
		int min = Math.min(atoms.length, terms.length);
		int i;
		for (i = 0; i < min; i++) {
			if (!match(atoms[i], terms[i])) {
				break;
			}
		}
		
		return i;
	}
	
	public static int match(Atom[] atoms0, Atom[] atoms1) {
		int min = Math.min(atoms0.length, atoms1.length);
		int i;
		for (i = 0; i < min; i++) {
			if (!match(atoms0[i], atoms1[i])) {
				break;
			}
		}
		
		return i;
	}
	
	public static int match(Atom[] labelAtoms, Atom[] insertStack, int stackPosition) {
		int min = Math.min(labelAtoms.length, insertStack.length - stackPosition - 1);
		int i;
		for (i = 0; i < min; i++) {
			if (!match(labelAtoms[i], insertStack[i + stackPosition])) {
				break;
			}
		}
		
		return i;
	}
	 
	public static int match(Atom[] labelAtoms, Term[] queryStack, int stackPosition) {
		int min = Math.min(labelAtoms.length, queryStack.length - stackPosition - 1);
		int i;
		for (i = 0; i < min; i++) {
			if (!match(labelAtoms[i], queryStack[i + stackPosition])) {
				break;
			}
		}
			
		return i;
	}
}
