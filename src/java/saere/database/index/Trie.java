package saere.database.index;

import java.util.IdentityHashMap;

import saere.Term;

/**
 * Common abstract base class for tries.
 * 
 * @author David Sullivan
 * @version 0.1, 11/24/2010
 */
public abstract class Trie {
	
	public boolean isRoot() {
		return false;
	}
	
	public boolean isInnerNode() {
		return false;
	}
	
	public Label getLabel() {
		return null;
	}
	
	public void setLabel(Label label) {
		throw new UnsupportedOperationException("Trie type " + this.getClass().getName() + " has no label");
	}
	
	public Trie getParent() {
		return null;
	}
	
	public void setParent(Trie parent) {
		throw new UnsupportedOperationException("Trie type " + this.getClass().getName() + " has no parent");
	}
	
	public Trie getFirstChild() {
		return null;
	}
	
	public void setFirstChild(Trie firstChild) {
		throw new UnsupportedOperationException("Trie type " + this.getClass().getName() + " has no first child");
	}
	
	public Trie getNextSibling() {
		return null;
	}
	
	public void setNextSibling(Trie nextSibling) {
		throw new UnsupportedOperationException("Trie type " + this.getClass().getName() + " has no first child");
	}
	
	public boolean isHashNode() {
		return false;
	}
	public IdentityHashMap<Label, Trie> getMap() {
		return null;
	}
	
	public void setMap(IdentityHashMap<Label, Trie> map) {
		throw new UnsupportedOperationException("Trie type " + this.getClass().getName() + " has no map");
	}
	
	public Trie getLastChild() {
		return null;
	}
	
	public void setLastChild(Trie lastChild) {
		throw new UnsupportedOperationException("Trie type " + this.getClass().getName() + " has no last child");
	}
	
	public boolean isSingleStorageLeaf() {
		return false;
	}
	
	public Term getTerm() {
		return null;
	}
	public void setTerm(Term term) {
		throw new UnsupportedOperationException("Trie type " + this.getClass().getName() + " has no term");
	}
	
	public boolean isMultiStorageLeaf() {
		return false;
	}
	
	public TermList getTerms() {
		return null;
	}
	
	public void setTerms(TermList termList) {
		throw new UnsupportedOperationException("Trie type " + this.getClass().getName() + " has no term list");
	}
	
	@Override
	public String toString() {
		return hashCode() + ":" + (getLabel() == null ? "<root>" : getLabel().toString());
	}
}
