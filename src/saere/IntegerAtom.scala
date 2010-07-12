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
package saere


/**
 * Representation of an integer atom.
 * 
 * @author Michael Eichberg
 */
final class IntegerAtom private(
	val value : Int
) extends Atom {

	override def isIntegerAtom = true

	override def asIntegerAtom = this

	def functor : StringAtom = StringAtom(value.toString)
	
	def sameAs (other : IntegerAtom) : Boolean = this.value == other.value
		
	override def eval() = value
	
	override def toString = Integer.toString(value)

}

object IntegerAtom {

	val IntegerAtom_M3 = new IntegerAtom(-3)
	val IntegerAtom_M2 = new IntegerAtom(-2)	
	val IntegerAtom_M1 = new IntegerAtom(-1)
	val IntegerAtom_0 = new IntegerAtom(0)	
	val IntegerAtom_1 = new IntegerAtom(1)
	val IntegerAtom_2 = new IntegerAtom(2)
	val IntegerAtom_3 = new IntegerAtom(3)
	val IntegerAtom_4 = new IntegerAtom(4)
	val IntegerAtom_5 = new IntegerAtom(5)
	val IntegerAtom_6 = new IntegerAtom(6)
	val IntegerAtom_7 = new IntegerAtom(7)
	val IntegerAtom_8 = new IntegerAtom(8)
	val IntegerAtom_9 = new IntegerAtom(9)

	def apply (value : Int) = value match {
		case -3 => IntegerAtom_M3
		case -2 => IntegerAtom_M2
		case -1 => IntegerAtom_M1
		case 0 => IntegerAtom_0
		case 1 => IntegerAtom_1
		case 2 => IntegerAtom_2
		case 3 => IntegerAtom_3
		case 4 => IntegerAtom_4
		case 5 => IntegerAtom_5
		case 6 => IntegerAtom_6
		case 7 => IntegerAtom_7
		case 8 => IntegerAtom_8			
		case 9 => IntegerAtom_9	
		case _ => new IntegerAtom(value)
	}
	
}