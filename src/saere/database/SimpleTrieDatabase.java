package saere.database;

import saere.Atom;

public class SimpleTrieDatabase extends TrieDatabase<Atom> {
	
	private static final SimpleTrieDatabase INSTANCE = new SimpleTrieDatabase();
	
	public static SimpleTrieDatabase getInstance() {
		return INSTANCE;
	}
}
