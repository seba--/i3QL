package saere.database.index;

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
 * @version 0.1, 10/6/2010
 */
public final class Matcher {
	
	private Matcher() {
		// ...
	}
	
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
					return match(t0, v1.binding()); // loop of doom with cyclic bindinds?
				}
			} else { // t0 is bound and t1 ist not a variable
				return match(t0, t1);
			}
		} else if (t1.isVariable()) { // !t0.isVariable() && t1.isVariable()
			Variable v1 = t1.asVariable();
			if (!v1.isInstantiated()) {
				return true; // for this purpose two variables are the same, if they are not instantiated
			} else {
				return match(t0, v1.binding()); // loop of doom with cyclic bindinds?
			}
		}
		
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
	 * Checks how much the specified {@link Label} and {@link TermStack}, that 
	 * is, the {@link Term}<tt>[]</tt> arrays they wrap, match.
	 * 
	 * @param label The label.
	 * @param stack The term stack.
	 * @return
	 * @see Matcher#match(Term[], Term[])
	 */
	public static int match(Label label, TermStack stack) {		
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
	
}
