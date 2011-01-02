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
import static saere.term.Terms.variable;
import saere.ComplexTerm;
import saere.Goal;
import saere.GoalStack;
import saere.PredicateIdentifier;
import saere.PredicateRegistry;
import saere.State;
import saere.StringAtom;
import saere.Term;
import saere.ThreeArgsPredicateFactory;
import saere.Variable;
import saere.term.ListElement2;

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
		public Goal createInstance(Term Xs, Term Ys, Term Zs) {
			return new Append3(Xs, Ys, Zs);
		}

	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(IDENTIFIER, FACTORY);
	}

	// variables to control/manage the execution this predicate
	private GoalStack goalStack = GoalStack.EMPTY_GOAL_STACK;
	private int goalToExecute = 1;
	// variables related to the predicate's state
	private Term Xs;
	private Term Ys;
	private Term Zs;
	final private State XsState; // REQUIRED BY TAIL-CALL OPTIMIZATION ...
	final private State YsState;
	final private State ZsState;
	// variables to store clause local information
	private Term clv0;
	private Term clv1;
	private Term clv2;

	public Append3(final Term Xs, final Term Ys, final Term Zs) {
		// the implementation depends on the property of Xs...Zs being
		// "unwrapped"
		this.Xs = Xs.unwrapped();
		this.Ys = Ys.unwrapped();
		this.Zs = Zs.unwrapped();

		// REQUIRED BY TAIL-CALL OPTIMIZATION ...
		this.XsState = Xs.manifestState();
		this.YsState = Ys.manifestState();
		this.ZsState = Zs.manifestState();
	}

	public void abort() {
		this.goalStack = goalStack.abortPendingGoals();
	}

	public boolean choiceCommitted() {
		return false;
	}

	public boolean next() {
		do {
			if (this.clause1()) {
				return true;
			}

			this.goalToExecute = 1;
			if (!this.clause2()) {
				if (XsState != null)
					XsState.reincarnate();
				if (YsState != null)
					YsState.reincarnate();
				if (ZsState != null)
					ZsState.reincarnate();
				return false;
			}
			// continue;
		} while (true);
	}

	private boolean clause1() {

		eval_goals: do {
			switch (this.goalToExecute) {
			case 1: {
				this.goalStack = goalStack.put(new Unify2(Xs, EMPTY_LIST));
			}
			case 2: {
				boolean succeeded = this.goalStack.peek().next();
				if (!succeeded) {
					this.goalStack = goalStack.drop();
					return false;
				} // fall through ... 3
			}
			case 3: {
				this.goalStack = goalStack.put(new Unify2(Zs, Ys));
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
			case 1: { // test (and extract)...
				if (Xs.isVariable()) {
					// Xs has to be free..., because Xs was explicitly
					// "unwrapped"
					this.clv0 = new Variable();
					this.clv1 = new Variable();
					Xs.asVariable().bind(new ListElement2(clv0, clv1));
				} else if (!(Xs.arity() == 2 && Xs.functor().sameAs(
						StringAtom.LIST))) {
					return false;
				} else {
					ComplexTerm ctXs = Xs.asCompoundTerm();
					this.clv0 = ctXs.arg(0);
					this.clv1 = ctXs.arg(1);
				}
				this.goalToExecute = 3;
				continue;
			}
			case 2: {
				// FIXME restore state of variable
				return false;
			}
			case 3: {
				this.clv2 = variable();
				this.goalStack = goalStack.put(new Unify2(Zs, new ListElement2(
						clv0, clv2)));
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
				Xs = clv1.unwrapped();
				// Ys = Ys.unwrapped();
				Zs = clv2.unwrapped();
				// prepare next round...
				this.goalToExecute = 1;
				this.goalStack = GoalStack.EMPTY_GOAL_STACK;
				return true;
			}
			default:
				// should never be reached
				throw new Error("internal compiler error");
			}
		} while (true);
	}
}
