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
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public abstract class UndoGoal implements Goal {

	UndoGoal() {
		// to prevent other classes (outside of the core of the SAE) to extend this one...
	}

	private static final UndoGoal DO_NOTHING_UNDO_GOAL = new UndoGoal() {

		@Override
		public void abort() {
			// nothing to do
		}

	};

	private static final class UndoGoalOneNonTrivialState extends UndoGoal {
		private final State state;

		UndoGoalOneNonTrivialState(State state) {
			// IMPROVE create a special undo goal for just one,two... states?
			this.state = state;
		}

		@Override
		public void abort() {
			state.reincarnate();
		}

	}

	private static final class UndoGoalTwoStates extends UndoGoal {

		private final State s0;
		private final State s1;

		UndoGoalTwoStates(Term t0, Term t1) {
			this.s0 = t0.manifestState();
			this.s1 = t1.manifestState();
		}

		@Override
		public void abort() {
			if (s0 != null)
				s0.reincarnate();
			if (s1 != null)
				s1.reincarnate();
		}

	}

	private static final class UndoGoalThreeStates extends UndoGoal {

		private final State s0;
		private final State s1;
		private final State s2;

		UndoGoalThreeStates(Term t0, Term t1, Term t2) {
			this.s0 = t0.manifestState();
			this.s1 = t1.manifestState();
			this.s2 = t2.manifestState();
		}

		@Override
		public void abort() {
			if (s0 != null)
				s0.reincarnate();
			if (s1 != null)
				s1.reincarnate();
			if (s2 != null)
				s2.reincarnate();
		}

	}

	private static final class UndoGoalFourStates extends UndoGoal {

		private final State s0;
		private final State s1;
		private final State s2;
		private final State s3;

		UndoGoalFourStates(Term t0, Term t1, Term t2, Term t3) {
			this.s0 = t0.manifestState();
			this.s1 = t1.manifestState();
			this.s2 = t2.manifestState();
			this.s3 = t3.manifestState();
		}

		@Override
		public void abort() {
			if (s0 != null)
				s0.reincarnate();
			if (s1 != null)
				s1.reincarnate();
			if (s2 != null)
				s2.reincarnate();
			if (s3 != null)
				s3.reincarnate();
		}

	}

	@Override
	public final boolean next() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean choiceCommitted() {
		throw new UnsupportedOperationException();
	}

	//
	//
	// F A C T O R Y M E T H O D S
	//
	//

	public final static UndoGoal create(State state) {
		if (state != null)
			return new UndoGoalOneNonTrivialState(state);
		else
			return DO_NOTHING_UNDO_GOAL;
	}

	public final static UndoGoal create(Term term) {
		State state = term.manifestState();
		if (state != null)
			return new UndoGoalOneNonTrivialState(state);
		else
			return DO_NOTHING_UNDO_GOAL;
	}

	public final static UndoGoal create(Term s0, Term s1) {
		return new UndoGoalTwoStates(s0, s1);
	}

	public final static UndoGoal create(Term s0, Term s1, Term s2) {
		return new UndoGoalThreeStates(s0, s1, s2);
	}

	public final static UndoGoal create(Term s0, Term s1, Term s2, Term s3) {
		return new UndoGoalFourStates(s0, s1, s2, s3);
	}

}
