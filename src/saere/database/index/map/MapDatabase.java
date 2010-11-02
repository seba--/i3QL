package saere.database.index.map;

import java.util.Iterator;

import saere.Term;
import saere.database.Database;

public final class MapDatabase extends Database {
	
	private static final MapDatabase INSTANCE = new MapDatabase();
	
	private MapTrieBuilder builder;
	private MapTrie root;
	
	private MapDatabase() {
		builder = new MapTrieBuilder();
		root = new MapTrie();
	}
	
	public static MapDatabase getInstance()  {
		return INSTANCE;
	}

	@Override
	public void add(Term fact) {
		builder.insert(fact, root);
	}

	@Override
	public void drop() {
		root = new MapTrie();
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
