package saere.database.index;


/**
 * Represents a (rare) special case of storage trie nodes that can storage 
 * multiple terms.
 * 
 * @author David Sullivan
 * @version 0.1, 10/18/2010
 */
public final class TrieWithCollision<T> extends Trie<T> {

	private TermList termList;
	
	public TrieWithCollision(Trie<T> parent, T label) {
		super(parent, label);
	}
	
	@Override
	public TermList getTermList() {
		return termList;
	}
	
	@Override
	public void setTermList(TermList termList) {
		termList.setNext(this.termList);
		this.termList = termList;
	}
	
	@Override
	public boolean hasTermList() {
		return true;
	}
}
