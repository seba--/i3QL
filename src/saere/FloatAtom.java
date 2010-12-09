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
 * Representation of an integer atom.
 * 
 * @author Michael Eichberg
 */
public final class FloatAtom extends Atom {

	private final double value;

	private FloatAtom(double value) {
		this.value = value;
	}

	@Override
	public boolean isFloatAtom() {
		return true;
	}

	@Override
	public FloatAtom asFloatAtom() {
		return this;
	}

	public StringAtom functor() {
		return StringAtom.instance(Double.toString(value));
	}

	public boolean sameAs(FloatAtom other) {
		return this.value == other.value;
	}

	@Override
	public double floatEval() {
		return value;
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

	public static final FloatAtom FLOAT_ATOM_M1 = new FloatAtom(-1.0);
	public static final FloatAtom FLOAT_ATOM_0 = new FloatAtom(0.0);
	public static final FloatAtom FLOAT_ATOM_1 = new FloatAtom(1.0);

	public static FloatAtom instance(double value) {
		if (value == -1.0)
			return FLOAT_ATOM_M1;
		if (value == 0.0)
			return FLOAT_ATOM_0;
		if (value == 1.0)
			return FLOAT_ATOM_1;
		return new FloatAtom(value);
	}

}