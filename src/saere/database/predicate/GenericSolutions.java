package saere.database.predicate;

import saere.Solutions;
import saere.Term;
import saere.database.DatabaseAdapter;

public final class GenericSolutions {
	
	private GenericSolutions() { /* empty */ }

	public static Solutions forArity(int arity, DatabaseAdapter adapter, Term query) {
		switch (arity) {
			case 1: return new SolutionsWithArity1(adapter, query);
			case 2: return new SolutionsWithArity2(adapter, query);
			case 3: return new SolutionsWithArity3(adapter, query);
			case 4: return new SolutionsWithArity4(adapter, query);
			case 10: return new SolutionsWithArity10(adapter, query);
			case 11: return new SolutionsWithArity11(adapter, query);
			case 15: return new SolutionsWithArity15(adapter, query);
			default: throw new UnsupportedOperationException("No generic solutions for arity" + arity);
		}
	}
	
	public static Solutions forArityNoCollision(int arity, DatabaseAdapter adapter, Term query) {
		switch (arity) {
			case 1: return new SolutionsWithArity1NoCollision(adapter, query);
			case 2: return new SolutionsWithArity2NoCollision(adapter, query);
			case 3: return new SolutionsWithArity3NoCollision(adapter, query);
			case 4: return new SolutionsWithArity4NoCollision(adapter, query);
			case 10: return new SolutionsWithArity10NoCollision(adapter, query);
			case 11: return new SolutionsWithArity11NoCollision(adapter, query);
			case 15: return new SolutionsWithArity15NoCollision(adapter, query);
			default: throw new UnsupportedOperationException("No generic solutions for arity" + arity);
		}
	}
}
