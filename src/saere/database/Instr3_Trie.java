package saere.database;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;

/**
 * Example for a rather short predicate with a very high frequency. Uses tries.
 * 
 * @author David Sullivan
 * @version $Id$
 */
public class Instr3_Trie extends DatabasePredicate {
	
	private final TermTrie facts;

	public Instr3_Trie() {
		super("instr", 3);
		facts = Database_Trie.getInstance().getPredicateSubtrie(this.functor);
	}

	@Override
	public Solutions unify(Term... terms) {
		if (terms.length == 3) {
			return unify(terms[0], terms[1], terms[2]);
		} else {
			return new EmptySolutions();
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
		
		private boolean t0FreeVar;
		private boolean t1FreeVar;
		private boolean t2FreeVar;

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
			
			t0FreeVar = t0.isVariable() && !t0.asVariable().isInstantiated();
			t1FreeVar = t1.isVariable() && !t1.asVariable().isInstantiated();
			t2FreeVar = t2.isVariable() && !t2.asVariable().isInstantiated();
			
			iterator = facts.query(t0, t1, t2).iterator();
		}
		
		public boolean next() {

			// clear states of previous successful unification
			// (actually one time too much -- for the first next() call)
			reset();

			if (iterator.hasNext()) {
				Term fact = iterator.next();
				
				if (t0FreeVar)
					t0.asVariable().bind(fact.arg(0));
				if (t1FreeVar)
					t1.asVariable().bind(fact.arg(1));
				if (t2FreeVar)
					t2.asVariable().bind(fact.arg(2));
				
				return true;
			}

			reset();
			return false;
		}
		
		private void reset() {
			t0.setState(s0);
			t1.setState(s1);
			t2.setState(s2);
		}
	}
}
