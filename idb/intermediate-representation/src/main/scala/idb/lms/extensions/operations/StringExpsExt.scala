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
package idb.lms.extensions.operations

import scala.virtualization.lms.common.{ScalaGenStringOps, StringOpsExp}
import scala.reflect.SourceContext

/**
 *
 * @author Ralf Mitschke , Mirko Köhler
 */
trait StringOpsExpExt
    extends StringOpsExp
{

	def infix_indexOf (s: Rep[String], c: Rep[String])(implicit pos: SourceContext): Rep[Int] =
		string_indexOf (s, c)

	def string_indexOf (s: Rep[String], c: Rep[String])(implicit pos: SourceContext): Rep[Int] =
		StringIndexOf (s, c)

    def infix_lastIndexOf (s: Rep[String], c: Rep[Char])(implicit pos: SourceContext): Rep[Int] =
        string_lastIndexOf (s, c)

    def string_lastIndexOf (s: Rep[String], c: Rep[Char])(implicit pos: SourceContext): Rep[Int] =
        StringLastIndexOf (s, c)


	def infix_toLowerCase (s: Rep[String])(implicit pos: SourceContext): Rep[String] =
		string_toLowerCase (s)

	def string_toLowerCase (s : Rep[String])(implicit pos: SourceContext): Rep[String] =
		StringToLowerCase (s)

	def infix_substring (s: Rep[String], startIndex : Rep[Int])(implicit pos: SourceContext): Rep[String] =
		string_substring (s, startIndex)

	def string_substring (s: Rep[String], startIndex : Rep[Int])(implicit pos: SourceContext): Rep[String] =
		StringSubstring (s, startIndex, string_length(s))

	def string_matches(s: Rep[String], pattern : Rep[String])(implicit pos: SourceContext): Rep[Boolean] =
		StringMatches(s, pattern)

	case class StringToLowerCase (s : Exp[String]) extends Def[String]
	case class StringIndexOf (s: Exp[String], s1: Exp[String]) extends Def[Int]
    case class StringLastIndexOf (s: Exp[String], c: Exp[Char]) extends Def[Int]
	case class StringMatches (s: Exp[String], pattern: Exp[String]) extends Def[Boolean]


	override def mirror[A: Manifest] (e: Def[A], f: Transformer)(implicit pos: SourceContext): Exp[A] = (e match {
		case StringIndexOf (s, s1) => string_indexOf(f (s), f (s1))
    	case StringLastIndexOf (s, c) => string_lastIndexOf (f (s), f (c))
		case StringEndsWith (s, end) => string_endsWith (f (s), f (end))
		case StringToLowerCase (s) => string_toLowerCase( f (s))
		case StringMatches(s, pattern) => string_matches(f (s), f (pattern))

		case _ => super.mirror (e, f)
    }).asInstanceOf[Exp[A]]
}


trait ScalaGenStringOpsExt extends ScalaGenStringOps
{
    val IR: StringOpsExpExt

    import IR._

    override def emitNode (sym: Sym[Any], rhs: Def[Any]) = rhs match {
      case StringIndexOf (s, s1) => emitValDef (sym, "%s.indexOf(%s)".format (quote (s), quote (s1)))
      case StringLastIndexOf (s, c) => emitValDef (sym, "%s.lastIndexOf(%s)".format (quote (s), quote (c)))
      case StringEndsWith (s, end) => emitValDef (sym, "%s.endsWith(%s)".format (quote (s), quote (end)))
      case StringToLowerCase (s) => emitValDef (sym, "%s.toLowerCase".format (quote (s)))
      case StringMatches (s, pattern) => emitValDef(sym, "%s.matches(%s)".format(quote(s), quote(pattern)))
      case _ => super.emitNode (sym, rhs)
    }
}
