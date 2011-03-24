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

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.WeakHashMap;
import static saere.PredicateRegistry.predicateRegistry;

/**
 * Representation of a Prolog string atom.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class StringAtom extends Atomic {

	private final byte[] value;
	private final int hashCode;

	private StringAtom(byte[] title) {
		this.value = title;
		this.hashCode = java.util.Arrays.hashCode(title);
	}

	public int termTypeID() {
		return Term.STRING_ATOM_TYPE_ID;
	}

	@Override
	public boolean isFloatValue() {
		return false;
	}

	@Override
	public boolean isIntValue() {
		return false;
	}

	@Override
	public boolean isStringAtom() {
		return true;
	}

	@Override
	public StringAtom asStringAtom() {
		return this;
	}

	@Override
	public StringAtom functor() {
		return this;
	}


	public boolean mayUnify(Term t) {
		return t == this;
	}

	/**
	 * Compares this StringAtom object with the given one and returns <code>true</code> if this one
	 * and the other one represent the same string atom.
	 * 
	 * @param other
	 *            Some StringAtom.
	 * @return <code>true</code> if this and the other <code>StringAtom</code> represent the same
	 *         atom (basically, the same String).
	 */
	public boolean sameAs(StringAtom other) {
		// StringAtoms are always "interned"...
		return this == other;
	}

	/**
	 * Tests if this string atom and the other object represent the same string atom.
	 * <p>
	 * <b>Performance Guidelines</b><br/>
	 * This method is not intended to be called by clients of StringAtom. Clients of StringAtom
	 * should use {@link #sameAs(StringAtom)}.
	 * </p>
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof StringAtom) {
			StringAtom other = (StringAtom) object;
			return java.util.Arrays.equals(this.value, other.value);
		} else {
			return false;
		}
	}

	/**
	 * @return This StringAtom's hash value.
	 *         <p>
	 *         <b>Performance Guidelines</b><br/>
	 *         Since the hashCode is calculated at instantiation time, caching this value on the
	 *         client side is meaningless.
	 *         </p>
	 */
	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public String toProlog() {
		String s = (new String(this.value)).replace("\\", "\\\\");
		if (Character.isUpperCase(s.charAt(0)) || s.charAt(0) == '_')
			s = "'" + s + "'";
		return s;
	}

	@Override
	public Goal call() {
		PredicateIdentifier pi = new PredicateIdentifier(this, 0);
		PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
		return ((PredicateFactoryNoArgs) pf).createInstance();
	}

	/**
	 * @return This StringAtom's raw value. The byte array must not be changed. <br />
	 *         <font color="red">This is a private method of the runtime environment which is public
	 *         only for technical reasons.</font>
	 */
	public byte[] rawValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "StringAtom[" + new String(value) + "]";
	}

	private final static WeakHashMap<StringAtom, WeakReference<StringAtom>> cache = new WeakHashMap<StringAtom, WeakReference<StringAtom>>();

	public final static Charset UTF8Charset = Charset.forName("UTF-8");

	@SuppressWarnings("all")
	public final static StringAtom get(String s) {
		return get(s.getBytes(UTF8Charset));
	}

	/**
	 * @param title
	 *            a UTF-8 encoded string.
	 */
	@SuppressWarnings("all")
	public final static StringAtom get(byte[] title) {
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

	public static final StringAtom UNIFY = get("=");
	public static final StringAtom TERM_EQUALITY = get("==");
	public static final StringAtom DOES_NOT_UNIFY = get("\\=");

	public static final StringAtom AND = get(",");
	public static final StringAtom OR = get(";");
	public static final StringAtom CUT = get("!");
	public static final StringAtom SOFT_CUT = get("*->");
	public static final StringAtom IF_THEN = get("->");
	public static final StringAtom TRUE = get("true");
	public static final StringAtom FALSE = get("false");
	public static final StringAtom FAIL = get("fail");
	public static final StringAtom NOT = get("not");
	public static final StringAtom NOT_OPERATOR = get("\\+");

	public static final StringAtom IS = get("is");
	public static final StringAtom MULT = get("*");
	public static final StringAtom MINUS = get("-");
	public static final StringAtom PLUS = get("+");
	public static final StringAtom INT_DIV = get("//");
	public static final StringAtom MOD = get("mod");
	public static final StringAtom ARITH_SMALLER_THAN = get("<");
	public static final StringAtom ARITH_SMALLER_OR_EQUAL_THAN = get("=<");
	public static final StringAtom ARITH_LARGER_THAN = get(">");
	public static final StringAtom ARITH_LARGER_OR_EQUAL_THAN = get(">=");
	public static final StringAtom ARITH_IS_EQUAL = get("=:=");
	public static final StringAtom ARITH_IS_NOT_EQUAL = get("=\\=");

	public static final StringAtom EMPTY_LIST = get("[]");
	public static final StringAtom LIST = get(".");

	public static final StringAtom GROUND = get("ground");
	public static final StringAtom VAR = get("var");
	public static final StringAtom NONVAR = get("nonvar");
	public static final StringAtom ATOM = get("atom");
	public static final StringAtom ATOMIC = get("atomic");
	public static final StringAtom NUMBER = get("number");
	public static final StringAtom INTEGER = get("integer");
	public static final StringAtom FUNCTOR = get("functor");
	public static final StringAtom ARG = get("arg");
	public static final StringAtom CALL = get("call");
	public static final StringAtom UNIV = get("=..");

}