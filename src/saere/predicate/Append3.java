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

import static saere.StringAtom.EMPTY_LIST;
import static saere.StringAtom.LIST;
import static saere.term.Terms.complexTerm;
import static saere.term.Terms.variable;
import saere.Goal;
import saere.GoalStack;
import saere.PredicateIdentifier;
import saere.PredicateRegistry;
import saere.State;
import saere.StringAtom;
import saere.Term;
import saere.ThreeArgsPredicateFactory;

/**
 * Implementation of ISO Prolog's append/3 predicate.
 * 
 * <pre>
 * <code>
 * append([],Ys,Ys).
 * append([X|Xs],Ys,[X|Zs]) :- append(Xs,Ys,Zs).
 * </code>
 * </pre>
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class Append3 implements Goal {

	public final static PredicateIdentifier IDENTIFIER = new PredicateIdentifier(
			StringAtom.get("append"), 3);

	public final static ThreeArgsPredicateFactory FACTORY = new ThreeArgsPredicateFactory() {

		@Override
		public Goal createInstance(Term t1, Term t2, Term t3) {
			return new Append3(t1, t2, t3);
		}

	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(IDENTIFIER, FACTORY);
	}

	// variables to control/manage the execution this predicate
	private int clauseToExecute = 1;
	private GoalStack goalStack = GoalStack.EMPTY_GOAL_STACK;
	private int goalToExecute = 1;
	// variables related to the predicate's state
	private Term arg0;
	private Term arg1;
	private Term arg2;
	final private State arg0State; // REQUIRED BY TAIL-CALL OPTIMIZATION ...
	final private State arg1State;
	final private State arg2State;
	// variables to store clause local information
	private Term clv0;
	private Term clv1;
	private Term clv2;

	public Append3(final Term arg0, final Term arg1, final Term arg2) {
		this.arg0 = arg0.unwrapped();
		this.arg1 = arg1.unwrapped();
		this.arg2 = arg2.unwrapped();

		this.arg0State = arg0.manifestState(); // REQUIRED BY TAIL-CALL
												// OPTIMIZATION ...
		this.arg1State = arg1.manifestState();
		this.arg2State = arg2.manifestState();
	}

	public void abort() {
		this.goalStack = goalStack.abortPendingGoals();
	}

	public boolean choiceCommitted() {
		return false;
	}

	public boolean next() {
		do { // REQUIRED BY TAIL-CALL OPTIMIZATION ...
			switch (this.clauseToExecute) {
			case 1: {
				if (this.clause1()) {
					return true;
				} else {
					// this clause contains no "cut"
					// prepare the execution of the next clause
					this.goalToExecute = 1;
					this.clauseToExecute = 2;
				}
			}
			case 2: {
				// REQUIRED BY TAIL-CALL OPTIMIZATION ...
				if (this.clause2()) {
					continue;
				} else {
					if (arg0State != null)
						arg0State.reincarnate();
					if (arg1State != null)
						arg1State.reincarnate();
					if (arg2State != null)
						arg2State.reincarnate();
					return false;
				}
			}
			default:
				// should never be reached
				throw new Error("internal compiler error");
			}
		} while (true);
	}

	private boolean clause1() {

		eval_goals: do {
			switch (this.goalToExecute) {
			case 1: {
				this.goalStack = goalStack.put(new Unify2(arg0, EMPTY_LIST));
			}
			case 2: {
				boolean succeeded = this.goalStack.peek().next();
				if (!succeeded) {
					this.goalStack = goalStack.drop();
					return false;
				} // fall through ... 3
			}
			case 3: {
				this.goalStack = goalStack.put(new Unify2(arg2, arg1));
			}
			case 4: {
				boolean succeeded = this.goalStack.peek().next();
				if (!succeeded) {
					this.goalStack = goalStack.drop();
					this.goalToExecute = 2;
					continue eval_goals;
				}
				this.goalToExecute = 4;
				return true;
			}
			default:
				// should never be reached
				throw new Error("internal compiler error");
			}
		} while (true);
	}

	private boolean clause2() {
		eval_goals: do {
			switch (this.goalToExecute) {
			case 1: {
				this.clv0 = variable();
				this.clv1 = variable();
				this.goalStack = goalStack.put(new Unify2(arg0, complexTerm(
						LIST, clv0, clv1)));
			}
			case 2: {
				boolean succeeded = this.goalStack.peek().next();
				if (!succeeded) {
					this.goalStack = goalStack.drop();
					return false;
				}
				// fall through ... 3
			}
			case 3: {
				this.clv2 = variable();
				this.goalStack = goalStack.put(new Unify2(arg2, complexTerm(
						LIST, clv0, clv2)));
			}
			case 4: {
				boolean succeeded = this.goalStack.peek().next();
				if (!succeeded) {
					this.goalStack = goalStack.drop();
					this.goalToExecute = 2;
					continue eval_goals;
				}
				// fall through ... 5
			}
			case 5: {
				// update "input" variables
				arg0 = clv1.unwrapped();
				arg1 = arg1.unwrapped();
				arg2 = clv2.unwrapped();
				// prepare next round...
				this.clauseToExecute = 1;
				this.goalToExecute = 1;
				this.goalStack = GoalStack.EMPTY_GOAL_STACK;
				return true;
				//
				// this.goalStack = goalStack.put(new append3(clv1, arg1,
				// clv2));
				// }
				// case 6: {
				// boolean succeeded = this.goalStack.peek().next();
				// if (!succeeded) {
				// this.goalStack = goalStack.drop();
				// this.goalToExecute = 4;
				// continue eval_goals;
				// }
				// this.goalToExecute = 6;
				// return true;
			}
			default:
				// should never be reached
				throw new Error("internal compiler error");
			}
		} while (true);
	}
}
