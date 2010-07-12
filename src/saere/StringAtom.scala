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

import java.nio.charset.Charset;
import java.util.WeakHashMap
import java.lang.ref.WeakReference


/**
 * Representation of a string atom.
 *  
 * @author Michael Eichberg
 */
final class StringAtom private(
	private val title : Array[Byte]
) extends Atom {

	override def isStringAtom = true

	override def asStringAtom = this
	
	def functor : StringAtom = this
	
	def sameAs (other : StringAtom) : Boolean = {
		// StringAtoms are always "interned"...
		this eq other
	}
		
	/**
	 * Tests if this StringAtom and the other object
	 * represent the same string atom.
	 * <p>
	 * This method is not intended to be called by clients
	 * of StringAtom. Clients of StringAtom should use
	 * {@link #sameAs(StringAtom)}.
	 * </p>
	 */
	override def equals(other : Any) : Boolean = other match {
		case other_sa : StringAtom => java.util.Arrays.equals(this.title, other_sa.title) 
		case _ => false
	}
	
	/**
	 * @return the hashcode value as calculated by java.util.Arrays.hashCode(title)
	 */
	override def hashCode() : Int =
		// hashCode is only called once (when put in the cache)
		java.util.Arrays.hashCode(title)
	
	override def toString = new String(title)
	
}
object StringAtom {

	private val cache = new WeakHashMap[StringAtom,WeakReference[StringAtom]]

	val UTF8Charset : Charset  = Charset.forName("UTF-8")

	def apply (s : String) : StringAtom = apply(s.getBytes(UTF8Charset))
	
	/**
	 * @param title a UTF-8 encoded string.
	 */
	def apply (title : Array[Byte]) : StringAtom = {
		val cand = new StringAtom(title)
		cache.synchronized {
			var interned = cache.get(cand)
			if (interned == null) { 
				interned = new WeakReference(cand)
				cache.put(cand,interned)
			}
			
			interned.get
		}
	}
	
	val emptyList = StringAtom("[]")
}