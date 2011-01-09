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
 * A list of state objects that represents a state on its own.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
final public class StatesList implements State {

	private final State state;

	private StatesList next;

	public StatesList(State state) {
		this.state = state;
	}

	public StatesList(State state, StatesList next) {
		this.state = state;
		this.next = next;
	}

	public void reincarnate() {
		StatesList cts = this;
		while (cts != null) {
			State s = cts.state;
			if (s != null)
				s.reincarnate();
			cts = cts.next;
		}
	}

	@Override
	public String toString() {
		StatesList los = next;
		String s = "[" + state.toString();
		while (los != null) {
			s += "," + los.state.toString();
			los = los.next;
		}
		return s += "]";
	}

	public static StatesList prepend(State s, StatesList sl) {
		return new StatesList(s, sl);
	}

	// TODO improve the following implementations..
	public static StatesList prepend(State s1, State s2, StatesList sl) {
		return new StatesList(s2, new StatesList(s1, sl));
	}

	public static StatesList prepend(State s1, State s2, State s3, StatesList sl) {
		return new StatesList(s3, new StatesList(s2, new StatesList(s1, sl)));
	}

	public static StatesList prepend(State s1, State s2, State s3, State s4,
			StatesList sl) {
		return new StatesList(s4, new StatesList(s3, new StatesList(s2,
				new StatesList(s1, sl))));
	}
}
