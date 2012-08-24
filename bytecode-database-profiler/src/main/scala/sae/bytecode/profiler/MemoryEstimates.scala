/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
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
package sae.bytecode.profiler

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 23.08.12
 * Time: 17:04
 */

object MemoryEstimates
{

    val objectHouseKeeping = 8

    def get(clazz: Class[_]): Long = {

        val fields = clazz.getDeclaredFields

        val basicFieldEstimate = (for (field <- fields) yield getFieldSize(field.getType)).sum

        basicFieldEstimate + objectHouseKeeping
    }

    private val booleanClass = classOf[Boolean]
    private val byteClass =classOf[Byte]
    private val charClass = classOf[Char]
    private val shortClass = classOf[Short]
    private val intClass = classOf[Int]
    private val floatClass = classOf[Float]
    private val longClass = classOf[Long]
    private val doubleClass = classOf[Double]


    private def getFieldSize(clazz: Class[_]) = clazz match {
        case `booleanClass` => 1
        case  `byteClass` => 1
        case `charClass` => 2
        case `shortClass` => 2
        case `intClass` => 4
        case `floatClass` => 4
        case `longClass` => 8
        case `doubleClass` => 8
        case _ => 4 // assume we have an object or array reference
    }
}
