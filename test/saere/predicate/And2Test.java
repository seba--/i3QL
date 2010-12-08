package saere.predicate;

import static saere.term.TermFactory.atom;
import static saere.term.TermFactory.compoundTerm;

import org.junit.Assert;
import org.junit.Test;

import saere.Solutions;
import saere.Term;
import saere.Variable;

public class And2Test {

	
	@Test
	public void testDeterministicEvaluation(){
		
		// v = 1, true.
		
		Variable v = new Variable();
		Term t1 = compoundTerm("=", v,atom(1));
		Term t2 = atom("true");
		Solutions solutions = compoundTerm(",", t1,t2).call();
		int solutionsCount = 0;
		while (solutions.next()) {
			solutionsCount ++;
		}
		Assert.assertEquals(1,solutionsCount);
	}
	
	
	@Test
	public void testNonDeterministicEvaluation(){

		// repeat,true.
		
		Term t1 = atom("repeat");
		Term t2 = atom("true");
		Solutions solutions = compoundTerm(",", new Term[]{t1,t2}).call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 1000) {
			solutionsCount ++;
		}
		Assert.assertEquals(1000,solutionsCount);
	}
	
	
	@Test
	public void testCutREvaluation(){
		// (repeat,true),!.
		
		Term t1 = atom("repeat");
		Term t2 = atom("true");
		Term t3 = atom("!");
		Term t4 = compoundTerm(",", new Term[]{t1,t2});
		Term t5 = compoundTerm(",", new Term[]{t4,t3});
		Solutions solutions = t5.call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 1000) {
			solutionsCount ++;
		}
		Assert.assertEquals(1,solutionsCount);
	}
	
	
	@Test
	public void testCutLEvaluation(){
			
		// (repeat,!),true.
		Term t1 = atom("repeat");
		Term t2 = atom("true");
		Term t3 = atom("!");
		Term t4 = compoundTerm(",", new Term[]{t1,t3});
		Term t5 = compoundTerm(",", new Term[]{t4,t2});
		Solutions solutions = t5.call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 1000) {
			solutionsCount ++;
		}
		Assert.assertEquals(1,solutionsCount);
	}
	
}
