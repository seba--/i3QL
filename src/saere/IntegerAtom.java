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

import static saere.StringAtom.StringAtom;

/**
 * Representation of an integer atom.
 * 
 * @author Michael Eichberg
 */
public final class IntegerAtom extends Atom {

	private final int value;

	private IntegerAtom(int value) {
		this.value = value;
	}

	@Override
	public boolean isIntegerAtom() {
		return true;
	}

	@Override
	public IntegerAtom asIntegerAtom() {
		return this;
	}

	public StringAtom functor() {
		return StringAtom(Integer.toString(value));
	}

	public boolean sameAs(IntegerAtom other) {
		return this.value == other.value;
	}

	@Override
	public int eval() {
		return value;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

	public static final IntegerAtom IntegerAtom_M3 = new IntegerAtom(-3);
	public static final IntegerAtom IntegerAtom_M2 = new IntegerAtom(-2);
	public static final IntegerAtom IntegerAtom_M1 = new IntegerAtom(-1);
	public static final IntegerAtom IntegerAtom_0 = new IntegerAtom(0);
	public static final IntegerAtom IntegerAtom_1 = new IntegerAtom(1);
	public static final IntegerAtom IntegerAtom_2 = new IntegerAtom(2);
	public static final IntegerAtom IntegerAtom_3 = new IntegerAtom(3);
	public static final IntegerAtom IntegerAtom_4 = new IntegerAtom(4);
	public static final IntegerAtom IntegerAtom_5 = new IntegerAtom(5);
	public static final IntegerAtom IntegerAtom_6 = new IntegerAtom(6);
	public static final IntegerAtom IntegerAtom_7 = new IntegerAtom(7);
	public static final IntegerAtom IntegerAtom_8 = new IntegerAtom(8);
	public static final IntegerAtom IntegerAtom_9 = new IntegerAtom(9);

	public final static IntegerAtom IntegerAtom(final int value) {
		switch (value) {

		case -3:
			return IntegerAtom_M3;
		case -2:
			return IntegerAtom_M2;
		case -1:
			return IntegerAtom_M1;
		case 0:
			return IntegerAtom_0;
		case 1:
			return IntegerAtom_1;
		case 2:
			return IntegerAtom_2;
		case 3:
			return IntegerAtom_3;
		case 4:
			return IntegerAtom_4;
		case 5:
			return IntegerAtom_5;
		case 6:
			return IntegerAtom_6;
		case 7:
			return IntegerAtom_7;
		case 8:
			return IntegerAtom_8;
		case 9:
			return IntegerAtom_9;
		default:
			return new IntegerAtom(value);
		}
	}

}