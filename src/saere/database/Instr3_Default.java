package saere.database;

import java.util.Iterator;
import java.util.List;

import saere.Solutions;
import saere.State;
import saere.Term;

/**
 * Example for a rather short predicate with a very high frequency. Uses lists.
 * 
 * @author David Sullivan
 * @version $Id$
 */
public class Instr3_Default extends DatabasePredicate {

	private List<Term> facts;
	
	public Instr3_Default() {
		super("instr", 3);
		facts = Database_Default.getInstance().getFacts(this.functor);
	}

	@Override
	public Solutions unify(Term... terms) {
		if (terms.length == arity) {
			return unify(terms[0], terms[1], terms[2]);
		} else {
			return new EmptySolutions();
		}
	}

	public Solutions unify(Term t0, Term t1, Term t2) {
		return new Instr3Solutions(t0, t1, t2);
	}

	private class Instr3Solutions implements Solutions {
		
		private final Term t0;
		private final Term t1;
		private final Term t2;

		private final State s0;
		private final State s1;
		private final State s2;

		private final Iterator<Term> iterator;

		public Instr3Solutions(Term t0, Term t1, Term t2) {
			assert t0 != null && t1 != null && t2 != null : "A term is null";
			
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;

			// save original states
			s0 = t0.manifestState();
			s1 = t1.manifestState();
			s2 = t2.manifestState();
			
			iterator = facts.iterator(); // XXX How much does a iterator 'cost'?
		}
		
		public boolean next() {
			
			// restore old states
			reset();

			while (iterator.hasNext()) {
				Term fact = iterator.next();
				
				// attempt unification...
				if (arity == fact.arity() && t0.unify(fact.arg(0)) && t1.unify(fact.arg(1)) && t2.unify(fact.arg(2))) {
					return true;
				} else {
					reset();
				}
			}
			
			return false;
		}
		
		private void reset() {
			t0.setState(s0);
			t1.setState(s1);
			t2.setState(s2);
		}
	}	
}
