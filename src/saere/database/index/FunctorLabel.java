package saere.database.index;

import java.lang.ref.WeakReference;

import saere.Atomic;

/**
 * Represents a simple functor label (a single atom with an associated arity).
 * 
 * @author David Sullivan
 * @version 0.2, 12/20/2010
 */
public final class FunctorLabel extends SimpleLabel {
	
	private final int arity;
	
	private FunctorLabel(Atomic atom, int arity) {
		super(atom);
		assert arity > 0 : "arity must be greater-than 0"; // If zero, we should use an AtomLabel
		this.arity = arity;
	}
	
	@Override
	public int arity() {
		return arity;
	}
	
	@Override
	public int hashCode() {
		return atom.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FunctorLabel) {
			return this.hashCode() == obj.hashCode() && arity == ((FunctorLabel) obj).arity;
		} else {
			return false;
		}
	}
	
	//@SuppressWarnings("constructorName")
	@SuppressWarnings("all")
	public static FunctorLabel FunctorLabel(Atomic atom, int arity) {
		final Label candidate = new FunctorLabel(atom, arity);
		synchronized (CACHE) {
			WeakReference<Label> cached = CACHE.get(candidate);
			if (cached == null) {
				cached = new WeakReference<Label>(candidate);
				CACHE.put(candidate, cached);
			}
			return (FunctorLabel) cached.get();
		}
	}
	
	@Override
	public String toString() {
		return atom.toString() + "/" + arity;
	}
}
