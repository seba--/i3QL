package saere.database.index.unique;

import java.util.Iterator;

import saere.Term;
import saere.database.Database;

public final class UniqueDatabase extends Database {
	
	private static final UniqueDatabase INSTANCE = new UniqueDatabase();
	
	private UniqueTrieBuilder builder;
	private UniqueTrie root;
	
	private UniqueDatabase() {
		builder = new UniqueTrieBuilder();
		root = new UniqueTrie();	
	}
	
	public static UniqueDatabase getInstance() {
		return INSTANCE;
	}

	@Override
	public void add(Term fact) {
		builder.insert(fact, root);
	}

	@Override
	public void drop() {
		root = new UniqueTrie();
		System.gc();
	}

	@Override
	public Iterator<Term> getCandidates(Term[] terms) {
		return builder.iterator(root, terms);
	}

	@Override
	public Iterator<Term> getFacts() {
		return builder.iterator(root);
	}

}
