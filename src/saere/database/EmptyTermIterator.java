package saere.database;

import java.util.Iterator;
import java.util.NoSuchElementException;

import saere.Term;

/**
 * The {@link EmptyTermIterator} singleton represents an empty iterator. 
 * {@link EmptyTermIterator#hasNext()} will always return <tt>false</tt> and 
 * {@link EmptyTermIterator#next()} will aways throw a 
 * {@link NoSuchElementException}.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public class EmptyTermIterator implements Iterator<Term> {

	private static final EmptyTermIterator INSTANCE = new EmptyTermIterator();
	
	private EmptyTermIterator() {
		
	}
	
	public static EmptyTermIterator getInstance() {
		return INSTANCE;
	}
	
	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Term next() {
		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
