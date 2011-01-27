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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saere.term.Terms.atomic;

import org.junit.Test;

import predicates.ancestor2;
import predicates.brother2;
import predicates.father2;
import predicates.grandparent2;
import predicates.half_sister2;
import predicates.male1;
import predicates.sibling2;
import predicates.sister2;
import saere.Goal;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class ancestor {

	public static void main(String[] args) {
		Variable X = new Variable();
		Variable Y = new Variable();
		Goal solutions = new half_sister2(X, Y);
		while (solutions.next()) {
			System.out.println("X=" + X.toProlog() + ", Y=" + Y.toProlog());
		}
	}

	@Test
	public void testMale() {
		Goal goal = new male1(StringAtom.get("Thilo"));
		assertTrue(goal.next());
		assertFalse(goal.next());

		assertFalse(new male1(StringAtom.get("Maria")).next());

		{
			final Term[] expectedSolutions = new Term[] { atomic("Thilo"), atomic("Michael"),
					atomic("Werner"), atomic("Reinhard"), atomic("Vincent"), atomic("Wilhelm") };
			final Variable X = new Variable();
			final Goal solutions = new male1(X);
			int count = 0;
			while (solutions.next()) {
				assertTrue(X.reveal().unify(expectedSolutions[count]));
				count++;
			}
			assertEquals(expectedSolutions.length,count);
		}
	}

	@Test
	public void testFather() {
		{
			Goal solutions = new father2(StringAtom.get("Reinhard"), StringAtom.get("Werner"));
			assertFalse(solutions.next());

		}
		{
			Goal solutions = new father2(StringAtom.get("Werner"), StringAtom.get("Michael"));
			assertTrue(solutions.next());
			assertFalse(solutions.next());
		}

		{
			StringAtom[][] expectedSolutions = new StringAtom[][] {
					{atomic("Michael"),atomic("Wilhelm")},
					{atomic("Michael"),atomic("Vincent")},
					{atomic("Michael"),atomic("Leonie")},
					{atomic("Michael"),atomic("Valerie")},
					{atomic("Reinhard"),atomic("Alice")},
					{atomic("Reinhard"),atomic("Thilo")},
					{atomic("Werner"),atomic("Michael")}
			};
			
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new father2(X, Y);
			int count = 0;
			while (solutions.next()) {
				assertTrue(X.reveal().asStringAtom().sameAs(expectedSolutions[count][0]));
				assertTrue(Y.reveal().asStringAtom().sameAs(expectedSolutions[count][1]));
				count ++;
			}
			assertEquals(expectedSolutions.length, count);
		}
	}

	@Test
	public void testSibling() {
		{
			StringAtom[][] expectedSolutions = new StringAtom[][] {
					{atomic("Leonie"),atomic("Vincent")},
					{atomic("Leonie"),atomic("Valerie")},
					{atomic("Vincent"),atomic("Leonie")},
					{atomic("Vincent"),atomic("Valerie")},
					{atomic("Valerie"),atomic("Leonie")},
					{atomic("Valerie"),atomic("Vincent")},
					{atomic("Alice"),atomic("Thilo")},
					{atomic("Thilo"),atomic("Alice")}
			};
			
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new sibling2(X, Y);
			int count = 0;
			while (solutions.next()) {
				assertTrue(X.reveal().asStringAtom().sameAs(expectedSolutions[count][0]));
				assertTrue(Y.reveal().asStringAtom().sameAs(expectedSolutions[count][1]));
				count ++;
			}
			assertEquals(expectedSolutions.length, count);
		}
	}
	
	@Test
	public void testBrother() {
		{
			StringAtom[][] expectedSolutions = new StringAtom[][] {
					{atomic("Vincent"),atomic("Leonie")},
					{atomic("Vincent"),atomic("Valerie")},
					{atomic("Thilo"),atomic("Alice")}
			};
			
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new brother2(X, Y);
			int count = 0;
			while (solutions.next()) {
				assertTrue(X.reveal().asStringAtom().sameAs(expectedSolutions[count][0]));
				assertTrue(Y.reveal().asStringAtom().sameAs(expectedSolutions[count][1]));
				count ++;
			}
			assertEquals(expectedSolutions.length, count);
		}
	}
	
	
	@Test
	public void testSister() {
		{
			StringAtom[][] expectedSolutions = new StringAtom[][] {
					{atomic("Leonie"),atomic("Vincent")},
					{atomic("Leonie"),atomic("Valerie")},
					{atomic("Valerie"),atomic("Leonie")},
					{atomic("Valerie"),atomic("Vincent")},
					{atomic("Alice"),atomic("Thilo")}
			};
			
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new sister2(X, Y);
			int count = 0;
			while (solutions.next()) {
				assertTrue(X.reveal().asStringAtom().sameAs(expectedSolutions[count][0]));
				assertTrue(Y.reveal().asStringAtom().sameAs(expectedSolutions[count][1]));
				count ++;
			}
			assertEquals(expectedSolutions.length, count);
		}
	}
	
	@Test
	public void testGrandparent() {
		{
			StringAtom[][] expectedSolutions = new StringAtom[][] {
					{atomic("Christel"),atomic("Wilhelm")},
					{atomic("Christel"),atomic("Vincent")},
					{atomic("Christel"),atomic("Leonie")},
					{atomic("Christel"),atomic("Valerie")},
					{atomic("Gertrud"),atomic("Michael")},
					{atomic("Werner"),atomic("Wilhelm")},
					{atomic("Werner"),atomic("Vincent")},
					{atomic("Werner"),atomic("Leonie")},
					{atomic("Werner"),atomic("Valerie")}

			};
			
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new grandparent2(X, Y);
			int count = 0;
			while (solutions.next()) {
				assertTrue(X.reveal().asStringAtom().sameAs(expectedSolutions[count][0]));
				assertTrue(Y.reveal().asStringAtom().sameAs(expectedSolutions[count][1]));
				count ++;
			}
			assertEquals(expectedSolutions.length, count);
		}
	}
	
	@Test
	public void testAncestor() {
		{
			StringAtom[][] expectedSolutions = new StringAtom[][] {
					{atomic("Gertrud"),atomic("Wilhelm")},
					{atomic("Heidi"),atomic("Leonie")},
					{atomic("Heidi"),atomic("Vincent")},
					{atomic("Heidi"),atomic("Valerie")},
					{atomic("Christel"),atomic("Michael")},
					{atomic("Gertrud"),atomic("Christel")},
					{atomic("Magdalena"),atomic("Alice")},
					{atomic("Magdalena"),atomic("Thilo")},
					{atomic("Michael"),atomic("Wilhelm")},
					{atomic("Michael"),atomic("Vincent")},
					{atomic("Michael"),atomic("Leonie")},
					{atomic("Michael"),atomic("Valerie")},
					{atomic("Reinhard"),atomic("Alice")},
					{atomic("Reinhard"),atomic("Thilo")},
					{atomic("Werner"),atomic("Michael")},
					{atomic("Christel"),atomic("Wilhelm")},
					{atomic("Christel"),atomic("Vincent")},
					{atomic("Christel"),atomic("Leonie")},
					{atomic("Christel"),atomic("Valerie")},
					{atomic("Gertrud"),atomic("Michael")},
					{atomic("Gertrud"),atomic("Wilhelm")},
					{atomic("Gertrud"),atomic("Vincent")},
					{atomic("Gertrud"),atomic("Leonie")},
					{atomic("Gertrud"),atomic("Valerie")},
					{atomic("Werner"),atomic("Wilhelm")},
					{atomic("Werner"),atomic("Vincent")},
					{atomic("Werner"),atomic("Leonie")},
					{atomic("Werner"),atomic("Valerie")}
			};
			
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new ancestor2(X, Y);
			int count = 0;
			while (solutions.next()) {
				assertTrue(X.reveal().asStringAtom().sameAs(expectedSolutions[count][0]));
				assertTrue(Y.reveal().asStringAtom().sameAs(expectedSolutions[count][1]));
				count ++;
			}
			assertEquals(expectedSolutions.length, count);
		}
	}
	
	@Test
	public void testHalfSister() {
		{
			Goal solutions = new half_sister2(StringAtom.get("Leonie"), StringAtom.get("Leonie"));
			assertFalse(solutions.next());
		}
		{
			Goal solutions = new half_sister2(StringAtom.get("Heidi"), StringAtom.get("Wilhelm"));
			assertFalse(solutions.next());
		}
		{
			Goal solutions = new half_sister2(StringAtom.get("Leonie"), StringAtom.get("Wilhelm"));
			assertTrue(solutions.next());
			assertFalse(solutions.next());
		}
		{
			Variable X = new Variable();
			Goal solutions = new half_sister2(X, StringAtom.get("Wilhelm"));
			assertTrue(solutions.next());
			assertTrue(X.reveal().asStringAtom().sameAs(atomic("Leonie")));
			assertTrue(solutions.next());
			assertTrue(X.reveal().asStringAtom().sameAs(atomic("Valerie")));
			assertFalse(solutions.next());
		}
		{
			StringAtom[][] expectedSolutions = new StringAtom[][] {
					{atomic("Leonie"),atomic("Wilhelm")},
					{atomic("Valerie"),atomic("Wilhelm")}
			};
			
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new half_sister2(X, Y);
			int count = 0;
			while (solutions.next()) {
				assertTrue(X.reveal().asStringAtom().sameAs(expectedSolutions[count][0]));
				assertTrue(Y.reveal().asStringAtom().sameAs(expectedSolutions[count][1]));
				count ++;
			}
			assertEquals(expectedSolutions.length, count);
		}
	}
}
