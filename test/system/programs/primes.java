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

import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;

import org.junit.Assert;
import org.junit.Test;

import predicates.benchmark1Factory;
import predicates.integers3Factory;
import predicates.primes2;
import predicates.primes2Factory;
import predicates.remove3Factory;
import predicates.sift2Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.Term;
import saere.Variable;
import saere.term.Terms;
import saere.utils.Performance;

public class primes {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		primes2Factory.registerWithPredicateRegistry(registry);
		integers3Factory.registerWithPredicateRegistry(registry);
		sift2Factory.registerWithPredicateRegistry(registry);
		remove3Factory.registerWithPredicateRegistry(registry);
		benchmark1Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) throws Exception {
		Variable solution = new Variable();
		Goal s = new primes2(atomic(1000), solution);
		if (s.next()) {

			System.out.println(solution.toProlog());
		} else {
			throw new Error("Evaluation failed.");
		}
	}

	@Test
	public void test() {
		{
			Variable solution = new Variable();
			Goal s = new primes2(atomic(11), solution);
			Assert.assertTrue(s.next());
			Assert.assertTrue(solution.unify(Terms.delimitedList(atomic(2),
					atomic(3), atomic(5), atomic(7), atomic(11))));
		}
	}

	public static void measure() throws Error {
		Term term = compoundTerm(atomic("benchmark"), atomic("primes"));
		Goal s = term.call();
		long startTime = System.nanoTime();
		boolean succeeded = s.next();
		long duration = System.nanoTime() - startTime;
		if (succeeded) {
			Performance.writeToPerformanceLog("primes", duration);
		} else {
			Performance.writeToPerformanceLog("primes", -1l);
		}
	}
}
