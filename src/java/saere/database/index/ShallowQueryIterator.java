package saere.database.index;


/**
 * Trie term iterator for shallow simple tries that supports queries.<br/>
 * <br/>
 * <b>This iterator works only with {@link InnerNode}s that have been built with 
 * a {@link DefaultTrieBuilder}.</b>
 * 
 * @author David Sullivan
 * @version 0.4, 11/9/2010
 */
public class ShallowQueryIterator extends TermIterator {
	
	/** Reference to the label singleton for free variables. */
	private static final VariableLabel FREE_VARIABLE = VariableLabel.VariableLabel();
	
	private final TrieBuilder builder;
	private final LabelStack stack;
	
	/**
	 * Creates a new query iterator for simple shallow tries.
	 * 
	 * @param builder The associated trie builder.
	 * @param start The start node.
	 * @param stack The query as stack.
	 */
	public ShallowQueryIterator(TrieBuilder builder, Trie start, LabelStack stack) {
		this.start = current = start;
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
				
				// Just skip the root
				if (current.getParent() == null) {
					current = current.getFirstChild();
					continue;
				}
				
				if (match()) {
					if (stack.size() == 1/* && current.isMultiStorageLeaf()*/) { // is MultiStorageLeaf is unncessary!
						list = current.getTerms();
						next = list.term();
						list = list.next();
					}
					nextNode();
				} else {
					
					/*
					 * No match, check if there can be a match at all.
					 * If so, jump directly to the matching sibling.
					 * 
					 * However, we must also check if the searched node is a 
					 * right sibling of the current. Otherwise we might jump 
					 * back to a node that was already processed by the 
					 * iterator.
					 * 
					 * As a first child is always checked first, it can never 
					 * be the node we'll want to jump back.
					 */
					
					// Only if current is the first child! That is, we check only once.
					if (current.getParent().getFirstChild() == current) {
						Trie searched = builder.getChild(current.getParent(), stack.peek());
						if (searched != null) {
							current = searched;
						} else {
							goUp();
							goRight();
						}
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
	public void resetTo(Trie newStart) {
		throw new UnsupportedOperationException("Cannot reset a shallow simple query iterator");
	}
	
	private boolean match() {
		assert current.getLabel() != null : "Cannot match a root";
		return current.getLabel().sameAs(stack.peek()) || stack.peek() == FREE_VARIABLE;
	}
}
