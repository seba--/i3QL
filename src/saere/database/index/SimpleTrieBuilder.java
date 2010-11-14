package saere.database.index;

import java.util.Iterator;

import saere.Term;

/**
 * The {@link SimpleTrieBuilder} uses for each element of a flattend term 
 * representation exactly one {@link Trie} node. That is, if the length of a 
 * flattened term repesenation is seven, the term will be found at node level 
 * seven (not counting the root).<br/>
 * <br/>
 * For example, the representation <code>[f, along, b, c]</code> will be 
 * stored/retrieved with four nodes with the labels <tt>f</tt>, <tt>along</tt>, 
 * <tt>b</tt> and <tt>c</tt> whereas the last node with label <tt>c</tt> at 
 * level four stores the term.
 * 
 * @author David Sullivan
 * @version 0.31, 10/17/2010
 */
public final class SimpleTrieBuilder extends TrieBuilder {
	
	public SimpleTrieBuilder(TermFlattener flattener, int mapThreshold) {
		super(flattener, mapThreshold);
	}

	@Override
	public Trie insert(Term term, Trie start) {
		stack = flattener.flatten(term);
		current = start;
		
		while (true) { // XXX Hmm...
			Label peeked = stack.peek();
			
			/*
			 * We search for the child we need by label. There are mainly four cases:
			 * 1) No such child exists and we want to add the term here.
			 * -- We create a new storage trie with the desired label, add the term and return.
			 * 
			 * 2) No such child exists and we don't want to add the the term here.
			 * -- We create a new (normal) trie and continue the insertion process with it.
			 * 
			 * 3) A child with the label already exists and we want to add the term here.
			 * -- If the child can store terms (i.e., stores already one) we simply add the term.
			 * -- If the child doesn't store we replace the child with a new node that can store (either storage trie or storage hash trie).
			 * 
			 * 4) A child with the label already exists and we don't want to the term here.
			 * -- Continue insertion process with the child.
			 */
			Trie searched = getChild(current, peeked);
			if (searched == null) {
				// No such node with the label we require exists
				if (stack.size() == 1) {
					// We need a storage trie (leaf)
					searched = new StorageTrie(current, peeked, term);
					addChild(current, searched);
					return searched;
				} else {
					// We need a normal (inner) trie
					searched = new Trie(current, peeked);
					addChild(current, searched);
					
					// Go down
					current = searched;
					stack.pop();
				}
			} else {
				// A child with the label already exists
				if (stack.size() == 1) {
					// We need a storage trie
					if (!searched.stores()) {
						// We want to add to a trie that already exists but doesn't store terms (inner trie node)
						Trie storageTrie;
						if (searched.hashes()) {
							storageTrie = new StorageHashTrie(current, current.getLabel(), current.getLastChild(), term);
							replace(current, storageTrie);
							storageTrie.setMap(current.getMap());
							return storageTrie;
						} else {
							storageTrie = new StorageTrie(current, current.getLabel(), term);
							replace(current, storageTrie);
							return storageTrie;
						}
					} else {
						// We want to add to a trie that already exists and stores (a) term(s) (collision)
						searched.addTerm(term);
						return searched;
					}
				} else {
					// We have a normal (inner) trie, go down
					current = searched;
					stack.pop();
				}
			}
		}
	}

	@Override
	public boolean remove(Term term, Trie start) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Iterator<Term> iterator(Trie start, Term query) {
		if (flattener instanceof ShallowFlattener) {
			return new ShallowSimpleQueryIterator(this, start, flattener.flatten(query));
		} else if (flattener instanceof FullFlattener) {
			return new FullSimpleQueryIterator(this, start, flattener.flatten(query));
		} else {
			throw new UnsupportedOperationException("Unexpected term flattener");
		}
	}
	
	@Override
	public String toString() {
		return flattener.toString() + "-simple";
	}	
}
