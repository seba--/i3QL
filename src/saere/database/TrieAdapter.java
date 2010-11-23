package saere.database;

import java.util.Iterator;

import saere.StringAtom;
import saere.Term;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;

public final class TrieAdapter implements DatabaseAdapter {

	private final Trie predicateSubtrie;
	private final TrieBuilder builder;
	
	public TrieAdapter(TrieDatabase database, StringAtom functor, int arity) {
		predicateSubtrie = database.getPredicateSubtrie(functor, arity);
		builder = database.trieBuilder();
	}
	
	@Override
	public Iterator<Term> query(Term query) {
		return builder.iterator(predicateSubtrie, query);
	}
}
