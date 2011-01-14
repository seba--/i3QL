package saere.database.index;

import java.util.IdentityHashMap;

/**
 * An extension of an {@link InnerNode} that uses a hash map in addition.
 * 
 * @author David Sullivan
 * @version 0.1, 11/9/2010
 */
public class InnerHashNode extends InnerNode {
	
	private IdentityHashMap<Label, Trie> map;
	private Trie lastChild;	
	
	public InnerHashNode(Trie parent, Label label, Trie lastChild) {
		super(parent, label);
		this.lastChild = lastChild;
		map = new IdentityHashMap<Label, Trie>();
	}
	
	@Override
	public boolean isHashNode() {
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
