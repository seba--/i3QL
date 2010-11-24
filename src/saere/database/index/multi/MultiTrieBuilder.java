package saere.database.index.multi;

import java.util.Iterator;

import saere.Atom;
import saere.Term;
import saere.database.index.AtomLabel;
import saere.database.index.FunctorLabel;
import saere.database.index.InnerHashNode;
import saere.database.index.InnerNode;
import saere.database.index.Label;
import saere.database.index.SingleStorageLeaf;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import saere.term.EmptyList0;

// XXX Many code fragments that are almost the same...
// Storing is only
public final class MultiTrieBuilder extends TrieBuilder {

	public MultiTrieBuilder(int mapThreshold) {
		super(null, mapThreshold);
	}

	@Override
	public InnerNode insert(Term term, Trie start) {
		current = start;
		
		//return insert(term, start, 0);
		
		Label label = label(term);
		
		// The previous argument (on the same level/dimension), i.e., the parent
		Trie prevArg;
		
		Trie searched = getChild(start, label);
		if (searched == null) {
			if (term.arity() == 0) {
				// Term is integer or string atom, simply create a storage trie to store atom
				searched = new SingleStorageLeaf(start, label, term); // Store term(s) at leaf node
				addChild(start, searched);
				return searched;
			} else {
				
				// We need nodes for a compound term (no nodes at all exist), create functor and argument nodes
				searched = new MultiTrie(start, label, null); // DON'T store terms also at functor node
				addChild(start, searched);
				prevArg = searched;
				for (int i = 0; i < term.arity(); i++) {
					Term arg = term.arg(i);
					
					if (arg.isCompoundTerm()) {
						
						MultiTrie multi = new MultiTrie(prevArg, label(arg), null);
						addChild(prevArg, multi);
						
						 // Recursion: We open another dimension. Watch out for lists!
						insert(arg, multi.getSubtrie());
						
						// If this is the last argument we store the term here, otherwise proceed
						if (i == term.arity() - 1) {
							addTerm(multi, term);
							return multi;
						} else {
							prevArg = multi;
						}
						
					} else {
						
						// Term is integer or string atom
						if (i == term.arity() - 1) {
							// We must store a term here at the last argument
							SingleStorageLeaf storageTrie = new SingleStorageLeaf(prevArg, AtomLabel.AtomLabel(atom(arg)), term);
							addChild(prevArg, storageTrie);
							return storageTrie;
						} else {
							InnerNode trie = new InnerNode(prevArg, AtomLabel.AtomLabel(atom(arg)));
							addChild(prevArg, trie);
							prevArg = trie;
						}
					}
				}
			}
			
		} else {
			
			// The searched child exists
			if (term.arity() == 0) {
				// Term is integer or string atom (and here MUST be a storage trie)
				addTerm(searched, term);
				return searched;
			} else {
				
				// We have a compound term, at least the functor node exists and some argument nodes, but not necessarily the appropriate for this term
				prevArg = searched;
				//searched.addTerm(term); // DON'T store all possible queries unless we want to run out of memory
				for (int i = 0; i < term.arity(); i++) {
					Term arg = term.arg(i);
					
					if (arg.isCompoundTerm()) {
						
						// Argument is compound term, search for an appropiate child
						Label argLabel = label(arg);
						Trie searchedChild = getChild(prevArg, argLabel);
						if (searchedChild == null) {
							// We don't have an appropriate child, create and add one
							searchedChild = new MultiTrie(prevArg, argLabel, null);
							addChild(prevArg, searchedChild);
						}
						
						// Recursion: We open another dimension to store the term part. Watch out for lists!
						insert(arg, searchedChild.getSubtrie());
						
						// If this is the last argument we store the term here
						if (i == term.arity() - 1) {
							addTerm(searchedChild, term);
							return searchedChild;
						} else {
							prevArg = searchedChild;
						}
						
					} else {
						
						// Argument is an integer or string atom, search for an appropiate child
						Label argLabel = label(arg);
						Trie searchedChild = getChild(prevArg, argLabel);
						
						if (i == term.arity() - 1) {
							
							// This is the last argument, we store the term here, otherwise proceed
							if (searchedChild == null) {
								// We don't have an appropriate child, create and add one
								searchedChild = new SingleStorageLeaf(prevArg, argLabel, term);
								addChild(prevArg, searchedChild);
							} else {
								assert searchedChild instanceof SingleStorageLeaf : "Storage trie expected";
								addTerm(searchedChild, term);
							}
							
							return searchedChild;
						} else {
							// Some inner argument
							if (searchedChild == null) {
								searchedChild = new InnerNode(prevArg, argLabel);
								addChild(prevArg, searchedChild);
							}
							
							prevArg = searchedChild;
						}
					}
				}
			}
		}
		
		assert false : "We shouldn't be here, Sam.";
		return null;
	}
	
	@Override
	public Iterator<Term> iterator(InnerNode start) {
		return new MultiTrieTermIterator(start);
	}

	@Override
	public Iterator<Term> iterator(InnerNode start, Term query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(Term term, InnerNode start) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public String toString() {
		return "multi";
	}
	
	@Override
	public void addChild(InnerNode parent, InnerNode child) {
		// Append to the head of the children list
		child.setNextSibling(parent.getFirstChild());
		parent.setFirstChild(child);
		
		// Root (which uses maps anyway) or another node which may use a map (or not)?
		if (parent.getParent() == null) {
			parent.getMap().put(child.getLabel(), child);
		} else {
			// Transform to hash trie node?
			//parent.setChildrenNumber(parent.getChildrenNumber() + 1);
			int childrenNumber = countChildren(parent);
			if (childrenNumber == mapThreshold) {
				
				InnerHashNode hashTrie = new InnerHashNode(parent.getParent(), parent.getLabel(), null);
				replace(parent, hashTrie);
				
				// Fill the hash map as replace() doesn't care for this
				InnerNode trie = hashTrie.getFirstChild();
				while (trie != null) {
					hashTrie.getMap().put(trie.getLabel(), trie);
					trie = trie.getNextSibling();
				}
				
			} else if (childrenNumber > mapThreshold) {
				assert childrenNumber > mapThreshold : "Attempt to using map before threshold is reached: " + parent + " with " + childrenNumber + " children";
				parent.getMap().put(child.getLabel(), child);
			}
		}
		
		
	}
	
	@Override
	public InnerNode getChild(InnerNode parent, Label label) {
		if (parent.getMap() != null) {
			return parent.getMap().get(label);
		} else {
			InnerNode child = parent.getFirstChild();
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
	
	private InnerNode insert(Term term, InnerNode start, int dimension) {
		current = start;
		System.out.println("dimension=" + dimension);
		
		Label label = label(term);
		
		// The previous argument (on the same level/dimension), i.e., the parent
		InnerNode prevArg;
		
		InnerNode searched = getChild(start, label);
		if (searched == null) {
			if (term.arity() == 0) {
				// Term is integer or string atom, simply create a storage trie to store atom
				searched = new SingleStorageLeaf(start, label, term); // Store term(s) at leaf node
				addChild(start, searched);
				return searched;
			} else {
				
				// We need nodes for a compound term (no nodes at all exist), create functor and argument nodes
				searched = new MultiTrie(start, label, term); // Store term(s) at functor node (e.g., f/2 stores everything that satisfies f(X,Y))
				addChild(start, searched);
				prevArg = searched;
				for (int i = 0; i < term.arity(); i++) {
					Term arg = term.arg(i);
					
					if (arg.isCompoundTerm()) {
						
						MultiTrie multi = new MultiTrie(prevArg, label(arg), arg);
						addChild(prevArg, multi);
						
						 // Recursion: We open another dimension. Watch out for lists!
						insert(arg, multi.getSubtrie(), dimension + 1);
						
						// If this is the last argument we store the term here, otherwise proceed
						if (i == term.arity() - 1) {
							addTerm(multi, term);
							return multi;
						} else {
							prevArg = multi;
						}
						
					} else {
						
						// Term is integer or string atom
						if (i == term.arity() - 1) {
							// We must store a term here at the last argument
							SingleStorageLeaf storageTrie = new SingleStorageLeaf(prevArg, AtomLabel.AtomLabel(atom(arg)), term);
							addChild(prevArg, storageTrie);
							return storageTrie;
						} else {
							InnerNode trie = new InnerNode(prevArg, AtomLabel.AtomLabel(atom(arg)));
							addChild(prevArg, trie);
							prevArg = trie;
						}
					}
				}
			}
			
		} else {
			
			// The searched child exists
			if (term.arity() == 0) {
				// Term is integer or string atom (and here MUST be a storage trie)
				addTerm(searched, term);
				return searched;
			} else {
				
				// We have a compound term, at least the functor node exists and some argument nodes, but not necessarily the appropriate for this term
				prevArg = searched;
				addTerm(searched, term);
				for (int i = 0; i < term.arity(); i++) {
					Term arg = term.arg(i);
					
					if (arg.isCompoundTerm()) {
						
						// Argument is compound term, search for an appropiate child
						Label argLabel = label(arg);
						InnerNode searchedChild = getChild(prevArg, argLabel);
						if (searchedChild == null) {
							// We don't have an appropriate child, create and add one
							searchedChild = new MultiTrie(prevArg, argLabel, arg);
							addChild(prevArg, searchedChild);
						}
						
						// Recursion: We open another dimension to store the term part. Watch out for lists!
						insert(arg, prevArg.getSubtrie(), dimension + 1);
						
						// If this is the last argument we store the term here
						if (i == term.arity() - 1) {
							addTerm(searchedChild, term);
							return searchedChild;
						} else {
							prevArg = searchedChild;
						}
						
					} else {
						
						// Argument is an integer or string atom, search for an appropiate child
						Label argLabel = label(arg);
						InnerNode searchedChild = getChild(prevArg, argLabel);
						
						if (i == term.arity() - 1) {
							
							// This is the last argument, we store the term here, otherwise proceed
							if (searchedChild == null) {
								// We don't have an appropriate child, create and add one
								searchedChild = new SingleStorageLeaf(prevArg, argLabel, term);
								addChild(prevArg, searchedChild);
							} else {
								assert searchedChild instanceof SingleStorageLeaf : "Storage trie expected";
								addTerm(searchedChild, term);
							}
							
							return searchedChild;
						} else {
							// Some inner argument
							if (searchedChild == null) {
								searchedChild = new InnerNode(prevArg, argLabel);
								addChild(prevArg, searchedChild);
							}
							
							prevArg = searchedChild;
						}
					}
				}
			}
		}
		
		assert false : "We shouldn't be here, Sam.";
		return null;
	}
		
	private Atom atom(Term term) {
		assert term.isIntegerAtom() || term.isStringAtom() || term instanceof EmptyList0 : "Term is not an atom or empty list: " + term;
		
		if (term.isIntegerAtom()) {
			return term.asIntegerAtom();
		} else {
			return term.functor();
		}
	}
	
	/**
	 * Creates the appropriate label (functor label or atom label) for the specified term.
	 * 
	 * @param term The term for which a label is wanted.
	 * @return The label for the term.
	 */
	private Label label(Term term) {
		if (term.isCompoundTerm()) {
			return FunctorLabel.FunctorLabel(term.functor(), term.arity());
		} else {
			return AtomLabel.AtomLabel(atom(term.functor()));
		}
	}
	
	private int countChildren(InnerNode parent) {
		int num = 0;
		InnerNode child = parent.getFirstChild();
		while (child != null) {
			num++;
			child = child.getNextSibling();
		}
		
		return num;
	}

	@Override
	public Iterator<Term> iterator(Trie start, Term query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(Term term, Trie root) {
		// TODO Auto-generated method stub
		
	}
}
