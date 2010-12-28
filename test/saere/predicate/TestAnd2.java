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
package saere.predicate;

import static saere.term.Terms.and;
import static saere.term.Terms.atomic;
import static saere.term.Terms.or;
import static saere.term.Terms.unify;

import org.junit.Assert;
import org.junit.Test;

import saere.Solutions;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class TestAnd2 {

    @Test
    public void testDeterministicEvaluation() {

	// v = 1, true.

	Variable v = new Variable();
	Term t1 = unify(v, atomic(1));
	Term t2 = atomic("true");
	Solutions solutions = and(t1, t2).call();
	int solutionsCount = 0;
	while (solutions.next() && solutionsCount < 2) {
	    solutionsCount++;
	}
	Assert.assertEquals(1, solutionsCount);
    }

    @Test
    public void testNonDeterministicEvaluation() {

	// repeat,true.

	Term t1 = atomic("repeat");
	Term t2 = atomic("true");
	Solutions solutions = and(t1, t2).call();
	int solutionsCount = 0;
	while (solutions.next() && solutionsCount < 1000) {
	    solutionsCount++;
	}
	Assert.assertEquals(1000, solutionsCount);
    }

    @Test
    public void testCutREvaluation() {
	// (repeat,true),!.

	Term t1 = atomic("repeat");
	Term t2 = atomic("true");
	Term t3 = atomic("!");
	Term t4 = and(t1, t2);
	Term t5 = and(t4, t3);
	Solutions solutions = t5.call();
	int solutionsCount = 0;
	while (solutions.next() && solutionsCount < 2) {
	    solutionsCount++;
	}
	Assert.assertEquals(1, solutionsCount);
    }

    @Test
    public void testCutLEvaluation() {

	// (repeat,!),true.
	Term t1 = atomic("repeat");
	Term t2 = atomic("true");
	Term t3 = atomic("!");
	Term t4 = and(t1, t3);
	Term t5 = and(t4, t2);
	Solutions solutions = t5.call();
	int solutionsCount = 0;
	while (solutions.next() && solutionsCount < 2) {
	    solutionsCount++;
	}
	Assert.assertEquals(1, solutionsCount);
    }

    @Test
    public void testAndOrCutLEvaluation() {

	// (repeat, !, fail ; true).
	Term t1 = atomic("repeat");
	Term t2 = StringAtom.CUT;
	Term t3 = atomic("fail");
	Term t4 = atomic("true");
	Term t5 = and(t1, t2);
	Term t6 = and(t5, t3);
	Term t7 = or(t6, t4);
	Solutions solutions = t7.call();
	int solutionsCount = 0;
	while (solutions.next() && solutionsCount < 2) {
	    solutionsCount++;
	}
	Assert.assertEquals(0, solutionsCount);
    }

    @Test
    public void testTwoSolutions() {

	// repeat,(true;!),true.
	Term t1 = atomic("repeat");
	Term t2 = atomic("true");
	Term t3 = StringAtom.CUT;
	Term t4 = or(t2, t3);
	Term t5 = and(t1, t4);
	Term t6 = and(t5, t2);
	Solutions solutions = t6.call();
	int solutionsCount = 0;
	while (solutions.next() && solutionsCount < 10) {
	    solutionsCount++;
	}
	Assert.assertEquals(2, solutionsCount);
    }
}
