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
import predicates.tak4;
import predicates.tak4Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.Term;
import saere.Variable;
import saere.utils.Evaluate;

public class tak {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		tak4Factory.registerWithPredicateRegistry(registry);
		benchmark1Factory.registerWithPredicateRegistry(registry);
	}

	@Test
	public void test() {

		{
			final Variable result = new Variable();
			Goal g = new tak4(atomic(18), atomic(12), atomic(5), result);
			Assert.assertTrue(g.next());
			Assert.assertEquals(6l, result.intEval());
			Assert.assertFalse(g.next());
		}

		{
			final Variable result = new Variable();
			Goal g = new tak4(atomic(18), atomic(12), atomic(4), result);
			Assert.assertTrue(g.next());
			Assert.assertEquals(5l, result.intEval());
			Assert.assertFalse(g.next());
		}
	}

	public static void main(String[] args) throws Throwable {

		final Variable result = new Variable();
		final long x = 34;
		final long y = 13;
		final long z = 6;
		final Term t = compoundTerm(atomic("tak"), atomic(x), atomic(y),
				atomic(z), result);

		Goal g = t.call();
		System.out.println(t.toProlog());
		while (g.next()) {
			System.out.println(result.toProlog() + "=" + result.toProlog());
		}

	}

	public static void measure() throws Error {
		Term term = compoundTerm(atomic("benchmark"), atomic("tak"));
		Goal s = term.call();
		long startTime = System.nanoTime();
		boolean succeeded = s.next();
		long duration = System.nanoTime() - startTime;
		if (succeeded) {
			Evaluate.writeToPerformanceLog("tak", duration);
		} else {
			Evaluate.writeToPerformanceLog("tak", -1l);
		}
	}

}
