package saere.database.index.reference;

import saere.StringAtom;

public final class SimpleKey implements Key {
	
	private final StringAtom key;
	
	public SimpleKey(StringAtom key) {
		this.key = key;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SimpleKey) {
			SimpleKey other = (SimpleKey) obj;
			return key.sameAs(other.key);
		} else {
			return false;
		}
	}
}
