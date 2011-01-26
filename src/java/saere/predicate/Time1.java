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

import saere.Goal;
import saere.PredicateFactoryOneArg;
import saere.PredicateIdentifier;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Term;

/**
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class Time1 implements Goal {

	public final static PredicateIdentifier IDENTIFIER = new PredicateIdentifier(
			StringAtom.get("time"), 1);

	public final static PredicateFactoryOneArg FACTORY = new PredicateFactoryOneArg() {

		@Override
		public Goal createInstance(Term t) {
			return new Time1(t);
		}
	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(IDENTIFIER, FACTORY);
	}

	private Goal goal;

	public Time1(final Term t) {

		this.goal = t.call();
	}

	public boolean next() {
		final long startTime = System.nanoTime();
		final boolean succeeded = goal.next();
		final double duration = ((System.nanoTime() - startTime)) / 1000.0 / 1000.0 / 1000.0;
		System.out.printf("%% Finished in %7.4f secs.\n", new Double(duration));
		return succeeded;
	}

	@Override
	public void abort() {
		goal.abort();
	}

	@Override
	public boolean choiceCommitted() {
		return false;
	}

}
