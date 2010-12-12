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
 * Encapsulates the current state of a variable.
 * 
 * @author Michael Eichberg
 */
abstract class VariableState extends State {

	static final VariableState immutable = new VariableState() {

		@Override
		void apply(Variable variable) {
			// nothing to do
		}

		@Override
		public String toString() {
			return "VariableStateImmutable";
		}
	};

	static final VariableState share(final Variable headVariable) {
		return new VariableState() {

			{
				assert headVariable.getValue() == null : "head variable is bound";
			}

			@Override
			void apply(Variable variable) {
				Variable v = variable;
				while (v != headVariable) {
					v = v.getValue().asVariable();
				}
				v.clear();
			}

			@Override
			public String toString() {
				return "VariableStateShare[" + headVariable + "]";
			}
		};
	}

	static final VariableState instantiated(final Variable headVariable) {
		return new VariableState() {
			
			private final State headVariableBindingState;
			
			{
				assert headVariable.getValue() != null : "the head variable is free; use VariableState.share(Variable) to encapsulate the state";

				headVariableBindingState = headVariable.getValue().manifestState();
			}

			@Override
			void apply(Variable variable) {
				Variable v = variable;
				while (v != headVariable) {
					v = v.getValue().asVariable();
				}
				v.getValue().setState(headVariableBindingState);
			}

			@Override
			public String toString() {
				return "VariableStateInstantiated[headVariableId="
						+ Variable.variableToName(headVariable) + "]";
			}
		};
	}

	@Override
	VariableState asVariableState() {
		return this;
	}

	abstract void apply(Variable variable);
}
