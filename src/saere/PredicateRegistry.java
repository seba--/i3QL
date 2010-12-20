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
import saere.predicate.Member2;
import saere.predicate.Not1;
import saere.predicate.ArithNotEqual2;
import saere.predicate.NotUnify2;
import saere.predicate.Or2;
import saere.predicate.Repeat0;
import saere.predicate.ArithEqual2;
import saere.predicate.Smaller2;
import saere.predicate.Time1;
import saere.predicate.True0;
import saere.predicate.Unify2;
import saere.predicate.Write1;

/**
 * Registry of all predicates.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public class PredicateRegistry {

	private static final PredicateRegistry PREDICATE_REGISTRY = new PredicateRegistry();

	public static PredicateRegistry predicateRegistry() {
		return PREDICATE_REGISTRY;
	}

	private final Map<PredicateIdentifier, PredicateFactory> predicates;

	private PredicateRegistry() {
		predicates = new HashMap<PredicateIdentifier, PredicateFactory>();

		// Register all default predicate implementations...
		// Alphabetical Order
		And2.registerWithPredicateRegistry(this);
		ArithEqual2.registerWithPredicateRegistry(this);
		ArithNotEqual2.registerWithPredicateRegistry(this);
		Cut0.registerWithPredicateRegistry(this);
		False0.registerWithPredicateRegistry(this);
		Is2.registerWithPredicateRegistry(this);
		Member2.registerWithPredicateRegistry(this);
		Not1.registerWithPredicateRegistry(this);
		NotUnify2.registerWithPredicateRegistry(this);
		Or2.registerWithPredicateRegistry(this);
		Repeat0.registerWithPredicateRegistry(this);
		Smaller2.registerWithPredicateRegistry(this);
		Time1.registerWithPredicateRegistry(this);
		True0.registerWithPredicateRegistry(this);
		Unify2.registerWithPredicateRegistry(this);
		Write1.registerWithPredicateRegistry(this);
	}

	// public void register(StringAtom functor, int arity,
	// PredicateInstanceFactory factory) {
	// PredicateIdentifier p = new PredicateIdentifier(functor, arity);
	// if (predicates.put(p, factory) != null)
	// throw new IllegalStateException("a predicate instance factory for "
	// + p + " was previously registered");
	// }
	public void register(PredicateIdentifier identifier,
			PredicateFactory factory) {
		if (predicates.put(identifier, factory) != null)
			throw new IllegalStateException("a predicate instance factory for "
					+ identifier + " was previously registered");
	}

	// public Solutions createPredicateInstance(StringAtom functor, Term[] args)
	// {
	// PredicateIdentifier p = new PredicateIdentifier(functor, args.length);
	// PredicateInstanceFactory pif = predicates.get(p);
	// assert (pif != null) : functor;
	// return pif.createPredicateInstance(args);
	// }

	public PredicateFactory getPredicateFactory(PredicateIdentifier identifier) {
		PredicateFactory pf = predicates.get(identifier);
//		assert (pf != null) : identifier.getFunctor().toProlog() + "/"
//				+ identifier.getArity();
		return pf;
	}

}
