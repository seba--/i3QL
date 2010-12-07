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
package saere.predicate;


import static saere.IntegerAtom.IntegerAtom;
import saere.Solutions;
import saere.State;
import saere.Term;
import saere.Variable;

public class Is2 {

	/**
	 * Implements the "is" operator. This method generates a choice point and
	 * is only intended to be used to execute meta-level calls. 
	 */
	public static Solutions call(final Term a1, final Term a2) {

		return new Solutions() {

			private final State a1State = a1.manifestState();

			private boolean called = false;

			@Override
			public boolean next() {
				if (called) {
					a1.setState(a1State);
					return false;
				}

				called = true;
				final int a2Value = a2.eval();
				return is(a1, a2Value);
			}

			@Override
			public boolean choiceCommitted() {
				return false;
			}

		};
	}

	public static final boolean is(Term a1, int a2Value) {
		if (a1.isVariable()) {
			final Variable v1 = a1.asVariable();
			if (v1.isInstantiated()) {
				return v1.eval() == a2Value;
			} else {
				v1.bind(IntegerAtom(a2Value));
				return true;
			}
		} else {
			return a1.eval() == a2Value;
		}
	}
}
