package saere.database;

import java.util.List;

import saere.StringAtom;
import saere.Term;

/**
 * Abstract base class for databases.
 * 
 * @author David Sullivan
 * @version $Id$
 */
public abstract class Database {
	
	/**
	 * Adds a term (fact) to the database.
	 * 
	 * @param fact The term to add.
	 */
	public abstract void add(Term fact);
	
	/**
	 * Only to be used if one desires to iterate over all facts for debugging 
	 * or the like.
	 * 
	 * @return A list of all facts.
	 */
	public abstract List<Term> getFacts();
	
	/**
	 * Gets all facts of a predicate that has the specified functor.
	 * 
	 * @param functor The functor of the predicate.
	 * @return A list of facts of a predicate.
	 */
	/*
	 * XXX What about predicates with same functor and different arity?
	 */
	public abstract List<Term> getFacts(StringAtom functor);
	
	public void serialize() {
		throw new UnsupportedOperationException();
	}
	
	public void deserialize() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Fills the database with the facts of the {@link Factbase}.
	 */
	public void fill() {
		List<Term> terms = Factbase.getInstance().getFacts();
		for (Term term : terms) {
			add(term);
		}
	}
}
