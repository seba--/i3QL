package saere.term;

import static saere.PredicateRegistry.predicateRegistry;
import saere.CompoundTerm;
import saere.PredicateFactory;
import saere.PredicateIdentifier;
import saere.Solutions;
import saere.StringAtom;
import saere.Term;

final class GenericCompoundTerm extends CompoundTerm {

	private final StringAtom functor;
	private final Term[] args;

	GenericCompoundTerm(StringAtom functor, Term[] args) {
		this.functor = functor;
		this.args = args;
	}

	@Override
	public Term arg(int i) throws IndexOutOfBoundsException {
		return args[i];
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
	public Solutions call() {
		PredicateIdentifier pi = new PredicateIdentifier(this.functor, args.length);
		PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
		return pf.createInstance(args);
	}

}
