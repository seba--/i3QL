package saere.database;

import java.util.Iterator;

import saere.StringAtom;
import saere.Term;
import saere.database.index.Trie;
import saere.database.predicate.DatabasePredicate;

/**
 * A {@link Trie}-based database that can be used by {@link DatabasePredicate}s 
 * unifications.
 * 
 * @author David Sullivan
 * @version 0.3, 10/14/2010
 * @see DatabasePredicate#useTries()
 */
public class TrieDatabase extends Database {
	
	private static final TrieDatabase INSTANCE = new TrieDatabase();

	private Trie root;

	private TrieDatabase() {
		root = new Trie();
	}

	public static TrieDatabase getInstance() {
		return INSTANCE;
	}
	
	// XXX Remove later...
	public Trie getRoot() {
		return root;
	}
	
	@Override
	public void add(Term fact) {
		root.insert(fact);
	}

	@Override
	protected void fillProcessComplete() {
		// root.prune();
	}

	@Override
	public void drop() {
		root = new Trie(); // and let the GC do the rest...
	}

	@Override
	public Iterator<Term> getFacts() {
		return root.iterator();
	}
	
	@Override
	public Iterator<Term> getFacts(StringAtom functor) {
		return root.iterator(new Term[] { functor });
	}

	@Override
	public Iterator<Term> getCandidates(Term[] terms) {
		assert terms != null && terms.length > 1 && terms[0].isStringAtom() : "Invalid terms specified";
		return root.iterator(terms);
	}
}
