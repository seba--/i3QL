package saere.database;

import saere.Atom;
import saere.Term;
import saere.database.index.Matcher;
import saere.database.index.TermFlattener;

/**
 * A class for filtering terms that match a query. (As for use with plain 
 * lists, for example.)
 * 
 * @author David Sullivan
 * @version 0.1, 10/6/2010
 */
public class TermFilter {
	
	private final Term[] query;
	private final TermFlattener flattener;
	
	/**
	 * Creates a new {@link TermFilter} with the specified query and a 
	 * {@link TermFlattener}. The latter must be the same as the one that 
	 * was used by creating the query.
	 * 
	 * @param query The query.
	 * @param flattener The term flattener.
	 */
	public TermFilter(Term[] query, TermFlattener flattener) {
		this.query = query;
		this.flattener = flattener;
	}
	
	/**
	 * Checks wether the specified {@link Term} is <i>allowed</i>, i.e., it
	 * satisfies the query.
	 * 
	 * @param term The term to check.
	 * @return <tt>true</tt> if the terms matches the query of this instance.
	 */
	public boolean allow(Term term) {
		Atom[] flattened = flattener.flatten(term);
		return query.length == Matcher.match(flattened, query);
	}
}
