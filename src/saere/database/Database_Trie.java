package saere.database;

import java.util.List;

import saere.StringAtom;
import saere.Term;

public class Database_Trie extends Database {
	
	private static final Database_Trie INSTANCE = new Database_Trie();

	private TermTrie trie;

	private Database_Trie() {
		trie = new TermTrie(TermTrie.ROOT_ATOM);
	}

	public static Database_Trie getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void add(Term fact) {
		trie.add(fact);
	}

	public List<Term> getFacts() {
		return trie.getAllTerms();
	}

	public List<Term> getFacts(StringAtom functor) {
		return trie.query(functor);
	}
	
	public TermTrie getPredicateSubtrie(StringAtom functor) {
		return trie.getPredicateSubtrie(functor);
	}
	
	/**
	 * Performs a query on the database.
	 * 
	 * @param terms The query parameters.
	 * @return A list of terms that answer the query.
	 */
	public List<Term> query(Term ... terms) {
		return trie.query(terms);
	}
	
	// XXX Remove later...
	public TermTrie getRoot() {
		return trie;
	}
}
