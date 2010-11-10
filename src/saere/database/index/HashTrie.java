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
	@Deprecated
	public void addChild(Trie child) {
		setChildrenNumber(getChildrenNumber() + 1);
		lastChild.setNextSibling(child);
		lastChild = child;
		map.put(child.getLabel(), child);
	}
	
	@Override
	@Deprecated
	public Trie getChild(Label label) {
		return map.get(label);
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
	protected IdentityHashMap<Label, Trie> getMap() {
		return map;
	}
	
	@Override
	protected void setMap(IdentityHashMap<Label, Trie> map) {
		this.map = map;
	}
}
