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
package idb.algebra.ir

import idb.algebra.base.RelationalAlgebraMultiRelations
import idb.lms.extensions.operations.TupleOpsExpExt
import idb.query.QueryEnvironment
import scala.virtualization.lms.common.{FunctionsExp, TupleOpsExp}


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRMultiRelations
    extends RelationalAlgebraMultiRelations
    with RelationalAlgebraIRBasicOperators
    with FunctionsExp
    with TupleOpsExp
{


    override def crossProduct[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB, DomainC)]] =
        projection (
            crossProduct (
                crossProduct (relationA, relationB),
                relationC
            ),
            flattenTuple3 (_: Rep[((DomainA, DomainB), DomainC)])
        )

    override def crossProduct[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB, DomainC, DomainD)]] =
        projection (
            crossProduct (
                crossProduct(relationA, relationB, relationC),
                relationD
            ),
            flattenTuple4 (_: Rep[((DomainA, DomainB, DomainC), DomainD)])
        )

    override def crossProduct[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest,
    DomainE: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]],
        relationE: Rep[Query[DomainE]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[(DomainA, DomainB, DomainC, DomainD, DomainE)]] =
        projection (
            crossProduct (
                crossProduct(relationA, relationB, relationC, relationD),
                relationE
            ),
            flattenTuple5 (_: Rep[((DomainA, DomainB, DomainC, DomainD), DomainE)])
        )



    def flattenTuple3[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest] (
        args: Rep[((DomainA, DomainB), DomainC)]
    ): Rep[(DomainA, DomainB, DomainC)] = make_tuple3 (args._1._1, args._1._2, args._2)

    def flattenTuple4[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest] (
        args: Rep[((DomainA, DomainB, DomainC), DomainD)]
    ): Rep[(DomainA, DomainB, DomainC, DomainD)] = make_tuple4 (args._1._1, args._1._2, args._1._3, args._2)

    def flattenTuple5[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, DomainE: Manifest] (
        args: Rep[((DomainA, DomainB, DomainC, DomainD), DomainE)]
    ): Rep[(DomainA, DomainB, DomainC, DomainD, DomainE)] = make_tuple5 (args._1._1, args._1._2, args._1._3, args._1._4,
        args._2)
}
