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

import org.junit.Assert;
import org.junit.Test;

import predicates.crypt1;
import predicates.crypt1Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.Variable;
import saere.term.Terms;
import saere.utils.Benchmark;

@Benchmark
public class crypt {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		crypt1Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) {

		Variable solution = new Variable();
		Goal s = new crypt1(solution);
		while (s.next()) {
			System.out.println(solution.toProlog());
		}
	}

	@Test
	public void test() {
		Variable solution = new Variable();
		new crypt1(solution).next();
		Assert.assertTrue(solution.unify(Terms.delimitedList(atomic(3), atomic(4), atomic(8),
				atomic(2), atomic(8), atomic(2), atomic(7), atomic(8), atomic(4), atomic(6),
				atomic(9), atomic(6), atomic(9), atomic(7), atomic(4), atomic(4))));
	}

}
