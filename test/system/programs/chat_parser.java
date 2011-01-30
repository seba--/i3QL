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

import static saere.term.Terms.and;
import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;

import org.junit.Assert;
import org.junit.Test;

import predicates.benchmark1Factory;
import predicates.chat_parser0Factory;
import predicates.determinate_say2Factory;
import predicates.input1Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.Term;
import saere.Variable;
import saere.utils.Evaluate;

public class chat_parser {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		input1Factory.registerWithPredicateRegistry(registry);
		determinate_say2Factory.registerWithPredicateRegistry(registry);
		chat_parser0Factory.registerWithPredicateRegistry(registry);
		benchmark1Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) {

		Variable Input = new Variable();
		Variable Answer = new Variable();
		Term term = and(compoundTerm(atomic("input"), Input),
				compoundTerm(atomic("determinate_say"), Input, Answer));
		Goal g = term.call();
		while (g.next()) {
			System.out.println("Input = " + Input.toProlog() + ",");
			System.out.println("Answer = " + Answer.toProlog() + ";");
		}
		System.out.println("false.");
	}

	@Test
	public void testAnswers() {
		Variable input = new Variable();
		Variable result = new Variable();
		Term t = and(compoundTerm(atomic("input"), input),
				compoundTerm(atomic("determinate_say"), input, result));
		Goal s = t.call();
		int counter = 0;
		while (s.next())
			counter++;
		Assert.assertEquals(16, counter);
	}

	public static void measure() throws Error {
		Term term = compoundTerm(atomic("benchmark"), atomic("chat_parser"));
		Goal s = term.call();
		long startTime = System.nanoTime();
		boolean succeeded = s.next();
		long duration = System.nanoTime() - startTime;
		if (succeeded) {
			Evaluate.writeToPerformanceLog("chat_parser", duration);
		} else {
			Evaluate.writeToPerformanceLog("chat_parser", -1l);
		}
	}

}
