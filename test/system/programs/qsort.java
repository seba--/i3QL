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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saere.term.Terms.and;
import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;

import org.junit.Test;

import predicates.benchmark1Factory;
import predicates.list1Factory;
import predicates.partition4Factory;
import predicates.qsort3Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.term.Terms;
import saere.utils.Performance;

public class qsort {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		list1Factory.registerWithPredicateRegistry(registry);
		qsort3Factory.registerWithPredicateRegistry(registry);
		partition4Factory.registerWithPredicateRegistry(registry);
		benchmark1Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) {
		Variable list = new Variable();
		Variable result = new Variable();
		Term t = and(compoundTerm(StringAtom.get("list"), list),
				compoundTerm(StringAtom.get("qsort"), list, result, StringAtom.EMPTY_LIST));
		t.call().next();
		System.out.println("Input=" + list.toProlog());
		System.out.println("Result=" + result.toProlog());
	}

	@Test
	public void test() {
		Variable list = new Variable();
		Variable result = new Variable();
		Term t = and(compoundTerm(StringAtom.get("list"), list),
				compoundTerm(StringAtom.get("qsort"), list, result, StringAtom.EMPTY_LIST));
		Goal s = t.call();
		assertTrue(s.next());
		Term expectedResult = Terms.delimitedList(atomic(0), atomic(2), atomic(4), atomic(6),
				atomic(7), atomic(8), atomic(10), atomic(11), atomic(11), atomic(17), atomic(18),
				atomic(18), atomic(21), atomic(27), atomic(27), atomic(28), atomic(28), atomic(28),
				atomic(29), atomic(31), atomic(32), atomic(33), atomic(37), atomic(39), atomic(40),
				atomic(46), atomic(47), atomic(51), atomic(53), atomic(53), atomic(55), atomic(59),
				atomic(61), atomic(63), atomic(65), atomic(66), atomic(74), atomic(74), atomic(75),
				atomic(81), atomic(82), atomic(83), atomic(85), atomic(85), atomic(90), atomic(92),
				atomic(94), atomic(95), atomic(99), atomic(99));
		assertTrue(result.unify(expectedResult));
		assertFalse(s.next());
	}

	public static void measure() throws Error {
		Term term = compoundTerm(atomic("benchmark"), atomic("qsort"));
		Goal s = term.call();
		long startTime = System.nanoTime();
		boolean succeeded = s.next();
		long duration = System.nanoTime() - startTime;
		if (succeeded) {
			Performance.writeToPerformanceLog("qsort", duration); 
		} else {
			Performance.writeToPerformanceLog("qsort", -1l);
		}
	}

}
