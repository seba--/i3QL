package saere;

public final class Unification {

	private Unification(){/* empty */}
	
	/**
	 * Unifies two terms.
	 * <p> 
	 * <b> This method does not take care* of state handling.
	 * It is the responsibility of the caller to manifest the state of the given
	 * terms before calling this method and to restore the state after calling.
	 * </b> 
	 * This enables optimizations of how and when the state is saved / restored.
	 * </p>
	 * 
	 * @param t1 The first term.
	 * @param t2 The second term.
	 * @return <code>true</code> if both terms were successfully unified; <code>false</code>
	 *  otherwise.
	 */
	static boolean unify(Term t1, Term t2) {
		if (t1 == t2) {
			return true;
		}

		// Basically, the Robinson Algorithm
		if (t1.isVariable()) {
			final Variable v1 = t1.asVariable(); 
			if (v1.isInstantiated()) {
				return unify(v1.binding(), t2);
			} else {
				if (t2.isVariable()) {
					// Performance Evaluate if it is more efficient to always just share a free variable with another variable or to bind it.
					final Variable v2 = t2.asVariable();
					if (v2.isInstantiated()) {
						v1.bind(v2.binding());
						return true;
					} else {
						/* 
	 					 * If two variables, e.g. X = Y, that are both not instantiated then we have to 
						 * "link" both variables, because in this case Y is just an alias for X.
						 */
						v1.share(v2);
						return true;
					}
				} else {
					v1.bind(t2);
					return true;
				}
			}
		} else if (t2.isVariable()) { // t1 is not a variable...
			final Variable v2 = t2.asVariable(); 
			if (v2.isInstantiated()) {
				return unify(v2.binding(), t1);
			} else {
				v2.bind(t1);
				return true;
			}
		} else {
			return
			(t1.isStringAtom() && t2.isStringAtom() && t1.asStringAtom().sameAs(t2.asStringAtom())) ||
			(t1.isIntegerAtom() && t2.isIntegerAtom() && t1.asIntegerAtom().sameAs(t2.asIntegerAtom())) ||
			(t1.isCompoundTerm() && t2.isCompoundTerm() && ((t1.asCompoundTerm()).unify(t2.asCompoundTerm()))); 
		}
	}
}
