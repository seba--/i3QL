package saere.meta;

import saere.CompoundTerm;
import saere.StringAtom;
import saere.Term;

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

}
