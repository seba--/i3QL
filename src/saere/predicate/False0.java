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

import saere.NoArgsPredicateFactory;
import saere.PredicateFactory;
import saere.PredicateIdentifier;
import saere.PredicateRegistry;
import saere.Solutions;
import saere.StringAtom;

/**
 * Implementation of ISO Prolog's false/0 resp. fail/0 predicate.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class False0 implements Solutions {

	public final static PredicateIdentifier FAIL_IDENTIFIER = new PredicateIdentifier(
			StringAtom.FAIL_FUNCTOR, 0);
	public final static PredicateIdentifier FALSE_IDENTIFIER = new PredicateIdentifier(
			StringAtom.FALSE_FUNCTOR, 0);

	public final static PredicateFactory FACTORY = new NoArgsPredicateFactory() {

		@Override
		public Solutions createInstance() {
			return FALSE0;
		}
	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(FAIL_IDENTIFIER, FACTORY);
		registry.register(FALSE_IDENTIFIER, FACTORY);
	}

	public static final False0 FALSE0 = new False0();

	public False0() {
		// nothing to do
	}

	public boolean next() {
		return false;
	}

	@Override
	public boolean choiceCommitted() {
		return false;
	}

	@Override
	public void abort() {
		// nothing to do
	}

}
