package saere.database.index;

import saere.Term;

/**
 * An extension of a {@link Trie} that can store terms <b>and</b> uses hash 
 * maps. (This should be required on rare occasions only.)
 * 
 * @author David Sullivan
 * @version 0.1, 11/9/2010
 */
// Multiple inheritance would be nice here...
public class StorageHashTrie extends HashTrie {

	private TermList terms;
	
	public StorageHashTrie(Trie parent, Label label, Trie lastChild, Term term) {
		super(parent, label, lastChild);
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
