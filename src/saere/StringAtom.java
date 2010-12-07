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

import java.nio.charset.Charset;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;


/**
 * Representation of a string atom.
 * 
 * @author Michael Eichberg
 */
public final class StringAtom extends Atom {
 
	private final byte[] title;
	//private final int hashCode;

	private StringAtom(byte[] title) {
		this.title = title;
		//hashCode = java.util.Arrays.hashCode(title);
	}

	@Override
	public boolean isStringAtom() {
		return true;
	}

	@Override
	public StringAtom asStringAtom() {
		return this;
	}

	public StringAtom functor() {
		return this;
	}

	public boolean sameAs(StringAtom other) {
		// StringAtoms are always "interned"...
		return this == other;
	}

	/**
	 * Tests if this StringAtom and the other object represent the same string
	 * atom.
	 * <p>
	 * This method is not intended to be called by clients of StringAtom.
	 * Clients of StringAtom should use {@link #sameAs(StringAtom)}.
	 * </p>
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof StringAtom) {
			StringAtom other_sa = (StringAtom) other;
			return java.util.Arrays.equals(this.title, other_sa.title);
		} else {
			return false;
		}
	}

	/**
	 * @return the hashcode value as calculated by
	 *         java.util.Arrays.hashCode(title)
	 */
	public int hashCode() {
		// hashCode is only called once (when put in the cache)
		// TODO check if it is worth to calcultate the hashcode once 
		return java.util.Arrays.hashCode(title);
		//return hashCode;
	}

	@Override
	public String toString() {
		return new String(title);
	}


	public Solutions call(){
		
		return PredicateRegistry.instance().createPredicateInstance(this, Term.NO_TERMS);
		
	}
	
	
	private final static WeakHashMap<StringAtom, WeakReference<StringAtom>> cache = new WeakHashMap<StringAtom, WeakReference<StringAtom>>();

	public final static Charset UTF8Charset = Charset.forName("UTF-8");

	@SuppressWarnings("all")
	public static final StringAtom StringAtom(String s) {
		return StringAtom(s.getBytes(UTF8Charset));
	}

	/**
	 * @param title
	 *            a UTF-8 encoded string.
	 */
	@SuppressWarnings("all")
	public final static StringAtom StringAtom(byte[] title) {
		final StringAtom cand = new StringAtom(title);
		synchronized (cache) {
			WeakReference<StringAtom> interned = cache.get(cand);
			if (interned == null) {
				interned = new WeakReference<StringAtom>(cand);
				cache.put(cand, interned);
			}

			return interned.get();
		}
	}

	public static final StringAtom EMPTY_LIST_FUNCTOR = StringAtom("[]");
	public static final StringAtom AND_FUNCTOR = StringAtom(",");
	public static final StringAtom OR_FUNCTOR = StringAtom(";");
	public static final StringAtom CUT_FUNCTOR = StringAtom("!");
	public static final StringAtom SOFT_CUT_FUNCTOR = StringAtom("*->");
	public static final StringAtom IF_THEN_FUNCTOR = StringAtom("->");
}