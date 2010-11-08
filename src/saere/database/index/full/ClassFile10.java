package saere.database.index.full;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.Variable;
import saere.database.predicate.DatabasePredicate;
import saere.database.predicate.EmptySolutions;

/**
 * Example for a rather short predicate with a low frequency. Uses lists.
 * 
 * @author David Sullivan
 * @version 0.2, 9/22/2010
 */
public final class ClassFile10 extends DatabasePredicate implements NoCollision {
	
	public ClassFile10() {
		super("class_file", 10);
	}
	
	@Override
	public Solutions unify(Term... terms) {
		if (terms.length == arity) {
			return new ClassFile10Solutions(terms[0], terms[1], terms[2], terms[3], terms[4], terms[5], terms[6], terms[7], terms[8], terms[9]);
		} else {
			return EmptySolutions.getInstance();
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
		
		// Free variables
		private final Variable var0;
		private final Variable var1;
		private final Variable var2;
		private final Variable var3;
		private final Variable var4;
		private final Variable var5;
		private final Variable var6;
		private final Variable var7;
		private final Variable var8;
		private final Variable var9;
		
		private final Iterator<Term> iterator;
		
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
			if (t3.isVariable() && !t3.asVariable().isInstantiated()) {
				var3 = t3.asVariable();
			} else {
				var3 = null;
			}
			if (t4.isVariable() && !t4.asVariable().isInstantiated()) {
				var4 = t4.asVariable();
			} else {
				var4 = null;
			}
			if (t5.isVariable() && !t5.asVariable().isInstantiated()) {
				var5 = t5.asVariable();
			} else {
				var5 = null;
			}
			if (t6.isVariable() && !t6.asVariable().isInstantiated()) {
				var6 = t6.asVariable();
			} else {
				var6 = null;
			}
			if (t7.isVariable() && !t7.asVariable().isInstantiated()) {
				var7 = t7.asVariable();
			} else {
				var7 = null;
			}
			if (t8.isVariable() && !t8.asVariable().isInstantiated()) {
				var8 = t8.asVariable();
			} else {
				var8 = null;
			}
			if (t9.isVariable() && !t9.asVariable().isInstantiated()) {
				var9 = t9.asVariable();
			} else {
				var9 = null;
			}
			
			iterator = database.getCandidates(new Term[] { functor, t0, t1, t2, t3, t4, t5, t6, t7, t8, t9 });
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
				if (var3 != null)
					var3.bind(fact.arg(3));
				if (var4 != null)
					var4.bind(fact.arg(4));
				if (var5 != null)
					var5.bind(fact.arg(5));
				if (var6 != null)
					var6.bind(fact.arg(6));
				if (var7 != null)
					var7.bind(fact.arg(7));
				if (var8 != null)
					var8.bind(fact.arg(8));
				if (var9 != null)
					var9.bind(fact.arg(9));
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
			if (var3 != null)
				t3.setState(s3);
			if (var4 != null)
				t4.setState(s4);
			if (var5 != null)
				t5.setState(s5);
			if (var6 != null)
				t6.setState(s6);
			if (var7 != null)
				t7.setState(s7);
			if (var8 != null)
				t8.setState(s8);
			if (var9 != null)
				t9.setState(s9);
		}
	}
}
