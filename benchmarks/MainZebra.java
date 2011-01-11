import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.houses1Factory;
import predicates.right_of3Factory;
import predicates.zebra1Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class MainZebra {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		zebra1Factory.registerWithPredicateRegistry(registry);
		houses1Factory.registerWithPredicateRegistry(registry);
		right_of3Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) throws Throwable {

		final Variable result = new Variable();
		final Term t = compoundTerm(StringAtom.get("time"),
				compoundTerm(atomic("zebra"), result));
		{
			Goal g = t.call();
			while (g.next()) {
				System.out.println("Result=" + result.toProlog());
			}
		}

		for (int i = 2; i <= 10; i++) {
			long startTime = System.nanoTime();
			Goal g = t.call();
			if (!(g.next() && !g.next())) {
				throw new Error("internal programming error");
			}
			long duration = System.nanoTime() - startTime;
			Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
			All.writeToPerformanceLog("zebra run " + i
					+ " (find all solutions) finished in: " + time + "\n");
		}
	}

}
