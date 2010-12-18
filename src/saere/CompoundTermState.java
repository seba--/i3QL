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
 * Encapsulate's the state of a compound term's arguments.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
final class CompoundTermState extends State {

	private final static class ListOfVariableStates {

		private final VariableState state;
		private ListOfVariableStates tail;

		ListOfVariableStates(VariableState state) {
			this.state = state;
		}

		@SuppressWarnings("hiding")
		ListOfVariableStates append(VariableState state) {
			ListOfVariableStates tail = new ListOfVariableStates(state);
			this.tail = tail;
			return tail;
		}

		ListOfVariableStates apply() {
			state.reset();
			return tail;
		}

		@Override
		public String toString() {
			ListOfVariableStates los = tail;
			String s = "[" + state;
			while (los != null) {
				s += "," + los.toString();
				los = los.tail;
			}
			return s += "]";
		}

	}

	private ListOfVariableStates first = null;
	private ListOfVariableStates temp = null;

	CompoundTermState(CompoundTerm compoundTerm) {
		doManifest(compoundTerm);
	}
	
	
	// we only manifest the state of the variables...
	private void doManifest(CompoundTerm compoundTerm) {
		final int arity = compoundTerm.arity(); 
		for (int i =  0; i < arity; i++) {
			Term arg_i = compoundTerm.arg(i);
			if (arg_i.isVariable()) {
				VariableState vs;
				Variable hv = arg_i.asVariable().headVariable();
				Term hvv = hv.getValue();
				if (hvv == null) {
					vs = VariableState.share(hv);
				} else if (hvv.isAtomic()) {
					continue;
				} else {
					vs = VariableState.instantiated(hv);
				}
				if (first == null)
					temp = first = new ListOfVariableStates(vs);
				else
					temp = temp.append(vs);
			} else if (arg_i.isCompoundTerm()) {
				doManifest(arg_i.asCompoundTerm());
			}
		}
	}

	void reset() {
		temp = first;
		while (temp != null) {
			temp = temp.apply();
		}
	}

	@Override
	public String toString() {
		return "CompoundTermState[" + first + "]";
	}

	@Override
	CompoundTermState asCompoundTermState() {
		return this;
	}
}
