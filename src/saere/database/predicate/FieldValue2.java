package saere.database.predicate;

import saere.database.Database;

public final class FieldValue2 extends DatabasePredicate {

	protected FieldValue2(Database database) {
		super("field_value", 2, database);
	}
}
