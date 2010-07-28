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
package saere.meta;

import saere.*;


/**
 * <p>
 * Implements the overall strategy how to evaluate a clause with multiple
 * goals.<br />
 * This class does not provide support for "or"ed goals using ";". Disjunctions
 * of goals have to be mapped to calls of the Or2 predicate. <br />
 * Support for the cut operator is not provided.
 * </p>
 * <b>Please note, that this class is not used by the compiler as the 
 * resulting code would not be efficient.</b> The primary use 
 * case of this class is to help you understand the evaluation strategy employed
 * by SAE Prolog and to use this class during testing or in semi-interpreted 
 *	mode.
 * 
 * <p>
 * <b>Example</b><br/>
 * Let's assume that we have the following predicate:
 * <code><pre>
 *	lookup(Key1,[(Key1,_)|Dict],A3) :-2 = lookup(A1,Dict,A3).
 *	<=> 
 *	lookup(A1,A2,A3) :- A2 = [(Key1,_)|Dict], A1 \= Key1, lookup(A1,Dict,A3).
 * </pre></code>
 * A direct translation of this predicate – when inheriting from this
 * class – would look as follows: 
 * <pre>
 * 	private class lookup3c2 extends MultipleGoals {
 * 	
 * 		val g1v1 = new Variable // Key1
 * 		val g1v2 = new Variable // Dict
 * 		val g1t1 = new ListElement2(new And2(g1v1,new Variable),g1v2)
 * 		
 * 		val goalCount : Int = 3
 * 		
 * 		def goal(i : Int ) : Solutions = i match {
 * 			case 1 => Unify2.unify(a2, g1t1)
 * 			case 2 => NotUnify2.unify(a1, g1v1)
 * 			case 3 => lookup3.unify(a1,g1v2,a3)
 * 		}	
 * 	}
 * </pre>
 * </p>
 * 
 * @author Michael Eichberg
 */
public abstract class MultipleGoals implements Solutions {
		
	/**
	 * Goal count must return a value that does not depend.
	 * @return the number of goals.
	 */
	protected abstract int goalCount();
		
	protected abstract Solutions goal(int i);
		
	private int currentGoal = 1;
	
	// IMPROVE Implement lazy semantics?
	private Solutions[] goalStack = new Solutions[goalCount()];
		
	public final boolean next() {
		while (currentGoal > 0) {
			Solutions solutions = goalStack[currentGoal-1];
			if (solutions == null) {
				solutions = goal(currentGoal);
				goalStack[currentGoal-1] = solutions;
			}
			
			if (solutions.next()) {
				if (currentGoal == goalCount()) {
					return true;
				} else {
					currentGoal += 1;
				}		
			} else {
				// Abolishing failed "goal iterators" is strictly required
				// to make sure that - when the goal is visited again - the
				// correct state information is saved.
				goalStack[currentGoal-1] = null;
				currentGoal -= 1;
			}
			
		}
		return false;
	}
}