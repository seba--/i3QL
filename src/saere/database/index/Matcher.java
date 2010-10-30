package saere.database.index;

import saere.Atom;
import saere.CompoundTerm;
import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Unification;
import saere.Variable;

/**
 * This class contains all matching methods (that are otherwise spread over 
 * various classes). Matching is like unification but <b>without binding</b>. 
 * Hence, {@link #match(Term, Term)} should return the same result 
 * (<tt>true</tt> or <tt>false</tt>) as {@link Unification#unify(Term, Term)} 
 * (without binding).
 * 
 * @author David Sullivan
 * @version 0.2, 10/14/2010
 */
public final class Matcher {
	
	private Matcher() { /* empty */ }
	
	/**
	 * Checks wether <tt>t0</tt> is the same as <tt>t1</tt> (i.e., if they <i>would</i> unify). 
	 * Both specified terms must be eather an atom or a variable.<br/>
	 * <br/>
	 * <b>Must yield the same result as {@link Term#unify(Term)} iff <tt>t0</tt> and <tt>t1</tt> are atoms or variables.</b>
	 * 
	 * @param t0 The first atom/variable.
	 * @param t1 The second atom/variable.
	 * @return <tt>true</tt> if so.
	 */
	// XXX What about cyclic bindings and the like?
	public static boolean match(Term t0, Term t1) {
		assert (t0.isIntegerAtom() || t0.isStringAtom() || t0.isVariable()) && (t1.isIntegerAtom() || t1.isStringAtom() || t1.isVariable()) : "Invalid parameter(s) specified";
		
		if (t0 == null) {
			return t1 == null; // null == null
		} else if (t1 == null) {
			return false; // a1 != null
		} else if (t0.isStringAtom() && t1.isStringAtom()) {
			return t0.asStringAtom().sameAs(t1.asStringAtom());
		} else if (t0.isIntegerAtom() && t1.isIntegerAtom()) {
			return t0.asIntegerAtom().sameAs(t1.asIntegerAtom());
		} else if (t0.isVariable()) {
			
			Variable v0 = t0.asVariable();
			if (!v0.isInstantiated()) {
				return true;
			} else {
				t0 = v0.binding(); // XXX What if another variable is returned?
			}

			if (t1.isVariable()) { // both are variables
				Variable v1 = t1.asVariable();
				if (!v1.isInstantiated()) {
					return true; // for this purpose two variables are the same, if they are not instantiated
				} else {
					return match(t0, v1.binding()); // loop with cyclic bindinds?
				}
			} else { // t0 is bound and t1 ist not a variable
				return match(t0, t1);
			}
		} else if (t1.isVariable()) { // !t0.isVariable() && t1.isVariable()
			Variable v1 = t1.asVariable();
			if (!v1.isInstantiated()) {
				return true; // for this purpose two variables are the same, if they are not instantiated
			} else {
				return match(t0, v1.binding()); // loop with cyclic bindinds?
			}
		}
		
		return false;
	}
	
	// In the following the left/first parameter is expected to be from the factbase (which cannot be a variable anymore)
	
	public static boolean match(Atom atom, Term term) {
		assert atom != null && term != null : "Parameter(s) is/are null";
		
		if (atom.isIntegerAtom() && term.isIntegerAtom()) {
			return atom.asIntegerAtom().sameAs(term.asIntegerAtom());
		} else if (atom.isStringAtom() && term.isStringAtom()) {
			return atom.asStringAtom().sameAs(term.asStringAtom());
		} else if (term.isVariable()) {
			
			// basically, code like in #mach(*, Variable)
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
	
	public static boolean match(IntegerAtom atom0, IntegerAtom atom1) {
		return atom0.sameAs(atom1);
	}
	
	public static boolean match(StringAtom atom0, StringAtom atom1) {
		return atom0.sameAs(atom1);
	}
	
	public static boolean match(IntegerAtom atom, Variable var) {
		Term binding = var.binding();
		if (binding == null) {
			return true;
		} else if (binding.isIntegerAtom()) {
			return atom.sameAs(binding.asIntegerAtom());
		} else { // var.isInstanciated() && !var.binding().isIntegerAtom()
			return false;
		}
	}
	
	public static boolean match(StringAtom atom, Variable var) {
		Term binding = var.binding();
		if (binding == null) {
			return true;
		} else if (binding.isStringAtom()) {
			return atom.sameAs(var.asStringAtom());
		} else { // var.isInstanciated() && !var.binding().isStringAtom()
			return false;
		}
	}
	
	// the following will never match
	
	public static boolean match(IntegerAtom atom0, StringAtom atom1) {
		return false;
	}
	
	public static boolean match(StringAtom atom0, IntegerAtom atom1) {
		return false;
	}
	
	public static boolean match(IntegerAtom atom, CompoundTerm term) {
		return false;
	}
	
	public static boolean match(StringAtom atom, CompoundTerm term) {
		return false;
	}
	
	/**
	 * Checks <i>how much</i> the two specified arrays match. The two arrays 
	 * have to contain only atoms or variables. Matching is done from the left 
	 * to the right and aborted as soon as the corresponding indixes do not 
	 * match.
	 * 
	 * @param terms0 The first array.
	 * @param terms1 The second array.
	 * @return The number of matches (from the left) until a mismatch occurs.
	 */
	public static int match(Term[] terms0, Term[] terms1) {
		int min = Math.min(terms0.length, terms1.length);
		int i;
		for (i = 0; i < min; i++) {
			if (!match(terms0[i], terms1[i])) {
				break;
			}
		}
		
		return i;
	}
	
	/**
	 * Checks how much the specified {@link Label} and {@link QueryStack}, that 
	 * is, the {@link Term}<tt>[]</tt> arrays they wrap, match.
	 * 
	 * @param label The label.
	 * @param stack The term stack.
	 * @return
	 * @see Matcher#match(Term[], Term[])
	 */
	public static int match(Label label, QueryStack stack) {		
		if (stack.size() == 0) {
			return 0;
		} else {
			return Matcher.match(label.asArray(), stack.asArray());
		}
	}
	
	/**
	 * Checks how much the specified {@link Label} and {@link QueryStack}, that 
	 * is, the {@link Term}<tt>[]</tt> arrays they wrap, match.
	 * 
	 * @param label The label.
	 * @param stack The term stack.
	 * @return
	 * @see Matcher#match(Term[], Term[])
	 */
	public static int match(Label label, InsertStack stack) {		
		if (stack.size() == 0) {
			return 0;
		} else {
			return Matcher.match(label.asArray(), stack.asArray());
		}
	}
	
	/**
	 * Checks wether the specified label (i.e., the term it wraps) and term 
	 * match.
	 * 
	 * @param label The label.
	 * @param term The term.
	 * @return <tt>true</tt> if they match.
	 */
	public static boolean match(Label label, Term term) {
		return label.isAtomLabel() && match(label.getLabel(0), term);
	}
	
	public static boolean match(Label label, Atom atom) {
		return label.isAtomLabel() && match(label.getLabel(0), atom);
	}
	
}
