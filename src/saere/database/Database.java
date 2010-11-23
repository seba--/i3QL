package saere.database;

import java.util.Iterator;
import java.util.List;

import saere.StringAtom;
import saere.Term;

/**
 * Abstract base class for the {@link ListDatabase} and {@link TrieDatabase}.
 * 
 * @author David Sullivan
 * @version 0.3, 10/18/2010
 */
public abstract class Database {
	
	/**
	 * Adds a term (fact) to the database.
	 * 
	 * @param fact The term to add.
	 */
	public abstract void add(Term fact);
	
	/**
	 * Fills the database with the facts of the {@link Factbase}.
	 */
	public void fill() {
		List<Term> terms = Factbase.getInstance().getFacts();
		for (Term term : terms) {
			add(term);
		}
		fillProcessComplete();
	}
	
	/**
	 * This method is called at the end of {@link #fill()} and may be used to 
	 * do some data reorganization after all facts of the {@link Factbase} are 
	 * inserted. This method does nothing by default.
	 */
	protected void fillProcessComplete() { /*empty */ }

	/**
	 * Empties the whole database.
	 */
	public abstract void drop();

	/**
	 * Gets an iterator for all facts.
	 * 
	 * @return An iterator for all facts.
	 */
	public abstract Iterator<Term> terms();
	
	/**
	 * Gets an iterator for the candidate set that was composed by the 
	 * {@link Database} with regards to the specified query.
	 * 
	 * @param query The query.
	 * @return An iterator for the candidates.
	 */
	public abstract Iterator<Term> query(Term query);
	
	public abstract DatabaseAdapter getAdapter(StringAtom functor, int arity);
	
	/** 
	 * True if the database can guarantee the abscence of term collision (if it 
	 * will return only terms that will unify).
	 * 
	 * @return <tt>true</tt> if so.
	 */
	public boolean noCollision() {
		return false;
	}
}
