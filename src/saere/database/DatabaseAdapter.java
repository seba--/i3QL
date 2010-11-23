package saere.database;

import java.util.Iterator;

import saere.Term;

public interface DatabaseAdapter {
	
	public Iterator<Term> query(Term query);
}
