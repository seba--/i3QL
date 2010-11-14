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
	public Solutions unify(Term query) {
		if (arity == query.arity() && functor.sameAs(query.functor())) {
			return new ClassFile10Solutions(query.arg(0), query.arg(1), query.arg(2), query.arg(3), query.arg(4), query.arg(5), query.arg(6), query.arg(7), query.arg(8), query.arg(9));
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
		private final boolean var0;
		private final boolean var1;
		private final boolean var2;
		private final boolean var3;
		private final boolean var4;
		private final boolean var5;
		private final boolean var6;
		private final boolean var7;
		private final boolean var8;
		private final boolean var9;
		
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
			
			if (hasFreeVariable(t0)) var0 = true;
			else var0 = false;
			
			if (hasFreeVariable(t1)) var1 = true;
			else var1 = false;
			
			if (hasFreeVariable(t2)) var2 = true;
			else var2 = false;
			
			if (hasFreeVariable(t3)) var3 = true;
			else var3 = false;
			
			if (hasFreeVariable(t4)) var4 = true;
			else var4 = false;
			
			if (hasFreeVariable(t5)) var5 = true;
			else var5 = false;
			
			if (hasFreeVariable(t6)) var6 = true;
			else var6 = false;
			
			if (hasFreeVariable(t7)) var7 = true;
			else var7 = false;
			
			if (hasFreeVariable(t8)) var8 = true;
			else var8 = false;
			
			if (hasFreeVariable(t9)) var9 = true;
			else var9 = false;
			
			iterator = database.query(new GenericCompoundTerm(functor, new Term[] {t0, t1, t2, t3, t4, t5, t6, t7, t8, t9 }));
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
				if (var3) t3.unify(fact.arg(3));
				if (var4) t4.unify(fact.arg(4));
				if (var5) t5.unify(fact.arg(5));
				if (var6) t6.unify(fact.arg(6));
				if (var7) t7.unify(fact.arg(7));
				if (var8) t8.unify(fact.arg(8));
				if (var9) t9.unify(fact.arg(9));
				
				return true;
			} else {
				return false;
			}	
		}
		
		private void reset() {
			if (var0) t0.setState(s0);
			if (var1) t1.setState(s1);
			if (var2) t2.setState(s2);
			if (var3) t3.setState(s3);
			if (var4) t4.setState(s4);
			if (var5) t5.setState(s5);
			if (var6) t6.setState(s6);
			if (var7) t7.setState(s7);
			if (var8) t8.setState(s8);
			if (var9) t9.setState(s9);
		}
	}
}
