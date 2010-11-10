package saere.database.index;


/**
 * Trie term iterator for shallow simple tries that supports queries.<br/>
 * <br/>
 * <b>This iterator works only with {@link Trie}s that have been built with 
 * a {@link SimpleTrieBuilder}.</b>
 * 
 * @author David Sullivan
 * @version 0.4, 11/9/2010
 */
public final class ShallowSimpleQueryIterator extends TermIterator {
	
	private static final VariableLabel FREE_VARIABLE = VariableLabel.VariableLabel();
	
	private final TrieBuilder builder;
	private final LabelStack stack;
	
	/**
	 * Creates a new trie iterator that starts from <tt>start</tt> and 
	 * returns only terms that match the term represented by <tt>terms</tt>.
	 * 
	 * @param start The start trie, e.g., a functor.
	 * @param terms A query represented by an array of terms (atoms/variables).
	 */
	public ShallowSimpleQueryIterator(TrieBuilder builder, Trie start, LabelStack stack) {
		super(start);
		this.builder = builder;
		this.stack = stack;
		findNext();
	}
	
	@Override
	protected void findNext() {
		next = null;
		
		if (list != null) {
			// List processing mode
			next = list.term();
			list = list.next();
		} else {
			// Normal mode, as long as we have nodes left and have no next term (list)
			while (current != null && next == null) {
				if (match()) {
					if (stack.size() == 1 && current.stores()) {
						list = current.getTerms();
						next = list.term();
						list = list.next();
					}
					nextNode();
				} else {
					// No match, check if there can be a match at all.
					// If so, jump directly to the matching sibling.
					Trie searched = builder.getChild(current.getParent(), stack.peek()); // ONLY ONCE!
					if (searched != null) {
						current = searched;
					} else {
						goUp();
						goRight();
					}
				}
			}
		}		
	}
	
	@Override
	protected void goUp() {
		stack.back();
		super.goUp();
	}
	
	@Override
	protected void goDown() {
		stack.pop();
		super.goDown();
	}
	
	@Override
	protected void resetTo(Trie newStart) {
		throw new UnsupportedOperationException();
	}
	
	private boolean match() {
		assert current.getLabel() != null : "Cannot match a root";
		return current.getLabel().sameAs(stack.peek()) || stack.peek() == FREE_VARIABLE;
	}
}
