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
package sae

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 11.08.12
 * Time: 11:25
 */

object Demo
{
    type Structural = {def foo(): Unit}

    class Real
    {
        def foo() {
            println ("foo")
        }
    }

    trait ContainerLike[V <: AnyRef, +This <: ContainerLike[V, This]]
    {
        self : This =>
        def addV(v: V)
    }

    trait Container[V <: AnyRef] extends ContainerLike[V, Container[V]]
    {

    }


    class ContainerImpl[V <: AnyRef] extends Container[V]
    {
        def addV(v: V) {println("added " + v)}
    }

    def main(args: Array[String]) {
        import Conversions._
        val first = funToConcA ((_: Data).id == 0)

        //val EXISTS : EXISTS_KEYWORD=null

        val fun = (_: Data).property == false

        val c: Container[Structural] = new ContainerImpl[Structural]

        c.addV(new Real)
        //c.addV(new Object) // should be wrong
        //val test1 = first and EXISTS SELECT ()

        //val test = (_:Data).id == 0 OR (_.property == false)
        //val query = first AND (_.name == "Kitty" OR  (_.property == false))
    }
}
