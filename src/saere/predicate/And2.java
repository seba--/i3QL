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
 * Implementation of SAE Prolog's and (<code>,</code>) operator.
 * 
 * @author Michael Eichberg
 */
public final class And2 implements Solutions {

	public static void registerWithPredicateRegistry(
			PredicateRegistry predicateRegistry) {
		predicateRegistry.registerPredicate(StringAtom.instance(","), 2,
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

	private boolean choiceCommitted = false;

	private int currentGoal = 0;
	private GoalStack goalStack = GoalStack.emptyStack();

	public And2(final Term l, final Term r) {
		this.l = l;
		this.r = r;
	}

	public boolean next() {
		while (true) {
			switch (currentGoal) {
			case Commons.FAILED:
				// reset all bindings
				l.setState(lState);
				if (rState != null)
					r.setState(rState);
				// clear "everything"
				goalStack = null;
				lState = rState = null;
				return false;
			case 0: // Initialization
				lState = l.manifestState();
				goalStack = goalStack.put(l.call());
				currentGoal = 1;
			case 1:
				// call (or redo) the first goal
				Solutions ls = goalStack.peek();
				if (!ls.next()) {
					choiceCommitted = choiceCommitted | ls.choiceCommitted();
					currentGoal = Commons.FAILED;
					continue;
				}
				// prepare call of the second goal
				if (rState == null)
					rState = r.manifestState();
				goalStack = goalStack.put(r.call());
				currentGoal = 2;
			case 2:
				Solutions rs = goalStack.peek();
				if (!rs.next()) {
					choiceCommitted = rs.choiceCommitted();
					if (choiceCommitted) {
						currentGoal = Commons.FAILED;
					} else {
						goalStack = goalStack.reduce();
						currentGoal = 1;
					}
					continue;
				} else {
					return true;
				}
			}
		}
	}

	@Override
	public boolean choiceCommitted() {
		return choiceCommitted;
	}
}
