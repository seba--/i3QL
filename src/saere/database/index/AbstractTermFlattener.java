package saere.database.index;


/**
 * Abstract base class that based on {@link TermFlattener}. It allows an 
 * additional constraint of maximal length for flattened term representations. 
 * Any extending classes must comply this constraint in their implementations.
 * 
 * @author David Sullivan
 * @version 0.11, 10/14/2010
 */
public abstract class AbstractTermFlattener implements TermFlattener {

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
