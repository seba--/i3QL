package saere.database;

import java.util.Iterator;

import saere.Term;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import saere.database.predicate.DatabasePredicate;

/**
 * A {@link Trie}-based database that can be used by {@link DatabasePredicate}s 
 * unifications.
 * 
 * @author David Sullivan
 * @version 0.4, 10/19/2010
 * @see DatabasePredicate#useTries()
 */
public class TrieDatabase extends Database {

	private Trie root;
	private TrieBuilder builder;

	public TrieDatabase(TrieBuilder builder) {
		this.builder = builder;
		root = Trie.root();
	}
	
	@Override
	public void add(Term fact) {
		builder.insert(fact, root);
	}

	@Override
	public void drop() {
		root = Trie.root();
		System.gc();
	}

	@Override
	public Iterator<Term> getFacts() {
		return builder.iterator(root);
	}

	@Override
	public Iterator<Term> query(Term query) {
		return builder.iterator(root, query);
	}
}
