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
 * operations (put, peek and drop) to manage a list of {@link Solutions} iterators.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public abstract class GoalStack {

    private final static class EmptyGoalStack extends GoalStack {

	public EmptyGoalStack() {
	    // nothing to do
	}

	@Override
	public GoalStack put(Solutions solutions) {
	    return new SomeGoalStack(this, solutions);
	}

	@Override
	public Solutions peek() {
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
    }

    private final static class SomeGoalStack extends GoalStack {

	private final GoalStack rest;
	private final Solutions solutions;

	public SomeGoalStack(GoalStack rest, Solutions solutions) {
	    this.rest = rest;
	    this.solutions = solutions;
	}

	@Override
	public GoalStack put(Solutions furtherSolutions) {
	    return new SomeGoalStack(this, furtherSolutions);
	}

	@Override
	public Solutions peek() {
	    return solutions;
	}

	@Override
	public GoalStack drop() {
	    return rest;
	}

	@Override
	public boolean isNotEmpty() {
	    return true;
	}

    }

    private final static EmptyGoalStack EMPTY_GOAL_STACK = new EmptyGoalStack();

    public abstract GoalStack put(Solutions solutions);

    public abstract Solutions peek() throws IllegalStateException;

    public abstract GoalStack drop() throws IllegalStateException;

    public abstract boolean isNotEmpty();

    public static GoalStack emptyStack() {
	return EMPTY_GOAL_STACK;
    }
}
