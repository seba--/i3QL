package saere.database.index;

import saere.Term;

/**
 * Common abstract base class for term flatteners. A {@link TermFlattener} 
 * defines how a {@link Term} is flattend (&quot;hashed&quot;) for storage and 
 * retrieval.
 * 
 * @author David Sullivan
 * @version 0.4, 11/9/2010
 */
public abstract class TermFlattener {
	
	/**
	 * Flattens a term. That is, creates a non-nested (no compound terms), 
	 * array-like representation of the specified term.
	 * 
	 * @param term The term to flatten.
	 * @return The flattened term representation.
	 */
	public abstract LabelStack flatten(Term term);
	
	/**
	 * The maximum length for flattened terms. A value smaller than 1 will turn 
	 * this behavior off.<br/>
	 * <br/>
	 * <b>Note that the maximum length for inserted terms and queries must be 
	 * the same. Otherwise some terms might not be found by a query.</b>
	 */
	protected int maxLength = 0;
	
	/**
	 * Sets the maximum length for flattened terms.
	 * 
	 * @param maxLength The new maximum length.
	 * @see #maxLength
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	
	/**
	 * Gets the maximum length for flattened terms.
	 * 
	 * @return The maximum length.
	 * @see #maxLength
	 */
	public int getMaxLength() {
		return maxLength;
	}
}
