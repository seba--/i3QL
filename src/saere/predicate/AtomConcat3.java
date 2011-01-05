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
import saere.PredicateIdentifier;
import saere.PredicateRegistry;
import saere.PrologException;
import saere.State;
import saere.StringAtom;
import saere.Term;
import saere.PredicateFactoryThreeArgs;

/**
 * Implementation of ISO Prolog's atom_concat/3 predicate.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class AtomConcat3 implements Goal {

	public final static PredicateIdentifier IDENTIFIER = new PredicateIdentifier(
			StringAtom.get("atom_concat"), 3);

	public final static PredicateFactoryThreeArgs FACTORY = new PredicateFactoryThreeArgs() {

		@Override
		public Goal createInstance(Term l, Term r, Term o) {
			return new AtomConcat3(l, r, o);
		}

	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(IDENTIFIER, FACTORY);
	}

	private boolean call = true;

	private Term l;
	private Term r;
	private Term o;

	private State oState;

	public AtomConcat3(final Term l, final Term r, final Term o) {
		if (!l.isAtomic() || !r.isAtomic()) {
			throw new PrologException("The terms are not sufficiently instantiated.");
		}

		this.l = l.unwrap();
		this.r = r.unwrap();
		this.o = o.unwrap();
		this.oState = o.manifestState();
	}

	public void abort() {
		if (oState != null)
			oState.reincarnate();
	}

	public boolean choiceCommitted() {
		return false;
	}

	public boolean next() {
		if (call) {
			StringAtom a = StringAtom.get(l.toProlog() + r.toProlog());
			if (o.unify(a))
				return true;
		}

		if (oState != null)
			oState.reincarnate();
		return false;
	}
}
