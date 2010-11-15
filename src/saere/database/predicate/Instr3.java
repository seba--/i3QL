package saere.database.predicate;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.TrieDatabase;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import saere.meta.GenericCompoundTerm;

/**
 * Example for a rather short predicate with a very high frequency.
 * 
 * @author David Sullivan
 * @version 0.4, 10/12/2010
 */
public final class Instr3 extends DatabasePredicate {
	
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
		
		private Iterator<Term> iterator;
		private int progress;

		public Instr3Solutions(Term t0, Term t1, Term t2) {
			assert t0 != null && t1 != null && t2 != null : "A term is null";
			
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;

			// save original states
			s0 = t0.manifestState();
			s1 = t1.manifestState();
			s2 = t2.manifestState();
			
			
			/*
			// Check wether we've only unbound variable, in this case we'll take a term iterator (not a term query iterator)
			if (database instanceof TrieDatabase && t0.isVariable() && !t0.asVariable().isInstantiated() && t1.isVariable() && !t1.asVariable().isInstantiated() && t2.isVariable() && !t2.asVariable().isInstantiated()) {
				iterator = ((TrieDatabase) database).termIterator(functor, arity);
			} else {
				iterator = database.query(new GenericCompoundTerm(functor, new Term[] { t0, t1, t2 }));
			}
			*/
			
			iterator = database.query(new GenericCompoundTerm(functor, new Term[] { t0, t1, t2 }));
		}
		
		public boolean next() {

			// restore old states
			reset();
			progress = 0;

			while (iterator.hasNext()) {
				Term fact = iterator.next();
				
				// attempt unification...
				if (arity == fact.arity()) {
					if (t0.unify(fact.arg(0))) {
						progress++;
						if (t1.unify(fact.arg(1))) {
							progress++;
							if (t2.unify(fact.arg(2))) {
								progress++;
								return true;
							}
						}
					}
				}

				reset();
				progress = 0;
			}
			
			return false;
		}
		
		private void reset() {
			if (progress > 0) t0.setState(s0);
			if (progress > 1) t1.setState(s1);
			if (progress > 2) t2.setState(s2);
		}
	}
}
