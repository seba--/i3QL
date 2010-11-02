package saere.database.predicate;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;

public final class Method15 extends DatabasePredicate {

	public Method15() {
		super("method", 15);
	}

	@Override
	public Solutions unify(Term... terms) {
		if (terms.length == arity) {
			return new Method15Solutions(terms[0], terms[1], terms[2], terms[3], terms[4], terms[5], terms[6], terms[7], terms[8], terms[9], terms[10], terms[11], terms[12], terms[13], terms[14]);
		} else {
			return EmptySolutions.getInstance();
		}
	}
	
	public Solutions unify(Term t0, Term t1, Term t2, Term t3, Term t4, Term t5, Term t6, Term t7, Term t8, Term t9, Term t10, Term t11, Term t12, Term t13, Term t14) {
		return new Method15Solutions(t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
	}
	
	private class Method15Solutions implements Solutions {

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
		private final Term t10;
		private final Term t11;
		private final Term t12;
		private final Term t13;
		private final Term t14;
		
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
		private final State s10;
		private final State s11;
		private final State s12;
		private final State s13;
		private final State s14;
		
		private final Iterator<Term> iterator;
		
		public Method15Solutions(Term t0, Term t1, Term t2, Term t3, Term t4,Term t5, Term t6, Term t7, Term t8, Term t9, Term t10, Term t11, Term t12, Term t13, Term t14) {
			assert t0 != null && t1 != null && t2 != null && t3 != null && t4 != null && t5 != null && t6 != null && t7 != null && t8 != null && t9 != null && t10 != null && t11 != null && t12 != null && t13 != null && t14 != null : "A term is null";
			
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
			this.t10 = t10;
			this.t11 = t11;
			this.t12 = t12;
			this.t13 = t13;
			this.t14 = t14;
			
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
			s10 = t10.manifestState();
			s11 = t11.manifestState();
			s12 = t12.manifestState();
			s13 = t13.manifestState();
			s14 = t14.manifestState();
			
			iterator = database.getCandidates(new Term[] { functor, t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14 });
		}

		public boolean next() {

			// restore old states
			reset();
			
			while (iterator.hasNext()) {
				Term fact = iterator.next();
				
				// attempt unification...
				if (arity == fact.arity() 
						&& t0.unify(fact.arg(0)) && t1.unify(fact.arg(1)) && t2.unify(fact.arg(2)) 
						&& t3.unify(fact.arg(3)) && t4.unify(fact.arg(4)) && t5.unify(fact.arg(5)) 
						&& t6.unify(fact.arg(6)) && t7.unify(fact.arg(7)) && t8.unify(fact.arg(8)) 
						&& t9.unify(fact.arg(9)) && t10.unify(fact.arg(10)) && t11.unify(fact.arg(11))
						&& t12.unify(fact.arg(12)) && t13.unify(fact.arg(13)) && t14.unify(fact.arg(14))) {
					return true;
				} else {
					reset();
				}
			}
			
			return false;
		}
		
		// Actually, we may not have to reset all...
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
			t10.setState(s10);
			t11.setState(s11);
			t12.setState(s12);
			t13.setState(s13);
			t14.setState(s14);
		}
	}
}
