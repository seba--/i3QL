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
 * An immutable list of state objects that represents a state on its own.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class StatesList implements State {

	private final State state;

	private final StatesList next;

	StatesList(State state) {
		this(state, null);
	}

	StatesList(State state, StatesList next) {
		assert state != null;

		this.state = state;
		this.next = next;
	}

	public void reincarnate() {
		StatesList cts = this;
		while (cts != null) {
			cts.state.reincarnate();
			cts = cts.next;
		}
	}

	@Override
	public String toString() {
		StatesList los = next;
		String s = "StatesList[" + state.toString();
		while (los != null) {
			s += "," + los.state.toString();
			los = los.next;
		}
		return s += "]";
	}

	public final static StatesList prepend(State s, StatesList sl) {
		if (s == null)
			return null;
		else
			return new StatesList(s, sl);
	}

	public final static StatesList prepend(State s1, State s2, StatesList sl) {
		StatesList rest = prepend(s2, sl);
		if (s1 != null)
			return new StatesList(s1, rest);
		else
			return rest;
	}

	public final static StatesList prepend(State s1, State s2, State s3, StatesList sl) {
		StatesList rest = prepend(s2, s3, sl);
		if (s1 != null)
			return new StatesList(s1, rest);
		else
			return rest;
	}

	public final static StatesList prepend(State s1, State s2, State s3, State s4, StatesList sl) {
		StatesList rest = prepend(s2, s3, s4, sl);
		if (s1 != null)
			return new StatesList(s1, rest);
		else
			return rest;
	}

	public final static StatesList prepend(State s1, State s2, State s3, State s4, State s5,
			StatesList sl) {
		StatesList rest = prepend(s2, s3, s4, s5, sl);
		if (s1 != null)
			return new StatesList(s1, rest);
		else
			return rest;
	}

	public final static StatesList prepend(Term t, StatesList sl) {
		return prepend(t.manifestState(), sl);
	}

	public final static StatesList prepend(Term t1, Term t2, StatesList sl) {
		return prepend(t1.manifestState(), t2.manifestState(), sl);
	}

	public final static StatesList prepend(Term t1, Term t2, Term t3, StatesList sl) {
		return prepend(t1.manifestState(), t2.manifestState(), t3.manifestState(), sl);
	}

	public final static StatesList prepend(Term t1, Term t2, Term t3, Term t4, StatesList sl) {
		return prepend(t1.manifestState(), t2.manifestState(), t3.manifestState(),
				t4.manifestState(), sl);
	}

	public final static StatesList prepend(Term t1, Term t2, Term t3, Term t4, Term t5,
			StatesList sl) {
		return prepend(t1.manifestState(), t2.manifestState(), t3.manifestState(),
				t4.manifestState(), t5.manifestState(), sl);
	}
}
