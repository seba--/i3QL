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

import saere.*;

/**
 * Implementation of SAE Prolog's <code>,</code>(and) operator.
 * <p>
 * This implementation generates a choice point and – in general – should not be
 * called.<br />
 * <i>It is only intended to be used to execute meta-level calls. </i><br />
 * The compiler has specific support for this operator and does not make use of
 * this class.
 * </p>
 * 
 * @author Michael Eichberg
 */
public class And2 implements Solutions {


	static void registerWithPredicateRegistry(PredicateRegistry predicateRegistry){
		predicateRegistry.registerPredicate(StringAtom.StringAtom(","), 2,
				new PredicateInstanceFactory() {

					@Override
					public Solutions createPredicateInstance(Term[] args) {
						return new And2(args[0], args[1]);
					}
				});

	}

	private final Term l;
	private final Term r;
	private State lState;
	private State rState;

	private int currentGoal = 0;
	private GoalStack goalStack = GoalStack.emptyStack();

	public And2(final Term l, final Term r) {
		this.l = l;
		this.r = r;
	}

	public boolean next() {
		while (true) {
			switch (currentGoal) {
			case 0:
				lState = l.manifestState();
				goalStack = goalStack.put(l.call());
				currentGoal = 1;
			case 1:
				if (!goalStack.peek().next()) {
					l.setState(lState);
					return false;
				}
				if (rState == null)
					rState = r.manifestState();
				goalStack = goalStack.put(r.call());
				currentGoal = 2;
			case 2:
				if (!goalStack.peek().next()) {
					goalStack = goalStack.reduce();
					currentGoal = 1;
				}
				else {
					return true;
				}
			}
		}
	}
}
