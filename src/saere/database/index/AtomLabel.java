package saere.database.index;

import java.lang.ref.WeakReference;

import saere.Atom;

/**
 * Represents a label for an atom (integer atom or string atom). For example 'a'.
 * 
 * @author David Sullivan
 * @version 0.1, 11/9/2010
 */
public final class AtomLabel extends SimpleLabel {
	
	private AtomLabel(Atom atom) {
		super(atom);
	}
	
	@Override
	public int hashCode() {
		if (atom.isIntegerAtom()) {
			return atom.eval();
		} else {
			return atom.hashCode();
		}
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AtomLabel) {
			Atom a = ((AtomLabel) obj).atom;
			if (atom.isIntegerAtom() && a.isIntegerAtom()) {
				return this.hashCode() == obj.hashCode();
			} else if (atom.isStringAtom() && a.isStringAtom()) {
				return this.hashCode() == obj.hashCode();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	//@SuppressWarnings("constructorName")
	@SuppressWarnings("all")
	public static AtomLabel AtomLabel(Atom atom) {
		final Label candidate = new AtomLabel(atom);
		synchronized (CACHE) {
			WeakReference<Label> cached = CACHE.get(candidate);
			if (cached == null) {
				cached = new WeakReference<Label>(candidate);
				CACHE.put(candidate, cached);
			}
			return (AtomLabel) cached.get();
		}
	}
	
	@Override
	public String toString() {
		return atom.toString();
	}
}
