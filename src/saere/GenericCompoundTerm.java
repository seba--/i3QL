package saere;

import saere.predicate.PredicateRegistry;

public class GenericCompoundTerm extends CompoundTerm {

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
	public int arity() {
		return args.length;
	}

	@Override
	public StringAtom functor() {
		return functor;
	}

	
	public Solutions call(){
		
		return PredicateRegistry.instance().createPredicateInstance(functor, args);
		
	}
}
