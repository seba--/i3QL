package saere.database.predicate;

import saere.database.Database;

public final class AnnotationDefault2 extends DatabasePredicate {

	protected AnnotationDefault2(Database database) {
		super("annotation_default", 2, database);
	}
}
