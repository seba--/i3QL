package saere.database.index;

import java.util.Iterator;

import saere.Atom;
import saere.Term;

/**
 * A {@link TrieBuilder} for <i>Complex Term Insertion</i> (CTI). 
 * {@link Trie}s created with this {@link TrieBuilder} have as much 
 * {@link Trie} nodes as required, but not more. However, the insertion process 
 * is a bit more <i>complex</i> (and may take more time).
 * 
 * @author David Sullivan
 * @version 0.6, 10/18/2010
 */
public class ComplexTrieBuilder extends TrieBuilder<Atom[]> {
	
	@Override
	public Trie<Atom[]> insert(Term term, Trie<Atom[]> start) {
		current = start;
		InsertStack stack = new InsertStack(flattener.flatten(term));
		Trie<Atom[]> insertionNode = null; // the trie node where the specified term will be added
		int match;
		while (insertionNode == null) {
			
			if (current.getParent() == null) { // root 
				
				if (current.getFirstChild() == null) { // create the very first node and add term directly
					current.setFirstChild(new Trie<Atom[]>(current, stack.asArray()));
					current.getFirstChild().setTerm(term);
					insertionNode = current.getFirstChild();
				} else { // move to child
					current = current.getFirstChild(); // set current for next insert()
				}

			} else { // !root
				
				// how "much" does the current label matches with the current stack (state)
				match = Matcher.match(current.getLabel(), stack.array(), stack.position());
				
				if (match == current.getLabel().length) { // complete match -> insert here or as child
					
					if (match < stack.size()) {
						
						// XXX FIXME
						
						// insert as child
						stack.pop(match);
						if (current.getFirstChild() == null) { // create first child and add term directly
							current.setFirstChild(new Trie<Atom[]>(current, stack.asArray()));
							current.getFirstChild().setTerm(term);
							insertionNode = current.getFirstChild();
						} else { // move to child
							current = current.getFirstChild();
						}
						
					} else { // match == stack.size(), insert here...
						
						if (current.hasTermList()) { // Already a node with collision
							current.setTerms(new TermList(term));
						} else if (current.getTerm() != null) { // Normal node, transform...
							Trie<Atom[]> trie = new TrieWithCollision<Atom[]>(current.getParent(), current.getLabel());
							replace(current, trie);
							if (current.hasTermList()) {
								current.setTerms(null); // FIXME And transform current 'back'!
							} else {
								current.setTerm(null);
							}
							current = trie;
						} else { // Normal, empty node
							current.setTerm(term);
						}
						
						insertionNode = current;
					}
					
				} else if (match > 0) { // partial match
					
					// split...
					Atom[] newLabel = split(current.getLabel(), match - 1);
					Trie<Atom[]> mediator;
					
					if (current.hasTermList()) { // Collision
						mediator = new TrieWithCollision<Atom[]>(current, newLabel);
						mediator.setTerms(current.getTerms());
						mediator.setTerms(new TermList(term));
						current.setTerms(null); // FIXME And transform node 'back'!
					} /*else if (current.getTerm() != null) { // Normal case
						mediator = new TrieWithCollision<Atom[]>(current, newLabel);
						mediator.setTermList(new TermList(current.getTerm()));
						mediator.setTermList(new TermList(term));
						current.setTerm(null);
					} */else {
						mediator = new Trie<Atom[]>(current, newLabel);
						mediator.setTerm(current.getTerm());
						current.setTerm(null);
					}
					
					// insert mediator
					if (current.getFirstChild() != null) {
						mediator.setFirstChild(current.getFirstChild());
						
						// set mediator as parent for all children
						Trie<Atom[]> child = mediator.getFirstChild();
						while (child != null) {
							child.setParent(mediator);
							child = child.getNextSibling();
						}
					}
					current.setFirstChild(mediator);
					
					// and go on...
					stack.pop(match);
					current = mediator;
					
				} else { // no match
					if (current.getNextSibling() == null) { // create first next sibling and add term directly
						current.setNextSibling(new Trie<Atom[]>(current.getParent(), stack.asArray()));
						current.getNextSibling().setTerm(term);
						insertionNode = current.getNextSibling();
					} else { // move to next sibling
						current = current.getNextSibling();
					}
				}
			}
			
		}
		
		return insertionNode;
	}

	@Override
	public boolean remove(Term term, Trie<Atom[]> start) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Iterator<Term> iterator(Trie<Atom[]> start, Term[] query) {
		return new ComplexTrieTermIterator(start, new QueryStack(flattener.flattenForQuery(query)));
	}
	
	@Override
	public String toString() {
		return flattener.toString() + "-complex";
	}
}
