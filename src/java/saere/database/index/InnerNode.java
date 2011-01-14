package saere.database.index;



/**
 * An inner trie node.
 * 
 * @author David Sullivan
 * @version 0.1, 24/11/2010
 */
public class InnerNode extends Trie {
	
	private Label label;
	private Trie parent;
	private Trie firstChild;
	private Trie nextSibling;
	
	public InnerNode(Trie parent, Label label) {
		assert parent != null : "Parent is null";
		assert label != null : "Label is null";
		
		setParent(parent);
		setLabel(label);
	}
	
	@Override
	public boolean isInnerNode() {
		return true;
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
	public Trie getFirstChild() {
		return firstChild;
	}
	
	@Override
	public void setFirstChild(Trie firstChild) {
		assert this != firstChild : "First child is the same as this";
		this.firstChild = firstChild;
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
	public Label getLabel() {
		return label;
	}
	
	@Override
	public void setLabel(Label label) {
		this.label = label;
	}
}
