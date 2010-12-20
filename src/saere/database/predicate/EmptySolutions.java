package saere.database.predicate;

import saere.Solutions;

/**
 * Represents a solution set that has no solutions. Hence,
 * {@link EmptySolutions#next()} will always return <tt>false</tt>.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public class EmptySolutions implements Solutions {
	
	private static final EmptySolutions INSTANCE = new EmptySolutions();
	
	private EmptySolutions() {
		
	}
	
	public static EmptySolutions getInstance() {
		return INSTANCE;
	}
	
	public boolean next() {
		return false;
	}

	@Override
	public boolean choiceCommitted() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abort() {
		throw new UnsupportedOperationException();
	}
}
