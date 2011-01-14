package saere.database.predicate;

import saere.database.Database;

public final class MethodExceptions2 extends DatabasePredicate {

	protected MethodExceptions2(String functor, int arity, Database database) {
		super("method_exceptiosn", 2, database);
	}
}
