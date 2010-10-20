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
public class TrieDatabase<T> extends Database {

	private Trie<T> root;
	private TrieBuilder<T> builder;

	protected TrieDatabase() {
		root = new Trie<T>();
	}
	
	@Override
	public void add(Term fact) {
		builder.insert(fact, root);
	}

	@Override
	public void drop() {
		root = new Trie<T>();
		System.gc();
	}

	@Override
	public Iterator<Term> getFacts() {
		return builder.iterator(root);
	}

	@Override
	public Iterator<Term> getCandidates(Term[] terms) {
		assert terms != null && terms.length > 1 && terms[0].isStringAtom() : "Invalid terms specified";
		return builder.iterator(root, terms);
	}
}
