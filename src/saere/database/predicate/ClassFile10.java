package saere.database.predicate;

import saere.database.Database;

/**
 * Example for a rather short predicate with a low frequency.
 * 
 * @author David Sullivan
 * @version 0.3, 11/22/2010
 */
public final class ClassFile10 extends DatabasePredicate {
	
	public ClassFile10(Database database) {
		super("class_file", 10, database);
	}
}
