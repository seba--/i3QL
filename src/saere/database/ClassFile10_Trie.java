package saere.database;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;

/**
 * Example for a rather short predicate with a low frequency. Uses tries.
 * 
 * @author David Sullivan
 * @version $Id$
 */
public class ClassFile10_Trie extends DatabasePredicate {
	
	private final Trie facts;
	
	public ClassFile10_Trie() {
		super("class_file", 10);
		facts = Database_Trie.getInstance().getPredicateSubtrie(this.functor);
	}
	
	@Override
	public Solutions unify(Term... terms) {
		if (terms.length == arity) {
			return unify(terms[0], terms[1], terms[2], terms[3], terms[4], terms[5], terms[6], terms[7], terms[8], terms[9]);
		} else {
			return new EmptySolutions();
		}
	}
	
	public Solutions unify(Term t0, Term t1, Term t2, Term t3, Term t4, Term t5, Term t6, Term t7, Term t8, Term t9) {
		return new ClassFile10Solutions(t0, t1, t2, t3, t4, t5, t6, t7, t8, t9);
	}
	
	private class ClassFile10Solutions implements Solutions {

		private final Term t0;
		private final Term t1;
		private final Term t2;
		private final Term t3;
		private final Term t4;
		private final Term t5;
		private final Term t6;
		private final Term t7;
		private final Term t8;
		private final Term t9;
		
		private final State s0;
		private final State s1;
		private final State s2;
		private final State s3;
		private final State s4;
		private final State s5;
		private final State s6;
		private final State s7;
		private final State s8;
		private final State s9;
		
		private boolean t0FreeVar;
		private boolean t1FreeVar;
		private boolean t2FreeVar;
		private boolean t3FreeVar;
		private boolean t4FreeVar;
		private boolean t5FreeVar;
		private boolean t6FreeVar;
		private boolean t7FreeVar;
		private boolean t8FreeVar;
		private boolean t9FreeVar;
		
		private Iterator<Term> iterator;
		
		public ClassFile10Solutions(Term t0, Term t1, Term t2, Term t3, Term t4,Term t5, Term t6, Term t7, Term t8, Term t9) {
			assert t0 != null && t1 != null && t2 != null && t3 != null && t4 != null && t5 != null && t6 != null && t7 != null && t8 != null && t9 != null : "A term is null";
			
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			this.t4 = t4;
			this.t5 = t5;
			this.t6 = t6;
			this.t7 = t7;
			this.t8 = t8;
			this.t9 = t9;
			
			// save original states
			s0 = t0.manifestState();
			s1 = t1.manifestState();
			s2 = t2.manifestState();
			s3 = t3.manifestState();
			s4 = t4.manifestState();
			s5 = t5.manifestState();
			s6 = t6.manifestState();
			s7 = t7.manifestState();
			s8 = t8.manifestState();
			s9 = t9.manifestState();
			
			iterator = facts.query(t0, t1, t2, t3, t4, t5, t6, t7, t8, t9).iterator();
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
				if (t3FreeVar)
					t3.asVariable().bind(fact.arg(3));
				if (t4FreeVar)
					t4.asVariable().bind(fact.arg(4));
				if (t5FreeVar)
					t5.asVariable().bind(fact.arg(5));
				if (t6FreeVar)
					t6.asVariable().bind(fact.arg(6));
				if (t7FreeVar)
					t7.asVariable().bind(fact.arg(7));
				if (t8FreeVar)
					t8.asVariable().bind(fact.arg(8));
				if (t9FreeVar)
					t9.asVariable().bind(fact.arg(9));
				
				return true;
			}

			reset();
			return false;
		}
		
		private void reset() {
			t0.setState(s0);
			t1.setState(s1);
			t2.setState(s2);
			t3.setState(s3);
			t4.setState(s4);
			t5.setState(s5);
			t6.setState(s6);
			t7.setState(s7);
			t8.setState(s8);
			t9.setState(s9);
		}
	}
}
