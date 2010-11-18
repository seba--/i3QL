package saere.database.index;

import java.util.IdentityHashMap;

/**
 * An extension of a {@link Trie} that also uses hash maps.
 * 
 * @author David Sullivan
 * @version 0.1, 11/9/2010
 */
public class HashTrie extends Trie {
	
	private IdentityHashMap<Label, Trie> map;
	private Trie lastChild;	
	
	public HashTrie(Trie parent, Label label, Trie lastChild) {
		super(parent, label);
		this.lastChild = lastChild;
		map = new IdentityHashMap<Label, Trie>();
	}
	
	@Override
	public boolean hashes() {
		return true;
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
