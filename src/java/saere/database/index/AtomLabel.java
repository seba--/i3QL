package saere.database.index;

import java.lang.ref.WeakReference;

import saere.Atomic;

/**
 * Represents a label for an atom (integer atom or string atom). For example 'a'.
 * 
 * @author David Sullivan
 * @version 0.3, 12/20/2010
 */
public final class AtomLabel extends SimpleLabel {
	
	private AtomLabel(Atomic atom) {
		super(atom);
	}
	
	@Override
	public int hashCode() {
		return atom.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AtomLabel) {
			Atomic a = ((AtomLabel) obj).atom;
			
			// Return true if the Prolog type is the same and the hash code
			if ((atom.isIntValue() && a.isIntValue())
					|| (atom.isStringAtom() && a.isStringAtom())
					|| (atom.isFloatValue() && a.isFloatValue())) {
				return this.hashCode() == a.hashCode();
			} else  {
				return false;
			}
		} else {
			return false;
		}
	}
	
	//@SuppressWarnings("constructorName")
	@SuppressWarnings("all")
	public static AtomLabel AtomLabel(Atomic atom) {
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
