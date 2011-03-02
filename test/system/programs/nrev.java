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
import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;

import org.junit.Assert;
import org.junit.Test;

import predicates.benchmark1;
import predicates.benchmark1Factory;
import predicates.nrev2Factory;
import predicates.nrev_range2Factory;
import predicates.run_nrev2;
import predicates.run_nrev2Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.predicate.Time1;
import saere.term.Terms;
import saere.utils.Evaluate;

public class nrev {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		run_nrev2Factory.registerWithPredicateRegistry(registry);
		nrev_range2Factory.registerWithPredicateRegistry(registry);
		nrev2Factory.registerWithPredicateRegistry(registry);
		benchmark1Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) {

		Variable range = new Variable();
		Variable solution = new Variable();
		StringAtom nrev_range = StringAtom.get("nrev_range");
		StringAtom nrev = atomic("nrev");
		Goal r = new Time1(compoundTerm(nrev_range,atomic(1500),range));
		System.out.print("% nrev_range ");
		r.next();
		Goal s = new Time1(compoundTerm(nrev, range, solution));
		System.out.print("% nrev ");
		s.next();	
		System.out.println(solution.toProlog());		
	}

	@Test
	public void test() {
		Variable result = new Variable();
		Goal s = new run_nrev2(atomic(10), result);
		assertTrue(s.next());
		Term expectedResult = Terms.delimitedList(atomic(9), atomic(8),
				atomic(7), atomic(6), atomic(5), atomic(4), atomic(3),
				atomic(2), atomic(1), atomic(0));
		assertTrue(result.unify(expectedResult));
		Assert.assertFalse(s.next());
	}

	public static void measure() throws Error {
		Goal s = new benchmark1(atomic("nrev"));
		long startTime = System.nanoTime();
		boolean succeeded = s.next();
		long duration = System.nanoTime() - startTime;
		if (succeeded) {
			Evaluate.writeToPerformanceLog("nrev", duration);
		} else {
			Evaluate.writeToPerformanceLog("nrev", -1l);
		}
	}

}
