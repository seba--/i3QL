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
 * Encapsulate's the state of a compound term's arguments; i.e., the state of the arguments which
 * are (free) variables.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
final class CompoundTermState implements State {

	/**
	 * The state of a complex term's argument.
	 */
	private final State state;

	private CompoundTermState next;

	private CompoundTermState(State state) {
		this.state = state;
	}

	CompoundTermState append(@SuppressWarnings("hiding") State state) {
		CompoundTermState tail = new CompoundTermState(state);
		this.next = tail;
		return tail;
	}

	public void reincarnate() {
		CompoundTermState cts = this;
		while (cts != null) {
			cts.state.reincarnate();
			cts = cts.next;
		}
	}

	@Override
	public String toString() {
		CompoundTermState los = next;
		String s = "[" + state;
		while (los != null) {
			s += "," + los.toString();
			los = los.next;
		}
		return s += "]";
	}

	@SuppressWarnings("all")
	// REMARK ... we just wanted to suppress the "hiding" related warning...
	static CompoundTermState manifest(CompoundTerm complexTerm) {
		// Compared to the recursive implementation,
		// this implementation is ~5-10% faster (overall!).
		CompoundTermsList workList = null;

		CompoundTermState first = null;
		CompoundTermState last = null;

		do {
			final int arity = complexTerm.arity();
			for_each_argument: for (int i = 0; i < arity; i++) {
				// We needed to integrate the state handling of 
				// variables here, to avoid stack overflow errors.
				// E.g., if a complex term is bound to a variable 
				// the manifestation of the variable would lead to 
				// another call of the manifest method. If we now 
				// manifest a large datastructure (e.g., a long list)
				// which contains a large number of (bound) variables
				// this easily leads to a stack overflow error.
				Term arg_i = complexTerm.arg(i).unwrap();
				switch (arg_i.termTypeID()) {
				case Term.VARIABLE:
					Variable variable = arg_i.asVariable();
					Term value = variable.getValue();
					if (value == null) {
						if (first == null)
							last = first = new CompoundTermState(variable);
						else
							last = last.append(variable);
					}
					break;

				case Term.COMPUND_TERM:
					if (workList == null)
						workList = new CompoundTermsList(arg_i.asCompoundTerm());
					else
						workList = workList.prepend(arg_i.asCompoundTerm());
					break;
				}
			}
			if (workList != null) {
				complexTerm = workList.first();
				workList = workList.rest();
			} else {
				complexTerm = null;
			}
		} while (complexTerm != null);

		return first;
	}
}
