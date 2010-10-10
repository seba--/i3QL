package saere.database.index;

import saere.Term;

public abstract class TermRemover {

protected Trie current = null;
	
	public Trie getCurrent() {
		return current;
	}
	
	public void setCurrent(Trie current) {
		this.current = current;
	}
	
	public abstract boolean remove(Term term);
}
