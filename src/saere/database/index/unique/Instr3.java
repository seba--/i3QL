package saere.database.index.unique;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.Variable;
import saere.database.predicate.DatabasePredicate;
import saere.database.predicate.EmptySolutions;

/**
 * Example for a rather short predicate with a very high frequency. Uses tries.
 * 
 * @author David Sullivan
 * @version 0.4, 10/12/2010
 */
public final class Instr3 extends DatabasePredicate implements NoCollision {
	
	public Instr3() {
		super("instr", 3);
	}

	@Override
	public Solutions unify(Term... terms) {
		if (terms.length == arity) {
			return new Instr3Solutions(terms[0], terms[1], terms[2]);
		} else {
			return EmptySolutions.getInstance();
		}
	}

	public Solutions unify(Term arg0, Term arg1, Term arg2) {
		return new Instr3Solutions(arg0, arg1, arg2);
	}

	private class Instr3Solutions implements Solutions {
		
		private final Term t0;
		private final Term t1;
		private final Term t2;

		private final State s0;
		private final State s1;
		private final State s2;
		
		// Free variables
		private final Variable var0;
		private final Variable var1;
		private final Variable var2;
		
		private Iterator<Term> iterator;

		public Instr3Solutions(Term t0, Term t1, Term t2) {
			assert t0 != null && t1 != null && t2 != null : "A term is null";
			
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;

			// save original states
			s0 = t0.manifestState();
			s1 = t1.manifestState();
			s2 = t2.manifestState();
			
			if (t0.isVariable() && !t0.asVariable().isInstantiated()) {
				var0 = t0.asVariable();
			} else {
				var0 = null;
			}
			if (t1.isVariable() && !t1.asVariable().isInstantiated()) {
				var1 = t1.asVariable();
			} else {
				var1 = null;
			}
			if (t2.isVariable() && !t2.asVariable().isInstantiated()) {
				var2 = t2.asVariable();
			} else {
				var2 = null;
			}
			
			iterator = database.getCandidates(new Term[] { functor, t0, t1, t2 });
		}
		
		public boolean next() {

			// restore old states
			reset();

			if (iterator.hasNext()) {
				Term fact = iterator.next();
				
				// We get only terms that'll unify (no set of 'maybe' candidates)
				if (var0 != null)
					var0.bind(fact.arg(0));
				if (var1 != null)
					var1.bind(fact.arg(1));
				if (var2 != null)
					var2.bind(fact.arg(2));
				return true;
			} else {
				return false;
			}
		}
		
		private void reset() {
			if (var0 != null)
				t0.setState(s0);
			if (var1 != null)
				t1.setState(s1);
			if (var2 != null)
				t2.setState(s2);
		}
	}
}
