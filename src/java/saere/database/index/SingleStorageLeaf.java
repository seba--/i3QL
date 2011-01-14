package saere.database.index;

import saere.Term;

/**
 * A trie that can store a term.
 * 
 * @author David Sullivan
 * @version 0.1, 11/24/2010
 */
public final class SingleStorageLeaf extends Trie {
	
	private Label label;
	private Trie parent;
	private Trie nextSibling;
	private Term term;
	
	public SingleStorageLeaf(Trie parent, Label label, Term term) {
		this.parent = parent;
		this.label = label;
		this.term = term;
	}
	
	@Override
	public Label getLabel() {
		return label;
	}
	
	@Override
	public void setLabel(Label label) {
		this.label = label;
	}
	
	@Override
	public Trie getParent() {
		return parent;
	}
	
	@Override
	public void setParent(Trie parent) {
		assert this != parent : "Parent is the same as this";
		this.parent = parent;
	}
	
	@Override
	public Trie getNextSibling() {
		return nextSibling;
	}
	
	@Override
	public void setNextSibling(Trie nextSibling) {
		assert this != nextSibling : "Next sibling is the same as this";
		this.nextSibling = nextSibling;
	}

	@Override
	public boolean isSingleStorageLeaf() {
		return true;
	}
	
	@Override
	public Term getTerm() {
		return term;
	}
	
	@Override
	public void setTerm(Term term) {
		this.term = term;
	}
}
