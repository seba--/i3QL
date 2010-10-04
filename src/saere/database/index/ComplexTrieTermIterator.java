package saere.database.index;

import java.util.Iterator;

import saere.Term;

public class ComplexTrieTermIterator extends TrieTermIterator implements Iterator<Term> {
	private TermStack stack;
	private Iterator<Term> subiterator;
	
	/**
	 * Creates a new trie iterator that starts from <tt>start</tt> and 
	 * returns only terms that match the term represented by <tt>terms</tt>.
	 * 
	 * @param start The start trie, e.g., a functor.
	 * @param terms A query represented by an array of terms (atoms/variables).
	 */
	public TrieTermIterator(Trie start, Term ... terms) {
		super();
		this.start = start;
		current = start;
		
		// break down terms to atoms/variables
		List<Term> list = new LinkedList<Term>();
		//list.add(label.getLabel(0)); // functor label
		for (Term term : terms) {
			Term[] termTerms = flatten(term);
			for (Term termTerm : termTerms) {
				list.add(termTerm);
			}
		}
			
		stack = new TermStack(list.toArray(new Term[0]));
		
		// find first next
		// FIXME don't skip the first node!
		findNext(); 
	}
	
	@Override
	protected void findNext() {
		next = null;
		
		// list processing mode
		if (list != null) {
			next = list.getTerm();
			list = list.getNext();
		} else { // normal mode
			
			// as long as we haven't found a new next and are not at an end point, i.e., current is null
			while (next == null && current != null) { // (or break)
				
				// skip the root
				if (current.isRoot()) {
					current = current.getFirstChild();
					continue;
				}
				
				boolean subiter = false;
				/*
				Term first = stack.peek(); // get next atom/variable
				if ((stack.size() == 0 || (stack.size() == 1 && (match(current.getLabel(), stack) == 1)))) {
					subiter = true;
				}
				*/
				if (subiterator != null)
					subiter = true;
				
				// subiteration mode
				if (subiter) {
					//System.out.println("!");
					
					if (subiterator == null) {
						subiterator = new SimpleTrieTermIterator(current);
					}
					
					if (subiterator.hasNext()) {
						next = subiterator.next();
						break; // = return
					} else {
						subiterator = null;
						//goRight();
						nextNode();
					}
				} else { // normal mode (non-subiteration mode)
					
					// FIXME X match possibilities (cf. ComlpexTermInserter)
					// FIXME Test query [f, a]...
					
					// how "much" does the current label matches with the current stack (state)
					int match = match(current.getLabel(), stack);
					int labelLength = current.labelLength();
					
					if (match > 0) {
						
						// FIXME Problem with inner node subiterators (how to go on after iteration?)
						
						// 1. end point reached? --> subiterator(s)
						if (match == stack.size()) {
							
							/*
							// try to "return" the current trie's term list
							if (current.termList != null) {
								list = current.termList;
								next = list.getTerm();
								list = list.getNext();
								break; // = return
							}
							*/
							
							// check if leaf, then no real subiter is necessary
							
							if (subiterator == null) {
								subiterator = new SimpleTrieTermIterator(current);
							}
							
							if (subiterator.hasNext()) {
								next = subiterator.next();
								break; // = return
							} else {
								subiterator = null;
								//goRight();
								nextNode();
							}
							
						} else if (match == labelLength) { // match < stack.size()
							
							//stack.pop(labelLength);
							nextNode();
							
						} else { // match < labelLength
							goRight(); // no full match, go right
						}
						
					} else {
						goRight();  // no match, go right
					}
				}
			}
		}
	}
	
	@Override
	protected void goUp() {
		stack.back(current.labelLength()); // new: labelLength()
		super.goUp();
	}
	
	@Override
	protected void goDown() {
		stack.pop(current.labelLength()); // new: labelLength()
		super.goDown();
	}
	
	// doesn't work with the root (since its label is null)
	protected int match(Label label, TermStack stack) {
		if (stack.size() == 0)
			return 0;
		
		// handle variables
		if (isFreeVar(stack.peek())) {
			Term[] array = stack.asArray();
			int i = 0;
			Term peeked = array[0];
			while (isFreeVar(peeked) && i < array.length) {
				peeked = array[i];
				i++;
			}
			return i > label.length() ? label.length() : i;
		} else {
			return label.match(stack.asArray());
		}
	}
}
