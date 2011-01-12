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
			return DO_NOTHING_ON_UNDO_GOAL;
	}

	//
	//
	// S P E C I A L I Z E D C L A S S E S
	//
	//

	private static final UndoGoal DO_NOTHING_ON_UNDO_GOAL = new UndoGoal() {

		@Override
		public void abort() {
			// nothing to do
		}

	};

	private static final class UndoGoalOneNonTrivialState extends UndoGoal {
		private final State state;

		UndoGoalOneNonTrivialState(State state) {
			this.state = state;
		}

		@Override
		public void abort() {
			state.reincarnate();
		}

	}
}
