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
package saere;

/**
 * The goal stack is a very simple, <i>immutable</i> linked list, that offers a stack's standard
 * operations (put, peek and drop) to manage a list of {@link Goal}s.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public abstract class GoalStack {

	private final static class EmptyGoalStack extends GoalStack {

		public EmptyGoalStack() {
			// nothing to do
		}

		@Override
		public GoalStack put(Goal solutions) {
			return new SomeGoalStack(this, solutions);
		}

		@Override
		public Goal peek() {
			throw new IllegalStateException("the goal stack is empty");
		}

		@Override
		public GoalStack drop() {
			throw new IllegalStateException("the goal stack is empty");
		}

		@Override
		public boolean isNotEmpty() {
			return false;
		}

		@Override
		public GoalStack abortPendingGoals() {
			return this;
		}

		@Override
		public GoalStack abortTopLevelGoal() throws IllegalStateException {
			throw new IllegalStateException();
		}

		@Override
		public GoalStack abortPendingGoals(int i) {
			assert i == 0 : "there are no more pending goals, the goal stack is empty";
			return this;
		}
	}

	private final static class SomeGoalStack extends GoalStack {

		private final GoalStack rest;
		private final Goal goal;

		public SomeGoalStack(GoalStack rest, Goal goal) {
			this.rest = rest;
			this.goal = goal;
		}

		@Override
		public GoalStack put(@SuppressWarnings("hiding") Goal goal) {
			return new SomeGoalStack(this, goal);
		}

		@Override
		public Goal peek() {
			return goal;
		}

		@Override
		public GoalStack drop() {
			return rest;
		}

		@Override
		public boolean isNotEmpty() {
			return true;
		}

		@Override
		public GoalStack abortPendingGoals() {
			goal.abort();
			return rest.abortPendingGoals();
		}

		@Override
		public GoalStack abortTopLevelGoal() {
			goal.abort();
			return rest;
		}

		@Override
		public GoalStack abortPendingGoals(int i) {
			if (i > 0) {
				goal.abort();
				return rest.abortPendingGoals(i - 1);
			}
			return this;
		}

	}

	public final static EmptyGoalStack EMPTY_GOAL_STACK = new EmptyGoalStack();

	public abstract GoalStack put(Goal goal);

	public abstract Goal peek() throws IllegalStateException;

	public abstract GoalStack drop() throws IllegalStateException;

	public abstract GoalStack abortTopLevelGoal();

	public abstract GoalStack abortPendingGoals(int i);

	public abstract GoalStack abortPendingGoals();

	public abstract boolean isNotEmpty();

}
