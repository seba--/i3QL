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

import saere.*;

/**
 * Implementation of ISO Prolog's or (<code>;</code>) operator.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class Or2 implements Solutions {

	public final static PredicateIdentifier IDENTIFIER = new PredicateIdentifier(
			StringAtom.OR_FUNCTOR, 2);

	public final static PredicateFactory FACTORY = new TwoArgsPredicateFactory() {

		@Override
		public Solutions createInstance(Term t1, Term t2) {
			return new Or2(t1, t2);
		}

	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(IDENTIFIER, FACTORY);
	}

	private final Term l;
	private final Term r;

	private boolean choiceCommitted = false;

	private int goalToExecute = 0;
	private GoalStack goalStack = GoalStack.emptyStack();

	public Or2(final Term l, final Term r) {
		this.l = l;
		this.r = r;
	}

	public boolean next() {
		while (true) {
			switch (goalToExecute) {
			case 0:
				// prepare left goal...
				goalStack = goalStack.put(l.call());
				goalToExecute = 1;
			case 1: {
				// evaluate left goal...
				Solutions s = goalStack.peek();
				if (s.next()) {
					return true;
				}

				// the left goal failed...
				if (s.choiceCommitted()) {
					choiceCommitted = true;
					return false;
				}

				// prepare right goal...
				goalStack = GoalStack.emptyStack();
				goalStack = goalStack.put(r.call());
				goalToExecute = 2;

			}
			case 2: {
				// evaluate right goal...
				Solutions s = goalStack.peek();
				if (s.next()) {
					return true;
				}

				// the right goal (also) failed...
				choiceCommitted = s.choiceCommitted();
				return false;
			}
			}
		}
	}

	@Override
	public void abort() {
		while (goalStack.isNotEmpty()) {
			goalStack.peek().abort();
			goalStack = goalStack.drop();
		}
	}

	@Override
	public boolean choiceCommitted() {
		return choiceCommitted;
	}
}
