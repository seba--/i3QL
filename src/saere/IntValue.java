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
 * Representation of an integer value. The SAE uses long values as the basis for integer arithmetic.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class IntValue extends Atomic {

    private final long value;

    private IntValue(long value) {
	this.value = value;
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
	return Term.INT_VALUE;
    }

    @Override
    public boolean equals(Object other) {
	return other instanceof IntValue && this.sameAs((IntValue) other);
    }

    @Override
    public int hashCode() {
	return (int) (value % Integer.MAX_VALUE);
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
	throw new IllegalStateException("calling integer values is not possible");
    }

    @Override
    public String toProlog() {
	return Long.toString(value);
    }

    @Override
    public String toString() {
	return "IntegerValue[" + Long.toString(value) + "]";
    }

    public static final IntValue IntValue_M3 = new IntValue(-3l);
    public static final IntValue IntValue_M2 = new IntValue(-2l);
    public static final IntValue IntValue_M1 = new IntValue(-1l);
    public static final IntValue IntValue_0 = new IntValue(0l);
    public static final IntValue IntValue_1 = new IntValue(1l);
    public static final IntValue IntValue_2 = new IntValue(2l);
    public static final IntValue IntValue_3 = new IntValue(3l);
    public static final IntValue IntValue_4 = new IntValue(4l);
    public static final IntValue IntValue_5 = new IntValue(5l);
    public static final IntValue IntValue_6 = new IntValue(6l);
    public static final IntValue IntValue_7 = new IntValue(7l);
    public static final IntValue IntValue_8 = new IntValue(8l);
    public static final IntValue IntValue_9 = new IntValue(9l);

    @SuppressWarnings("all")
    public final static IntValue get(final long value) {
	if (value > Integer.MIN_VALUE && value < Integer.MAX_VALUE) {
	    int intValue = (int) value;
	    switch (intValue) {

	    case -3:
		return IntValue_M3;
	    case -2:
		return IntValue_M2;
	    case -1:
		return IntValue_M1;
	    case 0:
		return IntValue_0;
	    case 1:
		return IntValue_1;
	    case 2:
		return IntValue_2;
	    case 3:
		return IntValue_3;
	    case 4:
		return IntValue_4;
	    case 5:
		return IntValue_5;
	    case 6:
		return IntValue_6;
	    case 7:
		return IntValue_7;
	    case 8:
		return IntValue_8;
	    case 9:
		return IntValue_9;
	    default:
		return new IntValue(value);
	    }
	} else {
	    return new IntValue(value);
	}
    }

}