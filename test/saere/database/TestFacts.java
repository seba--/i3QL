package saere.database;

import saere.CompoundTerm;
import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.meta.GenericCompoundTerm;

/**
 * A class for certain terms (or rather term patterns) that are not covered by 
 * the BAT-generated terms.
 * 
 * @author David Sullivan
 * @version 0.1, 11/4/2010
 */
public final class TestFacts {
	
	public static final String TEST_FUNCTOR = "f";
	
	// Terms/facts
	
	/** f(a(x,y),b,c(z). */
	public static final Term T0 = f(ct("a", sa("x"), sa("y")), sa("b"), ct("c", sa("z")));
	
	/** f(a(x),b,c(y,z). */
	public static final Term T1 = f(ct("a", sa("x")), sa("b"), ct("c", sa("y"), sa("z")));
	
	/** f(a(x,y,z),b,c). */
	public static final Term T2 = f(ct("a", sa("x"), sa("y"), sa("z")), sa("b"), sa("c"));
	
	/** f(a,b,c). */
	public static final Term T3 = f(sa("a"), sa("b"), sa("c"));
	
	/** f(d(x,y),b,c). */
	public static final Term T4 = f(ct("d", sa("x"), sa("y")), sa("b"), sa("c"));
	
	/** f(d(x,y),e,c). */
	public static final Term T5 = f(ct("d", sa("x"), sa("y")), sa("e"), sa("c"));
	
	public static final Term[] ALL_TERMS = { T0, T1, T2, T3, T4, T5 };
	
	// Queries
	
	/** f(X,b,Y). */
	public static final Term Q0 = f(v(), sa("b"), v());
	
	/** f(X,b,c). */
	public static final Term Q1 = f(v(), sa("b"), sa("c"));
	
	public static final Term[] ALL_QUERIES = { Q0, Q1 };
	
	private TestFacts() { /* empty */ }
	
	private static CompoundTerm f(Term ... args) {
		return ct(TEST_FUNCTOR, args);
	}
	
	private static CompoundTerm ct(String functor, Term ... args) {
		return new GenericCompoundTerm(sa(functor), args);
	}
	
	private static StringAtom sa(String value) {
		return StringAtom.StringAtom(value);
	}
	
	private static IntegerAtom ia(int value) {
		return IntegerAtom.IntegerAtom(value);
	}
	
	private static Variable v() {
		return new Variable();
	}
}
