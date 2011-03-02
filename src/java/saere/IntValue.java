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

/**
 * Representation of a Prolog integer value. The SAE uses long values as the basis for integer
 * arithmetic.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class IntValue extends Atomic {

	private final long value;

	private IntValue(long value) {
		this.value = value;
	}

	@Override
	public boolean isStringAtom() {
		return false;
	}

	@Override
	public boolean isFloatValue() {
		return false;
	}

	@Override
	public boolean isIntValue() {
		return true;
	}

	@Override
	public IntValue asIntValue() {
		return this;
	}

	@Override
	public StringAtom functor() {
		return StringAtom.get(Long.toString(value));
	}

	@Override
	public final int termTypeID() {
		return Term.INT_VALUE_TYPE_ID;
	}

	public boolean sameAs(IntValue other) {
		return this.value == other.value;
	}

	@Override
	public long intEval() {
		return value;
	}

	@Override
	public Goal call() {
		throw new PrologException("an integer value is not callable");
	}

	@Override
	public String toProlog() {
		return Long.toString(value);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof IntValue && this.sameAs((IntValue) other);
	}

	@Override
	public int hashCode() {
		return (int) (value % Integer.MAX_VALUE);
	}

	@Override
	public String toString() {
		return "IntegerValue[" + Long.toString(value) + "]";
	}

	public static final IntValue[] INT_VALUES;

	public static final IntValue IntValue_M3;
	public static final IntValue IntValue_M2;
	public static final IntValue IntValue_M1;
	public static final IntValue IntValue_0;
	public static final IntValue IntValue_1;
	public static final IntValue IntValue_2;
	public static final IntValue IntValue_3;
	public static final IntValue IntValue_4;
	public static final IntValue IntValue_5;
	public static final IntValue IntValue_6;
	public static final IntValue IntValue_7;
	public static final IntValue IntValue_8;
	public static final IntValue IntValue_9;

	static {
		INT_VALUES = new IntValue[2000];
		long l = -1000l;
		for (int i = 0; i < 2000; i++) {
			INT_VALUES[i] = new IntValue(l++);
		}

		IntValue_M3 = IntValue.get(-3l);
		IntValue_M2 = IntValue.get(-2l);
		IntValue_M1 = IntValue.get(-1l);
		IntValue_0 = IntValue.get(0l);
		IntValue_1 = IntValue.get(1l);
		IntValue_2 = IntValue.get(2l);
		IntValue_3 = IntValue.get(3l);
		IntValue_4 = IntValue.get(4l);
		IntValue_5 = IntValue.get(5l);
		IntValue_6 = IntValue.get(6l);
		IntValue_7 = IntValue.get(7l);
		IntValue_8 = IntValue.get(8l);
		IntValue_9 = IntValue.get(9l);
	}

	@SuppressWarnings("all")
	public final static IntValue get(final long value) {
		if (value >= -1000l && value < 1000l) {
			return INT_VALUES[((int) value) + 1000];
		} else {
			return new IntValue(value);
		}
	}

}