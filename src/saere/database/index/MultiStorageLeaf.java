package saere.database.index;

import saere.Term;

public class MultiStorageLeaf extends Trie {
	
	private Label label;
	private Trie parent;
	private Trie nextSibling;
	private TermList terms;
	
	public MultiStorageLeaf(Trie parent, Label label, Term term) {
		this.parent = parent;
		this.label = label;
		this.terms = new TermList(term, null);
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
	public boolean isMultiStorageLeaf() {
		return true;
	}
	
	@Override
	public TermList getTerms() {
		return terms;
	}
	
	@Override
	public void setTerms(TermList termList) {
		this.terms = termList;
	}
}
