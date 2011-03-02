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
 * Representation of a floating point value. The SAE uses (64Bit) double values.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class FloatValue extends Atomic {

	private final double value;

	private FloatValue(double value) {
		this.value = value;
	}

	@Override
	public final int termTypeID() {
		return Term.FLOAT_VALUE_TYPE_ID;
	}

	@Override
	public boolean isStringAtom() {
		return false;
	}

	@Override
	public boolean isIntValue() {
		return false;
	}

	@Override
	public boolean isFloatValue() {
		return true;
	}

	@Override
	public FloatValue asFloatValue() {
		return this;
	}

	@Override
	public StringAtom functor() {
		return StringAtom.get(Double.toString(value));
	}

	public boolean sameAs(FloatValue other) {
		return this.value == other.value;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof FloatValue && this.sameAs((FloatValue) other);
	}

	@Override
	public int hashCode() {
		return (int) (this.value % Integer.MAX_VALUE);
	}

	@Override
	public double floatEval() {
		return this.value;
	}

	@Override
	public Goal call() {
		throw new IllegalStateException("a float value is not callable");
	}

	@Override
	public String toProlog() {
		return Double.toString(value);
	}

	@Override
	public String toString() {
		return "FloatValue[" + Double.toString(value) + "]";
	}

	public static final FloatValue FLOAT_VALUE_M1 = new FloatValue(-1.0);
	public static final FloatValue FLOAT_VALUE_0 = new FloatValue(0.0);
	public static final FloatValue FLOAT_VALUE_1 = new FloatValue(1.0);

	public static FloatValue get(double value) {
		if (value == -1.0)
			return FLOAT_VALUE_M1;
		if (value == 0.0)
			return FLOAT_VALUE_0;
		if (value == 1.0)
			return FLOAT_VALUE_1;

		return new FloatValue(value);
	}

}