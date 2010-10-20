package saere.database.index;

import saere.Atom;
import saere.database.TrieDatabase;

public class ComplexTrieDatabase extends TrieDatabase<Atom[]> {
	
	private static final ComplexTrieDatabase INSTANCE = new ComplexTrieDatabase();
	
	public static ComplexTrieDatabase getInstance() {
		return INSTANCE;
	}
}
