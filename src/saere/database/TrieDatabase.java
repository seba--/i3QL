package saere.database;

import java.util.Iterator;

import saere.StringAtom;
import saere.Term;
import saere.database.index.FullFlattener;
import saere.database.index.FunctorLabel;
import saere.database.index.InnerNode;
import saere.database.index.Label;
import saere.database.index.Root;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import saere.database.predicate.DatabasePredicate;

/**
 * A {@link InnerNode}-based database that can be used by {@link DatabasePredicate}s 
 * unifications.
 * 
 * @author David Sullivan
 * @version 0.4, 10/19/2010
 * @see DatabasePredicate#useTries()
 */
public class TrieDatabase extends Database {

	private final boolean noCollision;
	private final TrieBuilder builder;

	private Trie root;
	
	public TrieDatabase(TrieBuilder builder) {
		this.builder = builder;
		root = new Root();
		
		if (builder.flattener() instanceof FullFlattener) {
			noCollision = true;
		} else {
			noCollision = false;
		}
	}
	
	public Iterator<Term> termIterator(StringAtom functor, int arity)  {
		Label functorLabel = FunctorLabel.FunctorLabel(functor, arity);
		return builder.iterator(TrieBuilder.getChildByLabel(root, functorLabel));
	}
	
	@Override
	public void add(Term fact) {
		builder.insert(fact, root);
	}

	@Override
	public void drop() {
		root = new Root();
		System.gc();
	}

	@Override
	public Iterator<Term> terms() {
		return builder.iterator(root);
	}

	@Override
	public Iterator<Term> query(Term query) {
		return builder.iterator(root, query);
	}
	
	public Trie getPredicateSubtrie(StringAtom functor, int arity) {
		return TrieBuilder.getChildByLabel(root, FunctorLabel.FunctorLabel(functor, arity));
	}
	
	public TrieBuilder trieBuilder() {
		return builder;
	}
	
	public Trie root() {
		return root;
	}
	
	@Override
	public boolean noCollision() {
		return noCollision;
	}

	@Override
	public DatabaseAdapter getAdapter(StringAtom functor, int arity) {
		return new TrieAdapter(this, functor, arity);
	}
}
