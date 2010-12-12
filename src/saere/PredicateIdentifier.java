package saere;

public final class PredicateIdentifier {

	private final StringAtom functor;
	private final int arity;
	private final int hashCode;

	PredicateIdentifier(StringAtom functor, int arity) {
		this.functor = functor;
		this.arity = arity;
		this.hashCode = functor.hashCode() + arity;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof PredicateIdentifier) {
			return this.sameAs((PredicateIdentifier) other);
		}
		return false;
	}

	public boolean sameAs(PredicateIdentifier other) {
		return this.arity == other.arity
				&& this.functor.sameAs(other.functor);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return functor.toString() + "/" + arity;
	}
}