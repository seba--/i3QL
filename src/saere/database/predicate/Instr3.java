package saere.database.predicate;

import saere.database.Database;

/**
 * Example for a rather short predicate with a very high frequency.
 * 
 * @author David Sullivan
 * @version 0.4, 10/12/2010
 */
public final class Instr3 extends DatabasePredicate {
	
	public Instr3(Database database) {
		super("instr", 3, database);
	}	
}
