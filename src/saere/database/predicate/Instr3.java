package saere.database.predicate;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.EmptySolutions;
import saere.database.Trie;
import saere.database.TrieDatabase;

/**
 * Example for a rather short predicate with a very high frequency. Uses tries.
 * 
 * @author David Sullivan
 * @version $Id$
 */
public class Instr3 extends DatabasePredicate {
	
	public Instr3() {
		super("instr", 3);
	}

	@Override
	public Solutions unify(Term... terms) {
		if (terms.length == arity) {
			return unify(terms[0], terms[1], terms[2]);
		} else {
			return EmptySolutions.getInstance();
		}
	}

	public Solutions unify(Term arg0, Term arg1, Term arg2) {
		return new Instr3Solutions(arg0, arg1, arg2);
	}

	private class Instr3Solutions implements Solutions {
		
		private Term t0;
		private Term t1;
		private Term t2;

		private State s0;
		private State s1;
		private State s2;

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
			
			iterator = database.getCandidates(new Term[] { functor, t0, t1, t2 });
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
