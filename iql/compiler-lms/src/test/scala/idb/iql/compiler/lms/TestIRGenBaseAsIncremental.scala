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
package idb.iql.compiler.lms

import org.junit.Test
import org.junit.Assert._
import scala.virtualization.lms.common._
import sae.SetExtent

/**
 *
 * @author Ralf Mitschke
 *
 */

class TestIRGenBaseAsIncremental
    extends RelationalAlgebraGenBaseAsIncremental
{

    val IR = new RelationalAlgebraIRBase with ScalaOpsPkgExp with RelationalAlgebraGenSAEBinding

    @Test
    def testConstructBaseRelation() {
        val base = new SetExtent[Int]
        val query = IR.baseRelation (base)
        val result = compile (query)
        assertEquals (base, result)
    }

    //  trait Prog extends Base with RelationalAlgebraIRBasicOperators with EffectExp {
    //    def f[Dom](v: Rep[Relation[Dom]]): Rep[Relation[Dom]] = v
    //
    //    def compile[Dom](e: Rep[Relation[Dom]])( rel: Rep[BagExtent[Dom]]): Rep[sae.Relation[Dom]] =
    //      e match {
    //      case Def(BaseRelation()) => rel
    //      case Def(Selection(selectE, f)) => new sae.operators.impl.SelectionView(compile(selectE)( rel), f)
    //    }
    //  }
    //
    //
    //  @Test
    //  def testBase ()
    //  {
    //    //val rel = new BagExtent[Int]
    //
    //    val prog = new Prog {}
    //    val exp = prog.select(prog.baseRelation(), (x:prog.Rep[_]) => true)
    //    val codegen = new ScalaGenEffect with RelationalAlgebraScalaGen { val IR: prog.type = prog }
    //    codegen.emitSource(prog.compile(exp), "F", new java.io.PrintWriter(System.out))
    //
    //  }


}
