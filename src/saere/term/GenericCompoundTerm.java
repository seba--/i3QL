package saere.term;

import static saere.PredicateRegistry.predicateRegistry;
import saere.CompoundTerm;
import saere.PredicateFactory;
import saere.PredicateIdentifier;
import saere.Goal;
import saere.StringAtom;
import saere.Term;

// TODO rethink how you create terms and the like...
public final class GenericCompoundTerm extends CompoundTerm {

	private final StringAtom functor;
	private final Term[] args;

	public GenericCompoundTerm(StringAtom functor, Term[] args) {
		this.functor = functor;
		this.args = args;
	}

	@Override
	public Term arg(int i) throws IndexOutOfBoundsException {
		return args[i];
	}

	@Override
	public Term firstArg() {
		return args[0];
	}

	@Override
	public Term secondArg() throws IndexOutOfBoundsException {
		return args[1];
	}

	@Override
	public int arity() {
		return args.length;
	}

	@Override
	public StringAtom functor() {
		return functor;
	}

	@Override
	public Goal call() {
		PredicateIdentifier pi = new PredicateIdentifier(this.functor, args.length);
		PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
		return pf.createInstance(args);
	}

}
