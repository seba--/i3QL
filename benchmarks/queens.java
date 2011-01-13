//package predicates;

import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.not_attack2Factory;
import predicates.not_attack3Factory;
import predicates.queens2Factory;
import predicates.queens3Factory;
import predicates.range3Factory;
import predicates.select3Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Variable;

public class queens {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		not_attack2Factory.registerWithPredicateRegistry(registry);
		not_attack3Factory.registerWithPredicateRegistry(registry);
		queens2Factory.registerWithPredicateRegistry(registry);
		queens3Factory.registerWithPredicateRegistry(registry);
		range3Factory.registerWithPredicateRegistry(registry);
		select3Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) {
//		queens20Findfirst();
		queens8Findall();
//		queens1to18();
		queens1to28();
	}

	private static void queens1to28() {
		long duration = 0l;
		for (int i = 1; i <= 28; i++) {
			Variable solution = new Variable();
			StringAtom time = StringAtom.get("time");
			StringAtom queens = StringAtom.get("queens");
			Goal s = compoundTerm(time, compoundTerm(queens, atomic(i), solution)).call();
			long startTime = System.nanoTime();
			boolean succeeded = s.next();
			long last_duration = System.nanoTime() - startTime;

			Utils.writeToPerformanceLog("queens " + i + " finished in: "
					+ new Double(last_duration / 1000.0 / 1000.0 / 1000.0) + "\n");
			duration += last_duration;
			if (succeeded) {
				System.out.println(i + " => " + solution.toProlog() + "\n");
			} else {
				System.out.println();
			}
		}
		Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
		System.out.printf("Finished in %10.4f secs.\n", time);
		Utils.writeToPerformanceLog("queens (1..28) finished in: " + time + "\n");
	}

	private static void queens1to18() {
		for (int i = 1; i <= 18; i++) {
			Variable solution = new Variable();
			StringAtom time = StringAtom.get("time");
			StringAtom queens = StringAtom.get("queens");
			Goal s = compoundTerm(time, compoundTerm(queens, atomic(i), solution)).call();
			if (s.next()) {
				System.out.println(i + " => " + solution.toProlog());
			} else {
				System.out.println();
			}
		}
	}

	private static void queens8Findall() {
		for (int i = 0; i < 200; i++) {
			long startTime = System.nanoTime();
			int counter = 0;
			Variable solution = new Variable();
			StringAtom queens = StringAtom.get("queens");
			Goal s = compoundTerm(queens, atomic(8), solution).call();
			while (s.next()) {
				counter++;
			}
			long last_duration = System.nanoTime() - startTime;

			Utils.writeToPerformanceLog("run \t" + i + "\t of queens 8 (all " + counter
					+ " solutions) finished in: \t" + last_duration / 1000 + "\tmicrosecs.\n");

		}
	}

	private static void queens20Findfirst() throws Error {
		for (int i = 0; i < 25; i++) {
			long startTime = System.nanoTime();
			Variable solution = new Variable();
			StringAtom queens = StringAtom.get("queens");
			Goal s = compoundTerm(queens, atomic(20), solution).call();
			if (!s.next()) {
				throw new Error();
			}
			long last_duration = System.nanoTime() - startTime;

			Utils.writeToPerformanceLog("run \t" + i
					+ "\t of queens 20 (first solution) finished in: \t" + last_duration / 1000
					/ 1000 + "\tmillisecs.\n");

		}
	}

}
