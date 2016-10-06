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
package idb.demo.chartparser.schema


import idb.syntax.iql.IR._

/**
 *
 * @author Ralf Mitschke
 */
trait ParserSchema
{


    type Edge = (Int, Int, Category, Seq[Category])

    implicit def edgeOrdering (): Ordering[Edge] = new Ordering[Edge]
    {
        def compare (x: Edge, y: Edge): Int = {
            val comp1 = x._1.compareTo (y._1)
            if (comp1 != 0)
                return comp1
            val comp2 = x._2.compareTo (y._2)
            if (comp2 != 0)
                return comp2
            val comp3 = x._3.compareTo (y._3)
            if (comp3 != 0)
                return comp3
            val comp4 = if(x._4 == y._4) 0 else 1
            return comp4
        }
    }

    type Category = String

    type Terminal = (String, Category)

    type InputToken = (String, Int)

    type Production = (Category, Seq[Category])

    def PassiveEdge (i: Rep[Int], j: Rep[Int], A: Rep[Category]): Rep[Edge] = (i, j, A, List ())

    def ActiveEdge (i: Rep[Int], j: Rep[Int], A: Rep[Category], next: Rep[Seq[Category]]): Rep[Edge] = (i, j, A, next)

    implicit def edgeToInfixOps (t: Rep[Edge]) =
        EdgeInfixOps (t)

    implicit def tokenToInfixOps (t: Rep[InputToken]) =
        TokenInfixOps (t)

    implicit def terminalToInfixOps (t: Rep[Terminal]) =
        TerminalInfixOps (t)

    implicit def productionToInfixOps (t: Rep[Production]) =
        ProductionInfixOps (t)


    case class EdgeInfixOps (c: Rep[Edge])
    {

        def start: Rep[Int] = c._1

        def end: Rep[Int] = c._2

        def category: Rep[Category] = c._3

        def next: Rep[Seq[Category]] = c._4

        def isPassive: Rep[Boolean] = next == Nil

        def isActive: Rep[Boolean] = next != Nil
    }

    case class TokenInfixOps (t: Rep[InputToken])
    {
        def value: Rep[String] = t._1

        def position: Rep[Int] = t._2
    }

    case class TerminalInfixOps (t: Rep[Terminal])
    {

        def tokenValue: Rep[String] = t._1

        def category: Rep[Category] = t._2
    }

    case class ProductionInfixOps (t: Rep[Production])
    {

        def head: Rep[Category] = t._1

        def body: Rep[Seq[Category]] = t._2
    }

}
