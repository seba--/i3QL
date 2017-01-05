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
package idb.lms.extensions.print

import scala.virtualization.lms.common._
import java.io.{PrintWriter, StringWriter}

import idb.lms.extensions.{FunctionUtils, ScalaCodeGenPkgExtensions, ScalaOpsPkgExpExtensions}
import idb.lms.extensions.operations._

/**
 *
 * @author Ralf Mitschke
 */
trait QuoteFunction
    extends ScalaCodeGenPkgExtensions
    with CodeGenIndent
{

    override val IR: ScalaOpsPkgExpExtensions with FunctionUtils

    import IR._

    def quoteBlock[A] (b : Block[A]): String = {
        val writer = new StringWriter ()
        stream = new PrintWriter (writer)
        emitBlock(b)
        stream.close ()
        writer.toString
    }


    def quoteFunction[A, B] (f: Exp[A => B]): String = {
        val writer = new StringWriter ()
        stream = new PrintWriter (writer)
        f match {
            case Def (Lambda (fun, x, y)) => {
                stream.print (indent)
                x match {
                    case UnboxedTuple (xs) => {
                        stream.print ("(" + xs.map (s => quote (s) + ":" + remap (s.tp)).mkString ("(", ",", ")") + ")")
                    }
                    case _ => {
                        stream.print ("(" + quote (x) + ": " + x.tp + ")")
                    }
                }
                stream.print (": " + returnType(f))
                stream.println (" => {")
                addIndent ()
                emitBlock (y)
                stream.print (indent)
                stream.println (quote (getBlockResult (y)))
                removeIndent ()
                stream.print (indent)
                stream.print ("}")
            }

            case _ => "f(x: " + f.tp.typeArguments(0) + "): " + f.tp.typeArguments(1)

        }


        stream.close ()
        writer.toString
    }

    override def emitValDef (sym: Sym[Any], rhs: String) {
        stream.print (indent)
        super.emitValDef (sym, rhs)
    }

    override def emitVarDef (sym: Sym[Variable[Any]], rhs: String) {
        stream.print (indent)
        super.emitVarDef (sym, rhs)
    }
}

