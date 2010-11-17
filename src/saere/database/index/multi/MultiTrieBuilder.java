package saere.database.index.multi;

import java.util.Iterator;

import saere.Atom;
import saere.Term;
import saere.database.index.AtomLabel;
import saere.database.index.FunctorLabel;
import saere.database.index.HashTrie;
import saere.database.index.Label;
import saere.database.index.StorageTrie;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import scala.serializable;

// XXX Many code fragments that are almost the same...
public final class MultiTrieBuilder extends TrieBuilder {

	public MultiTrieBuilder(int mapThreshold) {
		super(null, mapThreshold);
	}

	@Override
	public Trie insert(Term term, Trie start) {
		current = start;
		
		Label label;
		if (term.arity() == 0) {
			label = AtomLabel.AtomLabel(term.functor());
		} else {
			label = FunctorLabel.FunctorLabel(term.functor(), term.arity());
		}
		
		// The previous argument (on the same level/dimension)
		Trie parent;
		
		Trie searched = getChild(start, label);
		if (searched == null) {
			if (term.arity() == 0) {
				// Term is integer or string atom
				searched = new StorageTrie(start, label, term); // Store term at leaf
				addChild(start, searched);
				return searched;
			} else {
				// We have a compound term
				searched = new MultiTrie(start, label);
				addChild(start, searched);
				parent = searched;
				for (int i = 0; i < term.arity(); i++) {
					Term arg = term.arg(i);
					if (arg.isCompoundTerm()) {
						// Can never store a term because at least on atom node follows
						MultiTrie multi = new MultiTrie(parent, FunctorLabel.FunctorLabel(arg.functor(), arg.arity()));
						insert(arg, multi.getSubtrie()); // Recursion: We open another dimension. Watch out for lists!
						addChild(parent, multi);
						parent = multi;
					} else {
						// Term is integer or string atom
						if (i == term.arity() - 1) {
							// We must store a term (and the loop ends here)
							StorageTrie storageTrie = new StorageTrie(parent, AtomLabel.AtomLabel(atom(arg)), term);
							addChild(parent, storageTrie);
							return storageTrie;
						} else {
							Trie trie = new Trie(parent, AtomLabel.AtomLabel(atom(arg)));
							addChild(parent, trie);
							parent = trie;
						}
					}
				}
			}
		} else {
			// The searched child exists
			if (term.arity() == 0) {
				// Term is integer or string atom (and here MUST be a storage trie)
				searched.addTerm(term);
				return searched;
			} else {
				// We have a compound term
				parent = searched;
				for (int i = 0; i < term.arity(); i++) {
					Term arg = term.arg(i);
					if (arg.isCompoundTerm()) {
						// Can never store a term because at least on atom node follows
						insert(arg, parent.getSubtrie()); // Recursion: We open another dimension. Watch out for lists!
						
						if (parent.getFirstChild() == null) {
							MultiTrie multiTrie = new MultiTrie(parent, FunctorLabel.FunctorLabel(arg.functor(), arg.arity()));
							addChild(parent, multiTrie);
							parent = parent.getFirstChild();
						} else {
							Label argLabel = FunctorLabel.FunctorLabel(arg.functor(), arg.arity());
							Trie searchedChild = getChild(parent, argLabel);
							if (searchedChild == null) {
								searchedChild = new MultiTrie(parent, argLabel);
								addChild(parent, searched);
							}
							parent = searchedChild;
						}
					} else {
						// Term is integer or string atom
						if (i == term.arity() - 1) {
							if (parent.getFirstChild() == null) {
								Trie trie = new StorageTrie(parent, AtomLabel.AtomLabel(atom(arg)), term);
								addChild(parent, trie);
								return trie;
							} else {
								Label argLabel = AtomLabel.AtomLabel(atom(arg));
								Trie searchedChild = getChild(parent, argLabel);
								if (searchedChild == null) {
									searchedChild = new StorageTrie(parent, argLabel, term);
									addChild(parent, searchedChild);
									return searchedChild;
								} else {
									searchedChild.addTerm(term);
									return searchedChild;
								}
							}
						} else {							
							if (parent.getFirstChild() == null) {
								Trie trie = new Trie(parent, AtomLabel.AtomLabel(atom(arg)));
								addChild(parent, trie);
								parent = parent.getFirstChild();
							} else {
								Label argLabel = AtomLabel.AtomLabel(atom(arg));
								Trie searchedChild = getChild(parent, argLabel);
								if (searchedChild == null) {
									if (i == term.arity() - 1) {
										searchedChild = new StorageTrie(parent, argLabel, term);
										addChild(parent, searchedChild);
										return searchedChild;
									} else {
										searchedChild = new Trie(parent, argLabel);
										addChild(parent, searchedChild);
									}
								}
								parent = searchedChild;
							}
						}
					}
				}
			}
		}
		
		return parent; // XXX ???
	}

	@Override
	public Iterator<Term> iterator(Trie start, Term query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Term term, Trie start) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public String toString() {
		return "multi";
	}
	
	@Override
	protected void addChild(Trie parent, Trie child) {
		// Append to the head of the children list
		child.setNextSibling(parent.getFirstChild());
		parent.setFirstChild(child);
		
		// Transform to hash trie node?
		parent.setChildrenNumber(parent.getChildrenNumber() + 1);
		if (parent.getChildrenNumber() == mapThreshold) {
			
			HashTrie hashTrie = new HashTrie(parent.getParent(), parent.getLabel(), null);
			replace(parent, hashTrie);
			
			// Fill the hash map as replace() doesn't care for this
			Trie trie = hashTrie.getFirstChild();
			while (trie != null) {
				hashTrie.getMap().put(trie.getLabel(), trie);
				trie = trie.getNextSibling();
			}
			
		} else if (parent.getMap() != null) {
			parent.getMap().put(child.getLabel(), child);
		}
	}
	
	@Override
	protected Trie getChild(Trie parent, Label label) {
		if (parent.getMap() != null) {
			return parent.getMap().get(label);
		} else {
			Trie child = parent.getFirstChild();
			while (child != null) {
				if (child.getLabel().sameAs(label)) {
					return child;
				} else {
					child = child.getNextSibling();
				}
			}
		}
		
		return null;
	}
		
	private Atom atom(Term term) {
		assert term.isIntegerAtom() || term.isStringAtom() : "Term is not an atom: " + term;
		return term.isIntegerAtom() ? term.asIntegerAtom() : term.asStringAtom();
	}
}
