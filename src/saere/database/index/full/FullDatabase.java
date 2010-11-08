package saere.database.index.full;

import java.util.Iterator;

import saere.Term;
import saere.database.Database;

public final class FullDatabase extends Database {
	
	private static final FullDatabase INSTANCE = new FullDatabase();
	
	private FullTrieBuilder builder;
	private FullTrie root;
	
	private FullDatabase() {
		builder = new FullTrieBuilder();
		root = new FullTrie();	
	}
	
	public static FullDatabase getInstance() {
		return INSTANCE;
	}

	@Override
	public void add(Term fact) {
		builder.insert(fact, root);
	}

	@Override
	public void drop() {
		root = new FullTrie();
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
