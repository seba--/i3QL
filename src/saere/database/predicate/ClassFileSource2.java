package saere.database.predicate;

import saere.database.Database;

public final class ClassFileSource2 extends DatabasePredicate {

	protected ClassFileSource2(Database database) {
		super("class_file_source", 2, database);
	}
}
