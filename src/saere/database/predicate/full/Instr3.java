package saere.database.predicate.full;

import static saere.database.Utils.hasFreeVariable;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.predicate.DatabasePredicate;
import saere.database.predicate.EmptySolutions;
import saere.meta.GenericCompoundTerm;

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
	public Solutions unify(Term query) {
		if (arity == query.arity() && functor.sameAs(query.functor())) {
			return new Instr3Solutions(query.arg(0), query.arg(1), query.arg(2));
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
		private final boolean var0;
		private final boolean var1;
		private final boolean var2;
		
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
			
			if (hasFreeVariable(t0)) var0 = true;
			else var0 = false;
			
			if (hasFreeVariable(t1)) var1 = true;
			else var1 = false;
			
			if (hasFreeVariable(t2)) var2 = true;
			else var2 = false;
			
			iterator = database.query(new GenericCompoundTerm(functor, new Term[] { t0, t1, t2 }));
		}
		
		public boolean next() {

			// restore old states
			reset();

			if (iterator.hasNext()) {
				reset();
				Term fact = iterator.next();
				
				// We get only terms that'll unify (no set of 'maybe' candidates)
				if (var0) t0.unify(fact.arg(0));
				if (var1) t1.unify(fact.arg(1));
				if (var2) t2.unify(fact.arg(2));
				
				return true;
			} else {
				return false;
			}
		}
		
		private void reset() {
			if (var0) t0.setState(s0);
			if (var1) t1.setState(s1);
			if (var2) t2.setState(s2);
		}
	}
}
