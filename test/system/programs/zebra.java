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

import predicates.houses1Factory;
import predicates.right_of3Factory;
import predicates.zebra1;
import predicates.zebra1Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.Term;
import saere.Variable;
import saere.term.Terms;
import saere.utils.Benchmark;

@Benchmark
public class zebra {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		zebra1Factory.registerWithPredicateRegistry(registry);
		houses1Factory.registerWithPredicateRegistry(registry);
		right_of3Factory.registerWithPredicateRegistry(registry);
	}

	@Test
	public void test() {
		final Variable result = new Variable();
		Goal g = new zebra1(result);
		g.next();
		Term expectedResult = Terms.delimitedList(
				compoundTerm(atomic("house"), atomic("yellow"),
						atomic("norwegian"), atomic("fox"), atomic("water"),
						atomic("kools")),
				compoundTerm(atomic("house"), atomic("blue"),
						atomic("ukrainian"), atomic("horse"), atomic("tea"),
						atomic("chesterfields")),
				compoundTerm(atomic("house"), atomic("red"), atomic("english"),
						atomic("snails"), atomic("milk"), atomic("winstons")),
				compoundTerm(atomic("house"), atomic("ivory"),
						atomic("spanish"), atomic("dog"),
						atomic("orange_juice"), atomic("lucky_strikes")),
				compoundTerm(atomic("house"), atomic("green"),
						atomic("japanese"), atomic("zebra"), atomic("coffee"),
						atomic("parliaments")));
		Assert.assertTrue(expectedResult.unify(result));

	}

	public static void main(String[] args) throws Throwable {

		final Variable result = new Variable();
		Goal g = new zebra1(result);
		g.next();
		System.out.println("Result=" + result.toProlog());
	}

}
