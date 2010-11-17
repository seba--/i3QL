package saere.database.index.reference;

import saere.Atom;
import saere.StringAtom;

/**
 * Represents a two argument composite key.
 * 
 * @author David Sullivan
 * @version 1.0, 11/16/2010
 */
public final class CompositeKey implements Key {
	
	private final StringAtom first;
	private final Atom second;
	
	/**
	 * The first argument must be a string atom and the second can be a string 
	 * atom or integer atom.
	 * 
	 * @param first The first part of the key (a string atom).
	 * @param second The second part of the key (integer or string atom).
	 */
	public CompositeKey(StringAtom first, Atom second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public int hashCode() {
		return first.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CompositeKey) {
			CompositeKey other = (CompositeKey) obj;
			if (first.sameAs(other.first)) {
				if (second.isStringAtom() && other.second.isStringAtom()) {
					return second.asStringAtom().sameAs(other.second.asStringAtom());
				} else if (second.isIntegerAtom() && other.second.isIntegerAtom()) {
					return second.asIntegerAtom().sameAs(other.second.asIntegerAtom());
				}
			}
		}
		
		return false;
	}
}
