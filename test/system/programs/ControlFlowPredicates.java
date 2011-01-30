/* License (BSD Style License):
 * Copyright (c) 2010
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische 
 *    Universität Darmstadt nor the names of its contributors may be used to 
 *    endorse or promote products derived from this software without specific 
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package harness;

import org.junit.Assert;
import org.junit.Test;

import predicates.test2;
import saere.CompoundTerm;
import saere.Goal;
import saere.State;
import saere.Term;
import saere.Variable;

public class ControlFlowPredicates {

	// static {
	// PredicateRegistry registry = PredicateRegistry.predicateRegistry();
	// }

	public static void main(String[] args) throws Exception {
		new ControlFlowPredicates().test();
	}

	@Test
	public void test() throws Exception {
		int testsCounter = 0; // REFACTOR PASS THIS STUFF TO THE TEST RUNNER

		Variable predicate = new Variable();
		Variable tests = new Variable();
		Goal g = new test2(predicate, tests);
		while (g.next()) {
			CompoundTerm ct = predicate.reveal().asCompoundTerm();
			String functor = new String(ct.firstArg().asStringAtom().rawValue());
			long arity = ct.secondArg().asIntValue().intEval();

			@SuppressWarnings("unchecked")
			Class<Goal> testGoalClass = (Class<Goal>) Class
					.forName("predicates." + functor + arity);
			Term testArgs = tests.reveal();
			// all except the last argument are considered input arguments
			Term[] goalArgs = new Term[testArgs.arity()];
			for (int i = 0; i < testArgs.arity() - 1; i++) {
				goalArgs[i] = testArgs.arg(i).asCompoundTerm().firstArg(); // the first argument of
																			// "in"
			}
			Variable out = new Variable();
			goalArgs[testArgs.arity() - 1] = out;
			Goal testGoal = (Goal) testGoalClass.getConstructors()[0]
					.newInstance((Object[]) goalArgs);

			// expectedResults is always a list...
			Term expectedResults = testArgs.arg(testArgs.arity() - 1)
					.asCompoundTerm().firstArg();

			while (testGoal.next()) {
				State outState = out.manifestState();
				Term expectedResult = expectedResults.asCompoundTerm()
						.firstArg();
				State expectedResultState = expectedResult.manifestState();

				Assert.assertTrue(
						functor + "/" + arity + " => "
								+ expectedResult.toProlog() + " unify "
								+ out.toProlog(), expectedResult.unify(out));
				testsCounter++;

				if (outState != null)
					outState.reincarnate();
				if (expectedResultState != null)
					expectedResultState.reincarnate();

				expectedResults = expectedResults.asCompoundTerm().secondArg();
			}

		}

		System.out.println("Number of successful test: " + testsCounter);
	}

}
