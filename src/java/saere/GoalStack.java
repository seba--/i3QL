/* License (BSD Style License):
 * Copyright (c) 2010,2011
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
 * operations (put, peek and drop) to manage a list of {@link Goal}s. The empty goal stack is
 * represented by <code>null</code>.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public class GoalStack {

	private final GoalStack rest;
	private final Goal goal;

	public GoalStack(Goal goal) {
		this(goal,null); 
	}

	public GoalStack(Goal goal, GoalStack rest) {
		assert goal != null;
		
		this.goal = goal;
		this.rest = rest;
	}

	public GoalStack put(@SuppressWarnings("hiding") Goal goal) {
		assert goal != null;
		
		return new GoalStack(goal, this);
	}

	public Goal peek() {
		return this.goal;
	}

	public GoalStack scrapTopGoal() {
		return rest;
	}

	public GoalStack abortAndScrapAllGoals() {
		this.goal.abort();
		GoalStack gs = this.rest;
		return gs == null ? null : gs.abortAndScrapAllGoals();
	}

	public GoalStack abortAndScrapTopGoal() {
		this.goal.abort();
		return rest;
	}

	public GoalStack abortAndScrapGoals(int i) {
		assert i > 0;

		// TODO replace recursive call by while loop
		this.goal.abort();
		if (i > 1) {
			return this.rest.abortAndScrapGoals(i - 1);
		}
		return this.rest;
	}

}
