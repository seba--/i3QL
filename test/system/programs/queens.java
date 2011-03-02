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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;

import org.junit.Test;

import predicates.benchmark1Factory;
import predicates.not_attack2Factory;
import predicates.not_attack3Factory;
import predicates.queens2Factory;
import predicates.queens3Factory;
import predicates.range3Factory;
import predicates.select3Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.term.Terms;
import saere.utils.Evaluate;

public class queens {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		not_attack2Factory.registerWithPredicateRegistry(registry);
		not_attack3Factory.registerWithPredicateRegistry(registry);
		queens2Factory.registerWithPredicateRegistry(registry);
		queens3Factory.registerWithPredicateRegistry(registry);
		range3Factory.registerWithPredicateRegistry(registry);
		select3Factory.registerWithPredicateRegistry(registry);
		benchmark1Factory.registerWithPredicateRegistry(registry);
	}

	@Test
	public void test() {
		Term[] expectedResults = new Term[] {
				Terms.delimitedList(atomic(1)),
				null,
				null,
				Terms.delimitedList(atomic(3), atomic(1), atomic(4), atomic(2)),
				Terms.delimitedList(atomic(4), atomic(2), atomic(5), atomic(3),
						atomic(1)),
				Terms.delimitedList(atomic(5), atomic(3), atomic(1), atomic(6),
						atomic(4), atomic(2)),
				Terms.delimitedList(atomic(6), atomic(4), atomic(2), atomic(7),
						atomic(5), atomic(3), atomic(1)),
				Terms.delimitedList(atomic(4), atomic(2), atomic(7), atomic(3),
						atomic(6), atomic(8), atomic(5), atomic(1)),
				Terms.delimitedList(atomic(5), atomic(7), atomic(9), atomic(4),
						atomic(2), atomic(8), atomic(6), atomic(3), atomic(1)),
				Terms.delimitedList(atomic(7), atomic(4), atomic(2), atomic(9),
						atomic(5), atomic(10), atomic(8), atomic(6), atomic(3),
						atomic(1)),
				Terms.delimitedList(atomic(10), atomic(8), atomic(6),
						atomic(4), atomic(2), atomic(11), atomic(9), atomic(7),
						atomic(5), atomic(3), atomic(1)),
				Terms.delimitedList(atomic(4), atomic(9), atomic(7), atomic(2),
						atomic(11), atomic(6), atomic(12), atomic(10),
						atomic(8), atomic(5), atomic(3), atomic(1)),
				Terms.delimitedList(atomic(7), atomic(11), atomic(8),
						atomic(6), atomic(4), atomic(13), atomic(10),
						atomic(12), atomic(9), atomic(2), atomic(5), atomic(3),
						atomic(1)),
				Terms.delimitedList(atomic(11), atomic(8), atomic(6),
						atomic(2), atomic(9), atomic(14), atomic(4),
						atomic(13), atomic(10), atomic(12), atomic(7),
						atomic(5), atomic(3), atomic(1)),
				Terms.delimitedList(atomic(8), atomic(11), atomic(7),
						atomic(15), atomic(6), atomic(9), atomic(13),
						atomic(4), atomic(14), atomic(12), atomic(10),
						atomic(2), atomic(5), atomic(3), atomic(1)),
				Terms.delimitedList(atomic(10), atomic(8), atomic(11),
						atomic(4), atomic(7), atomic(16), atomic(6),
						atomic(15), atomic(12), atomic(14), atomic(9),
						atomic(13), atomic(2), atomic(5), atomic(3), atomic(1)),
				Terms.delimitedList(atomic(13), atomic(10), atomic(12),
						atomic(9), atomic(6), atomic(4), atomic(17),
						atomic(14), atomic(16), atomic(7), atomic(15),
						atomic(11), atomic(8), atomic(2), atomic(5), atomic(3),
						atomic(1)),
				Terms.delimitedList(atomic(10), atomic(14), atomic(9),
						atomic(11), atomic(4), atomic(7), atomic(18),
						atomic(6), atomic(17), atomic(13), atomic(16),
						atomic(12), atomic(15), atomic(8), atomic(2),
						atomic(5), atomic(3), atomic(1)) };

		for (int i = 0; i < 18; i++) {
			Variable solution = new Variable();
			StringAtom queens = StringAtom.get("queens");
			Goal s = compoundTerm(queens, atomic(i + 1), solution).call();
			if (s.next())
				assertTrue(expectedResults[i].unify(solution));
			else
				assertNull(expectedResults[i]);
		}
	}

	public static void main(String[] args) {
		long i = args.length == 0 ? 8l : Long.valueOf(args[0]).longValue();
		Variable solution = new Variable();
		StringAtom queens = StringAtom.get("queens");
		Goal s = compoundTerm(queens, atomic(i), solution).call();
		if (s.next())
			System.out.println(solution.toProlog());
		else
			System.out.println("no solution");
	}

	public static void measure(String[] args) throws Error {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("queens-20")) {
				queens20();
			} else if (arg.equals("queens-1-26")) {
				queens1to26();
			} else {
				throw new Error();
			}
		}
	}

	public static void measure() throws Error {
		{
			Term term = compoundTerm(atomic("benchmark"), atomic("queens"));
			Goal s = term.call();
			long startTime = System.nanoTime();
			boolean succeeded = s.next();
			long duration = System.nanoTime() - startTime;
			if (succeeded) {
				Evaluate.writeToPerformanceLog("queens", duration);
			} else {
				Evaluate.writeToPerformanceLog("queens", -1l);
			}
		}

		{
			Term term = compoundTerm(atomic("benchmark"),
					atomic("queens_8_findall"));
			Goal s = term.call();
			long startTime = System.nanoTime();
			boolean succeeded = s.next();
			long duration = System.nanoTime() - startTime;
			if (succeeded) {
				Evaluate.writeToPerformanceLog("queens-8-findall", duration);
			} else {
				Evaluate.writeToPerformanceLog("queens-8-findall", -1l);
			}
		}
	}

	private static void queens1to26() {
		for (int i = 1; i <= 26; i++) {
			Variable solution = new Variable();
			StringAtom queens = StringAtom.get("queens");
			Goal s = compoundTerm(queens, atomic(i), solution).call();
			long startTime = System.nanoTime();
			if (!(s.next() || i == 2 || i == 3)) {
				throw new Error();
			}
			long duration = System.nanoTime() - startTime;
			Evaluate.writeToPerformanceLog("queens-1-to-26", i, duration);
		}
	}

	private static void queens20() throws Error {
		for (int i = 0; i < 25; i++) {
			Variable solution = new Variable();
			StringAtom queens = StringAtom.get("queens");
			Goal s = compoundTerm(queens, atomic(20), solution).call();
			long startTime = System.nanoTime();
			if (!s.next()) {
				throw new Error();
			}
			long duration = System.nanoTime() - startTime;

			Evaluate.writeToPerformanceLog("queens-20", i, duration);
		}
	}

}
