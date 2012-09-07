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
package sae.syntax.sql.ast

import sae.{SetRelation, LazyView}
import sae.operators._
import scala.Some

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 02.09.12
 * Time: 20:57
 */

object Compiler
{
    def apply[Domain <: AnyRef, Range <: AnyRef](whereClause: WhereClause1[Domain, Range]): LazyView[Range] =
    {
        val fromClause = whereClause.fromClause
        val selection = compileSelections (whereClause.conditions, fromClause.relation)
        val projection = fromClause.selectClause.projection match {
            case Some (f) => new BagProjection (f, selection)
            case None => selection.asInstanceOf[LazyView[Range]] // this is made certain by the ast construction
        }
        if (fromClause.selectClause.distinct) {
            new SetDuplicateElimination (projection)
        }
        else
        {
            projection
        }
    }

    /**
     * For now lets just assume that joins and exist conditions are AND concatenated.
     * For correctness we need to normalize the conditions using De Morgan and discern them.
     */
    def apply[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](whereClause: WhereClause2[DomainA, DomainB, Range]): LazyView[Range] =
    {
        val fromClause = whereClause.fromClause
        val selectionA = compileSelections (whereClause.conditionsA, fromClause.relationA)
        val selectionB = compileSelections (whereClause.conditionsB, fromClause.relationB)

        compileJoins (whereClause.joinConditions.filter (_.isInstanceOf[JoinCondition[DomainA, DomainB, AnyRef, AnyRef]]).asInstanceOf[Seq[JoinCondition[DomainA, DomainB, AnyRef, AnyRef]]],
            whereClause.fromClause.selectClause.projection,
            selectionA,
            selectionB)
    }

    private def compileJoins[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](joinConditions: Seq[JoinCondition[DomainA, DomainB, AnyRef, AnyRef]],
                                                                                    projection: Option[(DomainA, DomainB) => Range],
                                                                                    relationA: LazyView[DomainA],
                                                                                    relationB: LazyView[DomainB]
                                                                                       ): LazyView[Range] =
    {
        if (joinConditions.isEmpty)
        {
            return compileCrossProduct (projection, relationA, relationB)
        }
        val leftKey = compileHashKey (joinConditions.map (_.left))
        val rightKey = compileHashKey (joinConditions.map (_.right))
        new HashEquiJoin (
            Conversions.lazyViewToIndexedView (relationA),
            Conversions.lazyViewToIndexedView (relationB),
            leftKey,
            rightKey,
            projection.getOrElse ((a: DomainA, b: DomainB) => (a, b)).asInstanceOf[(DomainA, DomainB) => Range]
        )
    }


    private def compileHashKey[Domain <: AnyRef](keyExtractors: Seq[Domain => AnyRef]): Domain => AnyRef = {
        keyExtractors.size match {
            case 1 => keyExtractors (0)
            case 2 => (x: Domain) => (keyExtractors (0)(x), keyExtractors (1)(x))
            case 3 => (x: Domain) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x))
            case 4 => (x: Domain) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x))
            case 5 => (x: Domain) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x))
            case 6 => (x: Domain) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x))
            case 7 => (x: Domain) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x), keyExtractors (6)(x))
            case 8 => (x: Domain) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x), keyExtractors (6)(x), keyExtractors (7)(x))
            case 9 => (x: Domain) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x), keyExtractors (6)(x), keyExtractors (7)(x), keyExtractors (8)(x))
            case 10 => (x: Domain) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x), keyExtractors (6)(x), keyExtractors (7)(x), keyExtractors (8)(x), keyExtractors (9)(x))
            case _ => throw new IllegalArgumentException ("Too many join conditions for SAE")
        }
    }


    /**
     * Normalize the conditions into disjunctive normal form with operator precedence (AND > OR)
     *
     */
    private def disjunctiveNormalForm(conditions: Seq[Predicate]): Seq[Seq[Predicate]] = {
        separateByOperators(eliminateSubExpressions(conditions))
    }


    private def conjunctiveNormalForm(conditions: Seq[Predicate]): Seq[Seq[Predicate]] = {
        null
    }


    private def eliminateSubExpressions(conditions: Seq[Predicate]): Seq[Predicate] = {
        val eliminatedNegations = conditions.map (_ match {
            case NegatedSubExpression1 (SubExpressionCondition1 (subConditions)) => deMorgan (conjunctiveNormalForm (subConditions), (c: Predicate) => NegatedSubExpression1 (c))
            case NegatedSubExpression2 (SubExpressionCondition2 (subConditions)) => deMorgan (conjunctiveNormalForm (subConditions), (c: Predicate) => NegatedSubExpression2 (c))
            case x => Seq (x)
        }).flatten

        val afterSubExpressionElimination: Iterator[Seq[Predicate]] = for (window <- eliminatedNegations.sliding (3)) yield
        {
            window match {
                case Seq (p, AndOperator, SubExpressionCondition1 (subConditions)) => distributeSubExpression (p, disjunctiveNormalForm (subConditions))
                case Seq (p, AndOperator, SubExpressionCondition2 (subConditions)) => distributeSubExpression (p, disjunctiveNormalForm (subConditions))
                case any => Seq (any.head)
            }
        }
        afterSubExpressionElimination.flatten.toSeq
    }

    /**
     * A and (B or C) == A and B or A and C
     */
    private def distributeSubExpression(conjunct: Predicate, dnf: Seq[Seq[Predicate]]): Seq[Predicate] =
    {
        val flatConjunctions = for (subConjunctions <- dnf)
        yield
        {
            Seq (conjunct, AndOperator) ++ subConjunctions
        }
        flatConjunctions.reduce ((left: Seq[Predicate], right: Seq[Predicate]) => left ++ Seq (OrOperator) ++ right)
    }

    /**
     * !(A and B) == !A or !B
     *
     * we apply this to a cnf representation: !((A or B or C) and (D or E or F))
     * hence we obtain a dnf representaion: !(A or B or C) or !(D or E or F ) == !A and !B and !C or !D and !E and !F
     */
    private def deMorgan(cnf: Seq[Seq[Predicate]], createNegation: Predicate => Predicate): Seq[Predicate] =
    {
        val disjunctions =
            for (listOfDisjuncts <- cnf)
            yield
            {
                deMorganDisjunctions (listOfDisjuncts, createNegation)
            }
        disjunctions.reduce ((left: Seq[Predicate], right: Seq[Predicate]) => left ++ Seq (OrOperator) ++ right)
    }

    /**
     * !(A or B) == !A and !B
     */
    private def deMorganDisjunctions(disjunctions: Seq[Predicate], createNegation: Predicate => Predicate): Seq[Predicate] =
    {
        val predicates  = disjunctions.map( (p:Predicate) =>Seq(createNegation(p)))
        predicates.reduce ((left: Seq[Predicate], right: Seq[Predicate]) => left ++ Seq (AndOperator) ++ right)
    }


    /**
     * Compile the condition with operator precedence (AND > OR)
     *
     */
    private def compileSelections[Domain <: AnyRef](conditions: Seq[Predicate], relation: LazyView[Domain]): LazyView[Domain] =
    {
        if (conditions.isEmpty) {
            return relation
        }
        val orConditions = separateByOperators (conditions)

        val orFilters = for (orExpr <- orConditions) yield {
            val andFilters = orExpr.filter (_.isInstanceOf[Filter[Domain]]).map (_.asInstanceOf[Filter[Domain]].filter)
            andFilters.reduce ((left: Domain => Boolean, right: Domain => Boolean) => (x: Domain) => left (x) && right (x))
        }
        val selection = orFilters.reduce ((left: Domain => Boolean, right: Domain => Boolean) => (x: Domain) => left (x) || right (x))
        new LazySelection (selection, relation)
    }

    /**
     * Separates the flat list of operators into a sequence of OR operations that each contain a sequence of AND operations.
     * Parenthesis are already handled by the syntax
     */
    private def separateByOperators(rest: Seq[Predicate]): Seq[Seq[Predicate]] =
    {
        val andConditions = rest.takeWhile ({
            case AndOperator => true
            case OrOperator => false
            case _ => true
        })
        val restConditions = rest.drop (andConditions.size)
        val andConditionsWithoutOperator = andConditions.filter ({
            case AndOperator => false
            case _ => true
        })
        if (restConditions.isEmpty)
        {
            Seq (andConditionsWithoutOperator)
        }
        else
        {
            Seq (andConditionsWithoutOperator) ++ separateByOperators (restConditions.drop (1))
        }
    }


    def apply[Domain <: AnyRef, Range <: AnyRef](fromClause: FromClause1[Domain, Range]): LazyView[Range] =
    {
        val projection = fromClause.selectClause.projection match {
            case Some (f) => new BagProjection (f, fromClause.relation)
            case None => fromClause.relation.asInstanceOf[LazyView[Range]] // this is made certain by the ast construction
        }
        compileDistinct (projection, fromClause.selectClause.distinct)
    }

    def apply[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](fromClause: FromClause2[DomainA, DomainB, Range]): LazyView[Range] =
    {
        val crossProduct = compileCrossProduct (fromClause.selectClause.projection, fromClause.relationA, fromClause.relationB)
        compileDistinct (crossProduct, fromClause.selectClause.distinct)
    }


    private def compileDistinct[Domain <: AnyRef](relation: LazyView[Domain], distinct: Boolean): LazyView[Domain] =
    {
        if (!distinct || relation.isInstanceOf[SetRelation[Domain]])
        {
            return relation
        }
        new SetDuplicateElimination (relation)
    }

    private def compileCrossProduct[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: Option[(DomainA, DomainB) => Range],
                                                                                           relationA: LazyView[DomainA],
                                                                                           relationB: LazyView[DomainB]) =
    {
        val crossProduct = new CrossProduct (
            Conversions.lazyViewToMaterializedView (relationA),
            Conversions.lazyViewToMaterializedView (relationB)
        )

        projection match {
            case Some (f) => new BagProjection (
                (tuple: (DomainA, DomainB)) => f (tuple._1, tuple._2),
                crossProduct
            )
            case None => crossProduct.asInstanceOf[LazyView[Range]] // this is made certain by the ast construction
        }
    }
}