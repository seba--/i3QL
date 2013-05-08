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
package idb.syntax

import scala.virtualization.lms.common.ScalaOpsPkgExp
import idb.syntax.iql.impl.{SelectClause1, FromClause1}
import idb.iql.compiler.lms.RelationalAlgebraIROpt


/**
 *
 *
 * Thi package object binds the lms framework to concrete representations for relational algebra with lifted Scala
 * functions.
 * Importing the package automatically brings Rep and Exp into Scope.
 * For some reason the concrete implementations (cf. package impl) require using functions explicitly as Exp[A=>B].
 * Actually, using Rep[A=>B] should be equivalent since the Rep is bound to Exp here (in the iql package object).
 *
 * @author Ralf Mitschke
 */
package object iql
    extends ScalaOpsPkgExp
    with RelationalAlgebraIROpt
{

    //trait Representation extends

    val * : STAR_KEYWORD = impl.StarKeyword

    //protected[iql] def clausesToOperators[Range] (clauses: SQL_QUERY[Range])  =

    type CompiledRelation[Domain] = idb.Relation[Domain]

    def toRep[Range] (clause: SQL_QUERY[Range]): Rep[Relation[Range]] = clause match {
        case FromClause1 (relation, SelectClause1 (project)) => projection (relation, project)
    }

}
