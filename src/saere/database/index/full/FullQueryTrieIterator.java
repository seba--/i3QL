package saere.database.index.full;

import java.util.IdentityHashMap;
import java.util.Iterator;

import saere.Atom;
import saere.Term;
import saere.database.index.Matcher;
import saere.database.index.QueryStack;
import saere.database.index.TermFlattener;

/**
 * Trie term iterator that supports queries. A query is expressed by an array of
 * {@link Term}s. For term flattening the same {@link TermFlattener} as in the 
 * {@link FullTrie} class is used.<br/>
 * <br/>
 * <b>This iterator works only with {@link FullTrie}s that have been built with 
 * a {@link SimpleTermInserter}.</b>
 * 
 * @author David Sullivan
 * @version 0.1, 10/26/2010
 */
/*
 * XXX Bad code, but it runs (at least for some small tests)... 
 */
public class FullQueryTrieIterator extends FullTermIterator implements Iterator<Term> {
	
	private final FullTermIterator subiterator;
	
	// XXX A stack or pseudo stack (with pooled variable iterators) would be better...
	// For goDown()/goRight()
	private final IdentityHashMap<FullTrie, VariableIterator> varIters;
	
	// Here a stack would be better too
	// For goUp()
	private final IdentityHashMap<FullTrie, VariableIterator> hooks;
	
	private QueryStack stack;
	private boolean useSubiterator = false;
	
	/**
	 * Creates a new trie iterator that starts from <tt>start</tt> and 
	 * returns only terms that match the term represented by <tt>terms</tt>.
	 * 
	 * @param start The start trie, e.g., a functor.
	 * @param terms A query represented by an array of terms (atoms/variables).
	 */
	public FullQueryTrieIterator(FullTrie start, QueryStack stack) {
		this.start = start;
		current = start;
		this.stack = stack;
		
		// create the one and only instance of the subiterator that'll be used
		subiterator = new FullTermIterator(start);
		
		varIters = new IdentityHashMap<FullTrie, VariableIterator>();
		hooks = new IdentityHashMap<FullTrie, VariableIterator>();
		
		findNext();
	}
	
	@Override
	protected void findNext() {
		next = null;
		
		while (next == null && current != null) { // (or break)
			
			// Skip the root
			if (current.parent == null) {
				current = current.firstChild;
			}
			
			Term peeked = stack.peek();
			boolean match = Matcher.match(current.label, peeked);
			
			// Create new subiterator or return directly?
			if (stack.size() == 1 && match) {
				if (current.firstChild != null && !useSubiterator) {
					useSubiterator = true;
					subiterator.resetTo(current);
				} else { // current.firstChild == null (leaf)
					next = current.term; // would be allowed to be null
					goRight();
					continue;
				}
			}
			
			if (useSubiterator) {
				// Subiteration mode
				if (subiterator.hasNext()) {
					next = subiterator.next();
					break; // = return
				} else {
					// We iterated the subtrie, go right
					useSubiterator = false;
					goRight();
				}
			} else {
				// Normal (non-subiteration) mode	
				if (match) {
					goDown();
				} else {
					// No match, check if there can be a match at all...
					Atom label = (peeked.isStringAtom()) ? peeked.asStringAtom() : peeked.asIntegerAtom();
					FullTrie searched = current.parent.getChild(label);
					if (searched != null && searched.siblingIndex > current.siblingIndex) { // XXX better not be a previous child! <-- can this happen?
						// Jump directly to the matching node and go down
						current = searched;
						goDown();
					} else {
						// There cannot be a match at this level, go up and right
						goUp();
						goRight();
					}
				}
			}
		}
	}
	
	@Override
	public void goUp() {
		VariableIterator iter  = hooks.get(current);
		if (iter != null) {
			hooks.remove(current);
			current = iter.getStart();
			stack.back(); // unpop a single variable for all nodes
		} else {
			super.goUp();
			stack.back();	
		}
	}
	
	@Override
	public void goDown() {
		VariableIterator varIter = null;
		if (stack.peek().isVariable() && !stack.peek().asVariable().isInstantiated()) {
			varIter = varIters.get(current);
			if (varIter == null) {
				varIter = new VariableIterator(current.firstChild, stack.peek().asVariable());
				varIters.put(current, varIter);
				
				if (varIter.hasNext()) {
					// Jump directly to the next node after the compound term (or atom)
					current = varIter.next();
					hooks.put(current, varIter);
					stack.pop();
				} else {
					if (current.firstChild != null) {
						super.goDown();
						stack.pop();
					} else {
						goRight();
					}
				}
			} else {
				System.out.println("Unexpected.");
				if (varIter.hasNext()) {
					current = varIter.next();
					// No stack.pop()
				} else {
					varIters.remove(current); // ?
					if (current.firstChild != null) {
						super.goDown();
						stack.pop();
					} else {
						goRight();
					}
					
				}
			}
		} else {
			super.goDown();
			stack.pop();
		}
	}
	
	@Override
	public void goRight() {
		FullTrie nextSibling = null;
		while (current != null && current != start && nextSibling == null) { // don't go higher up than start
			VariableIterator iter = hooks.get(current);
			if (iter != null && iter.hasNext()) {
				nextSibling = iter.next();
				// !!!
				hooks.remove(current);
				hooks.put(nextSibling, iter);
			} else {
				nextSibling = current.nextSibling;
			}
			
			if (nextSibling == null) {
				goUp();
			}
		}
		
		if (current == null)
			return;
		
		if (current != start) {
			current = nextSibling; // go (directly) right
		} else { // current == start
			current = null; // we treat start as root (and a root has no siblings), me march into the void
		}
	}
}
