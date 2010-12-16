package saere.term;

import saere.Atomic;
import saere.CompoundTerm;
import saere.FloatAtom;
import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class TermFactory {

	private static TermFactory instance = new TermFactory();

	public static TermFactory getInstance() {
		return instance;
	}

	protected TermFactory() {
		// nothing to do...
	}

	protected Variable newVariable() {
		return new Variable();
	}

	protected Variable newNamedVariable(String name) {
		// TODO support named variables
		return new Variable();
	}

	protected Atomic newAtom(long value) {
		return IntegerAtom.IntegerAtom(value);
	}

	protected Atomic newAtom(double value) {
		return FloatAtom.instance(value);
	}

	protected Atomic newAtom(String string) {
		return StringAtom.instance(string);
	}

	protected Atomic newCut() {
		return StringAtom.CUT_FUNCTOR;
	}

	protected Atomic newEmptyList() {
		return StringAtom.EMPTY_LIST_FUNCTOR;
	}

	protected CompoundTerm newAnd(Term t1, Term t2) {
		return new And2(t1, t2);
	}

	protected CompoundTerm newOr(Term t1, Term t2) {
		return new Or2(t1, t2);
	}

	protected CompoundTerm newUnify(Term t1, Term t2) {
		return new Equals2(t1, t2);
	}

	private CompoundTerm newCompoundTerm(StringAtom functor, Term[] args) {

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

	// The static methods are just convenience methods that always use
	// the default instance of the TermFactory.

	public static Atomic atomic(String value) {
		return getInstance().newAtom(value);
	}

	public static Atomic atomic(long value) {
		return getInstance().newAtom(value);
	}

	public static Atomic atomic(double value) {
		return getInstance().newAtom(value);
	}

	public static Variable variable() {
		return getInstance().newVariable();
	}

	public static Variable namedVariable(String name) {
		return getInstance().newNamedVariable(name);
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term... args) {
		return getInstance().newCompoundTerm(functor, args);
	}

	// Special methods for the most standard Prolog terms

	public static Atomic cut() {
		return getInstance().newCut();
	}

	public static Atomic emptyList() {
		return getInstance().newEmptyList();
	}

	public static CompoundTerm and(Term t1, Term t2) {
		return getInstance().newAnd(t1, t2);
	}

	public static CompoundTerm or(Term t1, Term t2) {
		return getInstance().newOr(t1, t2);
	}

	public static CompoundTerm unify(Term t1, Term t2) {
		return getInstance().newUnify(t1, t2);
	}

}
