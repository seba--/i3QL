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
final class CompoundTermState implements State {

	private final State state;
	
	private CompoundTermState next;

	private CompoundTermState(State state) {
		this.state = state;
	}

	@SuppressWarnings("hiding")
	CompoundTermState append(State state) {
		CompoundTermState tail = new CompoundTermState(state);
		this.next = tail;
		return tail;
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

	public void reset() {
		CompoundTermState cts = this;
		while (cts != null) {
			cts.state.reset();
			cts = cts.next;
		}
	}

	final static class CompoundTermStatePointers {
		CompoundTermState first;
		CompoundTermState last;
	}

	static CompoundTermState manifest(CompoundTerm compoundTerm) {
		CompoundTermStatePointers pointers = new CompoundTermStatePointers();
		doManifest(compoundTerm, pointers);
		return pointers.first;
	}

	// we only manifest the state of the variables...
	private static void doManifest(CompoundTerm compoundTerm,
			CompoundTermStatePointers pointers) {
		final int arity = compoundTerm.arity();
		for (int i = 0; i < arity; i++) {
			Term arg_i = compoundTerm.arg(i);
			if (arg_i.isVariable()) {
				State vs = arg_i.asVariable().manifestState();
				if (vs == null)
					continue;
				if (pointers.first == null)
					pointers.last = pointers.first = new CompoundTermState(vs);
				else
					pointers.last = pointers.last.append(vs);
			} else if (arg_i.isCompoundTerm()) {
				doManifest(arg_i.asCompoundTerm(), pointers);
			}
		}
	}
}
