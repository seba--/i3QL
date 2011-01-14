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

import saere.PredicateIdentifier;
import saere.PredicateRegistry;
import saere.Goal;
import saere.State;
import saere.StringAtom;
import saere.Term;
import saere.PredicateFactoryTwoArgs;

/**
 * Implementation of Prolog's <code>\=</code> (does not unify) operator.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class NotUnify2 implements Goal {

	public final static PredicateIdentifier IDENTIFIER = new PredicateIdentifier(
			StringAtom.DOES_NOT_UNIFY, 2);

	public final static PredicateFactoryTwoArgs FACTORY = new PredicateFactoryTwoArgs() {

		@Override
		public Goal createInstance(Term t1, Term t2) {
			return new NotUnify2(t1, t2);
		}

	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(IDENTIFIER, FACTORY);
	}

	private final Term l;
	private final Term r;

	private boolean called = false;

	public NotUnify2(Term l, Term r) {
		this.l = l;
		this.r = r;
	}

	public boolean next() {
		if (!called) {
			called = true;

			State lState = l.manifestState();
			State rState = r.manifestState();

			final boolean success = l.unify(r);
			// reset (partial) bindings; in case of "a(X,b(c)) \= a(1,c)." X
			// will not be bound!
			if (lState != null) {
				lState.reincarnate();
				lState = null;
			}
			if (rState != null) {
				rState.reincarnate();
				rState = null;
			}
			return !success;
		} else {
			return false;
		}
	}

	@Override
	public void abort() {
		// nothing to do
	}

	@Override
	public boolean choiceCommitted() {
		return false;
	}
}
