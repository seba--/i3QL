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

import java.util.HashMap;
import java.util.Map;

import saere.predicate.And2;
import saere.predicate.Cut0;
import saere.predicate.False0;
import saere.predicate.Is2;
import saere.predicate.Not1;
import saere.predicate.NotSame2;
import saere.predicate.NotUnify2;
import saere.predicate.Or2;
import saere.predicate.Repeat0;
import saere.predicate.Same2;
import saere.predicate.Smaller2;
import saere.predicate.Time1;
import saere.predicate.True0;
import saere.predicate.Unify2;
import saere.predicate.Write1;

/**
 * Registry of all predicates.
 * 
 * @author Michael Eichberg
 */
public class PredicateRegistry {

	private final static class Predicate {
		private final StringAtom functor;
		private final int arity;

		Predicate(StringAtom functor, int arity) {
			this.functor = functor;
			this.arity = arity;
		}

		@Override
		public int hashCode() {
			return functor.hashCode() + arity;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Predicate) {
				Predicate otherPredicate = (Predicate) other;
				return this.arity == otherPredicate.arity
						&& this.functor == otherPredicate.functor;
			}
			return false;
		}

		@Override
		public String toString() {
			return functor.toString() + "/" + arity;
		}
	}

	private static final PredicateRegistry PREDICATE_REGISTRY = new PredicateRegistry();

	public static PredicateRegistry instance() {
		return PREDICATE_REGISTRY;
	}

	private final Map<Predicate, PredicateInstanceFactory> predicates;

	private PredicateRegistry() {
		predicates = new HashMap<PredicateRegistry.Predicate, PredicateInstanceFactory>();

		// Register all default predicate implementations...
		// Alphabetical Order
		And2.registerWithPredicateRegistry(this);
		Cut0.registerWithPredicateRegistry(this);
		False0.registerWithPredicateRegistry(this);
		Is2.registerWithPredicateRegistry(this);
		Not1.registerWithPredicateRegistry(this);
		NotSame2.registerWithPredicateRegistry(this);
		NotUnify2.registerWithPredicateRegistry(this);
		Or2.registerWithPredicateRegistry(this);
		Repeat0.registerWithPredicateRegistry(this);
		Same2.registerWithPredicateRegistry(this);
		Smaller2.registerWithPredicateRegistry(this);
		Time1.registerWithPredicateRegistry(this);
		True0.registerWithPredicateRegistry(this);
		Unify2.registerWithPredicateRegistry(this);
		Write1.registerWithPredicateRegistry(this);
	}

	public void register(StringAtom functor, int arity,
			PredicateInstanceFactory factory) {
		Predicate p = new Predicate(functor, arity);
		if (predicates.put(p, factory) != null)
			throw new IllegalStateException("a predicate instance factory for "
					+ p + " was previously registered");
	}

	public Solutions createPredicateInstance(StringAtom functor, Term[] args) {
		Predicate p = new Predicate(functor, args.length);
		PredicateInstanceFactory pif = predicates.get(p);
		return pif.createPredicateInstance(args);

	}

}
