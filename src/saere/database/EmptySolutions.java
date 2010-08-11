package saere.database;

import saere.Solutions;

/**
 * Represents a solution set that has no solutions. Hence,
 * {@link EmptySolutions#next()} will always return <tt>false</tt>.
 * 
 * @author David Sullivan
 * @version $Id$
 */
public class EmptySolutions implements Solutions {
	
	public boolean next() {
		return false;
	}
}
