package saere.database.index;

import java.util.IdentityHashMap;

/**
 * A trie root. Can use hash maps.
 * 
 * @author David Sullivan
 * @version 11/24/2010
 */
public final class Root extends Trie {
	
	private Trie firstChild;
	private Trie lastChild;
	private IdentityHashMap<Label, Trie> map;
	
	@Override
	public boolean isRoot() {
		return true;
	}
	
	public Trie getFirstChild() {
		return firstChild;
	}
	
	public void setFirstChild(Trie firstChild) {
		assert this != firstChild : "First child is the same as this";
		this.firstChild = firstChild;
	}
	
	@Override
	public Trie getLastChild() {
		return lastChild;
	}
	
	@Override
	public void setLastChild(Trie lastChild) {
		this.lastChild = lastChild;
	}
	
	@Override
	public IdentityHashMap<Label, Trie> getMap() {
		return map;
	}
	
	@Override
	public void setMap(IdentityHashMap<Label, Trie> map) {
		this.map = map;
	}
}
