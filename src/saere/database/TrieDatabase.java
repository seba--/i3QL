package saere.database;

import java.util.Iterator;

import saere.StringAtom;
import saere.Term;
import saere.database.index.EmptyTermIterator;
import saere.database.index.Trie;

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
		
		// simply iterate through children list (which should be okay if we have a small set of predicates)
		Trie predicate = root.getFirstChild();
		while (predicate != null) {
			if (predicate.labelMatches(functor)) {
				return predicate.iterator(new Term[] { functor });
			}
			predicate = predicate.getNextSibling();
		}
		
		return EmptyTermIterator.getInstance();
	}

	@Override
	public Iterator<Term> getCandidates(Term[] terms) {
		assert terms != null && terms.length > 1 && terms[0].isStringAtom() : "Invalid terms specified";
		
		return root.iterator(terms);
		/*
		// simply iterate through children list (which should be okay if we have a small set of predicates)
		Trie predicate = root.getFirstChild();
		while (predicate != null) {
			if (predicate.labelMatches(terms[0])) {
				return predicate.iterator(terms);
			}
			predicate = predicate.getNextSibling();
		}
		
		return EmptyTermIterator.getInstance();
		*/
	}
}
