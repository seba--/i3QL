package saere.database.index;

import saere.Term;

/**
 * An extension of a {@link Trie} that can store terms.
 * 
 * @author David Sullivan
 * @version 0.1, 11/9/2010
 */
public final class StorageTrie extends Trie {
	
	private TermList terms;
	
	/**
	 * Creates a new storage trie.
	 * 
	 * @param parent The parent of this trie.
	 * @param label The label of this trie.
	 * @param term The term to store.
	 */
	public StorageTrie(Trie parent, Label label, Term term) {
		super(parent, label);
		terms = new TermList(term, null);
	}
	
	@Override
	public void addTerm(Term term) {
		TermList head = new TermList(term, terms);
		terms = head;
	}

	@Override
	public TermList getTerms() {
		return terms;
	}
	
	@Override
	public void setTerms(TermList terms) {
		this.terms = terms;
	}
	
	@Override
	public boolean stores() {
		return true;
	}
}
