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

import static saere.StringAtom.NUMBER;
import saere.Goal;
import saere.PredicateFactoryOneArg;
import saere.PredicateIdentifier;
import saere.PredicateRegistry;
import saere.Term;

/**
 * Prolog's "number/1" predicate.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public class Number1 extends TestGoal {

	public final static PredicateIdentifier IDENTIFIER = new PredicateIdentifier(NUMBER, 1);

	public final static PredicateFactoryOneArg FACTORY = new PredicateFactoryOneArg() {

		@Override
		public Goal createInstance(Term t) {
			return new Number1(t);
		}
	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(IDENTIFIER, FACTORY);
	}

	private final Term t;
	private boolean called = false;

	public Number1(Term t) {
		this.t = t;
	}

	@Override
	public boolean next() {
		if (!called) {
			called = true;
			return isNumber(t);
		} else {
			return false;
		}
	}

	public static final boolean isNumber(Term t) {
		return t.reveal().termTypeID() >= Term.NUMBER_TYPE_ID;
	}
}