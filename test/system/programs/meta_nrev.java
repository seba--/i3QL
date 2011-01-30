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

import static org.junit.Assert.assertTrue;
import static saere.term.Terms.and;
import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import static saere.term.Terms.delimitedList;

import org.junit.Assert;
import org.junit.Test;

import predicates.benchmark1Factory;
import predicates.interpret1Factory;
import predicates.meta_nrev_range2Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.term.Terms;
import saere.utils.Evaluate;

public class meta_nrev {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		benchmark1Factory.registerWithPredicateRegistry(registry);
		interpret1Factory.registerWithPredicateRegistry(registry);
		meta_nrev_range2Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) throws Throwable {

		Variable range = new Variable();
		Variable solution = new Variable();
		Goal g1 = compoundTerm(atomic("meta_nrev_range"), atomic(100), range)
				.call();
		if (!g1.next())
			throw new Error();
		Goal g2 = compoundTerm(atomic("interpret"),
				delimitedList(compoundTerm(atomic("nrev"), range, solution)))
				.call();
		if (!g2.next())
			throw new Error();

		System.out.println(solution.toProlog());

	}

	@Test
	public void test() {
		Variable list = new Variable();
		Variable result = new Variable();
		Term t = and(
				compoundTerm(StringAtom.get("meta_nrev_range"), atomic(10),
						list),
				compoundTerm(
						StringAtom.get("interpret"),
						delimitedList(compoundTerm(atomic("nrev"), list, result))));
		Goal s = t.call();
		assertTrue(s.next());
		Term expectedResult = Terms.delimitedList(atomic(9), atomic(8),
				atomic(7), atomic(6), atomic(5), atomic(4), atomic(3),
				atomic(2), atomic(1), atomic(0));
		assertTrue(result.unify(expectedResult));
		Assert.assertFalse(s.next());
	}

	public static void measure() throws Error {
		Term term = compoundTerm(atomic("benchmark"), atomic("meta_nrev"));
		Goal s = term.call();
		long startTime = System.nanoTime();
		boolean succeeded = s.next();
		long duration = System.nanoTime() - startTime;
		if (succeeded) {
			Evaluate.writeToPerformanceLog("meta_nrev", duration);
		} else {
			Evaluate.writeToPerformanceLog("meta_nrev", -1l);
		}
	}

}
