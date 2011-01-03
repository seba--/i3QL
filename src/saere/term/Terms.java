package saere.term;

import saere.CompoundTerm;
import saere.FloatValue;
import saere.IntValue;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class Terms {

	private Terms() {
		// nothing to do...
	}

	public static StringAtom atomic(String value) {
		return StringAtom.get(value);
	}

	public static IntValue atomic(long value) {
		return IntValue.get(value);
	}

	public static FloatValue atomic(double value) {
		return FloatValue.get(value);
	}

	public static Variable variable() {
		return new Variable();
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term... args) {
		assert (args.length > 0);

		// arithmetic expression require special support
		switch (args.length) {
		case 1:
			// Terms with one argument
			switch (functor.rawValue()[0]) {
			case '-':
				return new Minus1(args[0]);
			default:
				return new GenericCompoundTerm(functor, args);
			}
		case 2:
			// Terms with two arguments
			switch (functor.rawValue()[0]) {

			// control-flow related terms
			case ',':
				return new And2(args[0], args[1]);
			case ';':
				return new Or2(args[0], args[1]);

				// arithmetic terms
			case '-':
				return new Minus2(args[0], args[1]);
			case '+':
				return new Plus2(args[0], args[1]);

				// other commonly used terms
			case '.':
				return new ListElement2(args[0], args[1]);

			default:
				return new GenericCompoundTerm(functor, args);
			}

		default:
			return new GenericCompoundTerm(functor, args);
		}
	}

	// Special methods for the most standard Prolog terms

	public static And2 and(Term t1, Term t2) {
		return new And2(t1, t2);
	}

	public static Or2 or(Term t1, Term t2) {
		return new Or2(t1, t2);
	}

	public static CompoundTerm unify(Term t1, Term t2) {
		return new Equals2(t1, t2);
	}
	
	public static ListElement2 list(Term head,Term tail){
		return new ListElement2(head, tail);
	}

	public static Term delimitedList(Term... terms) {
		Term rest = StringAtom.EMPTY_LIST;

		for (int i = terms.length - 1; i >= 0; i--) {
			rest = new ListElement2(terms[i], rest);
		}

		return rest;
	}

}
