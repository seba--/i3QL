package saere.database;

import static saere.database.Utils.isFreeVariable;

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
		if (allArgumentsAreFreeVariables(query)) {
			return builder.iterator(predicateSubtrie);
		} else {
			return builder.iterator(predicateSubtrie, query);
		}
	}
	
	// TODO Check how long this takes!
	private boolean allArgumentsAreFreeVariables(Term query) {
		for (int i = 0; i < query.arity(); i++) {
			if (!isFreeVariable(query.arg(i))) {
				return false;
			}
		}
		
		return true;
	}
}
