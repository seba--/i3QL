package saere.predicate;

import static saere.term.TermFactory.atom;
import static saere.term.TermFactory.cut;
import static saere.term.TermFactory.and;
import static saere.term.TermFactory.or;
import static saere.term.TermFactory.unify;

import org.junit.Assert;
import org.junit.Test;

import saere.Solutions;
import saere.Term;
import saere.Variable;

public class And2Test {

	@Test
	public void testDeterministicEvaluation() {

		// v = 1, true.

		Variable v = new Variable();
		Term t1 = unify(v, atom(1));
		Term t2 = atom("true");
		Solutions solutions = and(t1, t2).call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 2) {
			solutionsCount++;
		}
		Assert.assertEquals(1, solutionsCount);
	}

	@Test
	public void testNonDeterministicEvaluation() {

		// repeat,true.

		Term t1 = atom("repeat");
		Term t2 = atom("true");
		Solutions solutions = and(t1, t2).call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 1000) {
			solutionsCount++;
		}
		Assert.assertEquals(1000, solutionsCount);
	}

	@Test
	public void testCutREvaluation() {
		// (repeat,true),!.

		Term t1 = atom("repeat");
		Term t2 = atom("true");
		Term t3 = atom("!");
		Term t4 = and(t1, t2);
		Term t5 = and(t4, t3);
		Solutions solutions = t5.call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 2) {
			solutionsCount++;
		}
		Assert.assertEquals(1, solutionsCount);
	}

	@Test
	public void testCutLEvaluation() {

		// (repeat,!),true.
		Term t1 = atom("repeat");
		Term t2 = atom("true");
		Term t3 = atom("!");
		Term t4 = and(t1, t3);
		Term t5 = and(t4, t2);
		Solutions solutions = t5.call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 2) {
			solutionsCount++;
		}
		Assert.assertEquals(1, solutionsCount);
	}

	@Test
	public void testAndOrCutLEvaluation() {

		// (repeat, !, fail ; true).
		Term t1 = atom("repeat");
		Term t2 = cut();
		Term t3 = atom("fail");
		Term t4 = atom("true");
		Term t5 = and(t1, t2);
		Term t6 = and(t5, t3);
		Term t7 = or(t6, t4);
		Solutions solutions = t7.call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 2) {
			solutionsCount++;
		}
		Assert.assertEquals(0, solutionsCount);
	}

	@Test
	public void testTwoSolutions() {

		// repeat,(true;!),true.
		Term t1 = atom("repeat");
		Term t2 = atom("true");
		Term t3 = cut();
		Term t4 = or(t2, t3);
		Term t5 = and(t1, t4);
		Term t6 = and(t5, t2);
		Solutions solutions = t6.call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 10) {
			solutionsCount++;
		}
		Assert.assertEquals(2, solutionsCount);
	}
}
