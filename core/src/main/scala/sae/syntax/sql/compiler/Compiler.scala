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
package sae.syntax.sql.compiler


import sae.{SetRelation, LazyView}
import sae.operators._
import sae.syntax.RelationalAlgebraSyntax._
import sae.syntax.sql.ast._
import predicates._
import sae.syntax.sql.SQL_QUERY

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 02.09.12
 * Time: 20:57
 */

object Compiler
{

    def apply[Range <: AnyRef](query: SQL_QUERY[Range]): LazyView[Range] = {
        // There is some ugliness here because we deliberately forget some types in the AST
        query.representation match {
            case SQLQuery (SelectClause1 (projection, distinct), from@FromClause1 (relation), None) =>
                compileNoWhere1 (
                    projection.asInstanceOf[Option[from.Domain => Range]],
                    distinct,
                    relation
                )
            case SQLQuery (SelectClause1 (projection, distinct), from@FromClause1 (relation), Some (where)) =>
                compile1 (
                    projection.asInstanceOf[Option[from.Domain => Range]],
                    distinct,
                    relation,
                    where.expressions
                )
            case SQLQuery (SelectClause2 (projection, distinct), from@FromClause2 (relationA, relationB), None) =>
                compileNoWhere2 (
                    projection.asInstanceOf[Option[(from.DomainA, from.DomainB) => Range]],
                    distinct,
                    relationA,
                    relationB
                )
            case SQLQuery (SelectClause2 (projection, distinct), from@FromClause2 (relationA, relationB), Some (where)) =>
                compile2 (
                    projection.asInstanceOf[Option[(from.DomainA, from.DomainB) => Range]],
                    distinct,
                    relationA,
                    relationB,
                    where.expressions
                )
            case SQLQuery (AggregateSelectClauseSelfMaintainable1 (projection, functionFactory, distinct), from@FromClause1 (relation), None) =>
                compileAggregationSelfMaintainable (
                    compileNoWhere1 (
                        projection.asInstanceOf[Option[from.Domain => Range]],
                        distinct,
                        relation
                    ).asInstanceOf[LazyView[AnyRef]],
                    functionFactory
                ).asInstanceOf[LazyView[Range]] // the syntax makes sure this is correct
            case SQLQuery (AggregateSelectClauseSelfMaintainable1 (projection, functionFactory, distinct), from@FromClause1 (relation), Some (where)) =>
                compileAggregationSelfMaintainable (
                    compile1 (
                        projection.asInstanceOf[Option[from.Domain => Range]],
                        distinct,
                        relation,
                        where.expressions
                    ).asInstanceOf[LazyView[AnyRef]],
                    functionFactory
                ).asInstanceOf[LazyView[Range]] // the syntax makes sure this is correct
            case SQLQuery (AggregateSelectClauseSelfMaintainable2 (projection, functionFactory, distinct), from@FromClause2 (relationA, relationB), None) =>
                compileAggregationSelfMaintainable (
                    compileNoWhere2 (
                        projection.asInstanceOf[Option[(from.DomainA, from.DomainB) => Range]],
                        distinct,
                        relationA,
                        relationB
                    ).asInstanceOf[LazyView[AnyRef]],
                    functionFactory
                ).asInstanceOf[LazyView[Range]] // the syntax makes sure this is correct
            case SQLQuery (AggregateSelectClauseSelfMaintainable2 (projection, functionFactory, distinct), from@FromClause2 (relationA, relationB), Some (where)) =>
                compileAggregationSelfMaintainable (
                    compile2 (
                        projection.asInstanceOf[Option[(from.DomainA, from.DomainB) => Range]],
                        distinct,
                        relationA,
                        relationB,
                        where.expressions
                    ).asInstanceOf[LazyView[AnyRef]],
                    functionFactory
                ).asInstanceOf[LazyView[Range]] // the syntax makes sure this is correct
        }
    }


    private def compileAggregationSelfMaintainable[Domain <: AnyRef, AggregateValue](relation: LazyView[Domain],
                                                                                     functionFactory: SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]): LazyView[Some[AggregateValue]] =
    {
        γ (relation, functionFactory)
    }

    private def compileNoWhere1[Domain <: AnyRef, Range <: AnyRef](projection: Option[Domain => Range],
                                                                   distinct: Boolean,
                                                                   relation: LazyView[Domain]): LazyView[Range] =
    {
        compileDistinct (
            compileProjection (
                projection,
                relation
            ),
            distinct
        )
    }


    private def compileNoWhere2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: Option[(DomainA, DomainB) => Range],
                                                                                       distinct: Boolean,
                                                                                       relationA: LazyView[DomainA],
                                                                                       relationB: LazyView[DomainB]): LazyView[Range] =
    {
        compileDistinct (
            compileCrossProduct (
                projection,
                relationA,
                relationB
            ),
            distinct
        )
    }

    private def compile1[Domain <: AnyRef, Range <: AnyRef](projection: Option[Domain => Range],
                                                            distinct: Boolean,
                                                            relation: LazyView[Domain],
                                                            expressions: Seq[WhereClauseExpression]): LazyView[Range] =
    {
        val cnf = NormalizePredicates (expressions)
        val compiledQueries =
            partitionForFilters (cnf) match {
                case (Nil, seq) => seq.map (compileSubQueries1 (_, relation)).flatten
                case (seq, Nil) => Seq (compileSelection (combineFilters (seq), relation))
                case (seqFilters, seqSubQueries) => compileSelection (combineFilters (seqFilters), relation) +: seqSubQueries.map (compileSubQueries1 (_, relation)).flatten
                case _ => throw new IllegalArgumentException ("Compile method for where clause called with empty where clause.")
            }

        val union =
            if (compiledQueries.size == 1) {
                compiledQueries (0)
            }
            else
            {
                compiledQueries.reduce (compileUnion (_, _))
            }

        compileDistinct (
            compileProjection (
                projection,
                union
            ),
            distinct
        )
    }


    private def compile2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: Option[(DomainA, DomainB) => Range],
                                                                                distinct: Boolean,
                                                                                relationA: LazyView[DomainA],
                                                                                relationB: LazyView[DomainB],
                                                                                expressions: Seq[WhereClauseExpression]): LazyView[Range] =
    {
        val cnf = NormalizePredicates (expressions)

        val (filters, others) = partitionForFilters (cnf)

        val selectionsOnly =
            if (!filters.isEmpty) {
                val relationFilters = filtersByRelation (filters)
                val filtersA = relationFilters.getOrElse (Seq (1), Nil)
                val filtersB = relationFilters.getOrElse (Seq (2), Nil)
                Some (
                    compileCrossProduct (
                        projection,
                        compileSelection (combineFilters (filtersA), relationA),
                        compileSelection (combineFilters (filtersB), relationB)
                    )
                )
            }
            else None

        val (joins, subQueries) = partitionForJoins (others)

        val joinsOnly =
            joins.map (
                compileJoins (
                    _,
                    projection,
                    relationA,
                    relationB
                )
            )

        (selectionsOnly, joinsOnly) match {
            case (None, seq) if seq.size > 1 => seq.reduce (compileUnion (_, _))
            case (None, seq) if seq.size == 1 => seq.head
            case (Some (q), Nil) => q
            case (Some (q), seq) => seq.foldLeft (q)(compileUnion (_, _))
            case _ => null
        }
    }

    private def combineFilters[Domain <: AnyRef](filters: Seq[Seq[Predicate]]): Option[Domain => Boolean] = {
        if (filters.isEmpty || filters.size == 1 && filters.head == Nil)
            return None
        Some (
            (for (conjunction <- filters) yield {
                (for (filter <- conjunction) yield {
                    val fun: Domain => Boolean = filter match {
                        case Filter (f: (Domain => Boolean), _) => f
                        case Negation (Filter (f: (Domain => Boolean), _)) => !f (_)
                    }
                    fun
                }).reduce ((left: Domain => Boolean, right: Domain => Boolean) => ((x: Domain) => (left (x) && right (x))))
            }).reduce ((left: Domain => Boolean, right: Domain => Boolean) => ((x: Domain) => (left (x) || right (x))))
        )
    }

    /**
     * discerns the different filters for different relations
     */
    def filtersByRelation(cnf: Seq[Seq[Predicate]]): Map[Seq[Int], Seq[Seq[Predicate]]] =
    {
        cnf.groupBy (
            _.map (
            {
                case Filter (_, num) => num
                case Negation (Filter (_, num)) => num
            }
            ).sorted.distinct
        )
    }

    /**
     * partition the predicates (given in CNF form) to (filtersOnly, Others)
     */
    private def partitionForFilters(cnf: Seq[Seq[Predicate]]): (Seq[Seq[Predicate]], Seq[Seq[Predicate]]) = {
        val (others, filtersOnly) = cnf.partition (_.exists {
            case Filter (_, _) => false
            case Negation (Filter (_, _)) => false
            case _ => true
        })
        (filtersOnly, others)
    }


    /**
     * partition the predicates (given in CNF form) to (JoinsAndFilters, Others)
     */
    private def partitionForJoins(cnf: Seq[Seq[Predicate]]): (Seq[Seq[Predicate]], Seq[Seq[Predicate]]) = {
        val (others, joinsAndFiltersOnly) = cnf.partition (_.exists {
            case Join (_, _) => false
            case Negation (Join (_, _)) => false
            case Filter (_, _) => false
            case Negation (Filter (_, _)) => false
            case _ => true
        })
        (joinsAndFiltersOnly, others)
    }

    private def compileSelection[Domain <: AnyRef](selection: Option[Domain => Boolean],
                                                   relation: LazyView[Domain]): LazyView[Domain] =
    {
        selection match {
            case Some (fun) => new LazySelection[Domain](fun, relation)
            case None => relation
        }

    }


    private def compileProjection[Domain <: AnyRef, Range <: AnyRef](projection: Option[(Domain) => Range],
                                                                     relation: LazyView[Domain]): LazyView[Range] =
    {
        projection match {
            case Some (f) => new BagProjection (f, relation)
            case None => relation.asInstanceOf[LazyView[Range]] // this is made certain by the ast construction
        }
    }

    private def compileDistinct[Domain <: AnyRef](relation: LazyView[Domain], distinct: Boolean): LazyView[Domain] =
    {
        if (!distinct || relation.isInstanceOf[SetRelation[Domain]])
        {
            return relation
        }
        δ (relation)
    }

    private def compileCrossProduct[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: Option[(DomainA, DomainB) => Range],
                                                                                           relationA: LazyView[DomainA],
                                                                                           relationB: LazyView[DomainB]) =
    {
        val crossProduct =
            Conversions.lazyViewToMaterializedView (relationA) × Conversions.lazyViewToMaterializedView (relationB)

        projection match {
            case Some (f) =>
                Π (
                    (tuple: (DomainA, DomainB)) => f (tuple._1, tuple._2)
                )(
                    crossProduct
                )
            case None => crossProduct.asInstanceOf[LazyView[Range]] // this is made certain by the ast construction
        }
    }

    private def compileJoins[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](predicates: Seq[Predicate],
                                                                                    projection: Option[(DomainA, DomainB) => Range],
                                                                                    relationA: LazyView[DomainA],
                                                                                    relationB: LazyView[DomainB]
                                                                                       ): LazyView[Range] =
    {

        val joins = predicates.filter (_.isInstanceOf[Join[DomainA, DomainB, _, _]]).asInstanceOf[Seq[Join[AnyRef, AnyRef, _, _]]]
        if (joins.isEmpty)
        {
            // could happen if we have negative joins
            return compileCrossProduct (projection, relationA, relationB)
        }
        val filtersA = predicates.filter ({
            case Filter (_, 1) => true
            case Negation (Filter (_, 1)) => true
            case _ => false
        })
        val filtersB = predicates.filter ({
            case Filter (_, 2) => true
            case Negation (Filter (_, 2)) => true
            case _ => false
        })
        val leftKey = compileHashKey (joins.map (_.left))
        val rightKey = compileHashKey (joins.map (_.right))

        (
            (
                Conversions.lazyViewToIndexedView (compileSelection (combineFilters (Seq (filtersA)), relationA)),
                leftKey
                ) ⋈ (
                rightKey,
                Conversions.lazyViewToIndexedView (compileSelection (combineFilters (Seq (filtersB)), relationB))
                )
            ) (projection.getOrElse ((a: DomainA, b: DomainB) => (a, b)).asInstanceOf[(DomainA, DomainB) => Range])
    }

    private def compileSubQueries1[Domain <: AnyRef](predicates: Seq[Predicate],
                                                     relation: LazyView[Domain]
                                                        ): Seq[LazyView[Domain]] =
    {
        val filters = predicates.filter ({
            case Filter (_, 1) => true
            case Negation (Filter (_, 1)) => true
            case _ => false
        })

        val existsSubQueries = predicates.filter (_.isInstanceOf[Exists[_ <: AnyRef]]).asInstanceOf[Seq[Exists[_ <: AnyRef]]]

        val notExistsSubQueries = predicates.collect {
            case Negation (e: Exists[_]) => e
        }

        val (existsWithJoin, existsWithoutJoin) = existsSubQueries.partition ({
            case Exists (_, num) => num > 0
        })

        val (notExistsWithJoin, notExistsWithoutJoin) = notExistsSubQueries.partition ({
            case Exists (_, num) => num > 0
        })

        val compiledExists =
            for (subQuery <- existsWithJoin.map (_.subQuery);
                 (concreteSubQuery, unboundJoins) <- concreteQueriesAndUnboundJoin1 (subQuery))
            yield
            {
                val subRelation = Compiler (concreteSubQuery)
                val outerKey = compileHashKey (unboundJoins.map (_.right))
                val innerKey = compileHashKey (unboundJoins.map (_.left))
                (
                    Conversions.lazyViewToIndexedView (compileSelection (combineFilters (Seq (filters)), relation)),
                    outerKey
                    ) ⋉ (
                    innerKey,
                    Conversions.lazyViewToIndexedView (subRelation)
                    )
            }

        val compiledNotExists =
            for (subQuery <- notExistsWithJoin.map (_.subQuery);
                 (concreteSubQuery, unboundJoins) <- concreteQueriesAndUnboundJoin1 (subQuery))
            yield
            {
                val subRelation = Compiler (concreteSubQuery)
                val outerKey = compileHashKey (unboundJoins.map (_.right))
                val innreKey = compileHashKey (unboundJoins.map (_.left))
                (
                    Conversions.lazyViewToIndexedView (compileSelection (combineFilters (Seq (filters)), relation)),
                    outerKey
                    ) ⊳ (
                    innreKey,
                    Conversions.lazyViewToIndexedView (subRelation)
                    )
            }

        (compiledExists ++ compiledNotExists)
    }


    private def concreteQueriesAndUnboundJoin1[Range <: AnyRef](query: SQL_QUERY[Range]): Seq[(SQL_QUERY[Range], Seq[Join[AnyRef, AnyRef, _, _]])] = {
        query match {
            case Union (left, right) => concreteQueriesAndUnboundJoin1 (left).asInstanceOf[Seq[(SQL_QUERY[Range], Seq[Join[AnyRef, AnyRef, _, _]])]] ++ concreteQueriesAndUnboundJoin1 (right).asInstanceOf[Seq[(SQL_QUERY[Range], Seq[Join[AnyRef, AnyRef, _, _]])]]
            case SQLQuery (selectClause, fromClause, whereClause) =>
            {
                if (!whereClause.isDefined) {
                    Seq ((query, Nil))
                }
                else
                {


                    val cnf = NormalizePredicates (whereClause.get.expressions)
                    // if there are multiple queries with a join we need to make a union
                    for (conjunct <- cnf) yield {
                        val (unboundJoins, predicates) = conjunct.partition {
                            case UnboundJoin (_) => true
                            //case Negation (UnboundJoin (_)) => true// TODO what about negated unbounds?
                            case _ => false
                        }
                        val newSubQuery =
                            SQLQuery[Range](
                                selectClause,
                                fromClause,
                                if (predicates.isEmpty) None
                                else Some (WhereClauseSequence (predicates.flatMap (Seq (AndOperator, _)).drop (1)))
                            )
                        (newSubQuery, unboundJoins.map {
                            case UnboundJoin (join) => join.asInstanceOf[Join[AnyRef, AnyRef, _, _]]
                        })
                    }
                }
            }

        }

    }

    private def compileHashKey(keyExtractors: Seq[AnyRef => Any]): AnyRef => AnyRef = {
        keyExtractors.size match {
            case 1 => keyExtractors (0).asInstanceOf[AnyRef => AnyRef]
            case 2 => (x: AnyRef) => (keyExtractors (0)(x), keyExtractors (1)(x))
            case 3 => (x: AnyRef) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x))
            case 4 => (x: AnyRef) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x))
            case 5 => (x: AnyRef) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x))
            case 6 => (x: AnyRef) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x))
            case 7 => (x: AnyRef) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x), keyExtractors (6)(x))
            case 8 => (x: AnyRef) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x), keyExtractors (6)(x), keyExtractors (7)(x))
            case 9 => (x: AnyRef) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x), keyExtractors (6)(x), keyExtractors (7)(x), keyExtractors (8)(x))
            case 10 => (x: AnyRef) => (keyExtractors (0)(x), keyExtractors (1)(x), keyExtractors (2)(x), keyExtractors (3)(x), keyExtractors (4)(x), keyExtractors (5)(x), keyExtractors (6)(x), keyExtractors (7)(x), keyExtractors (8)(x), keyExtractors (9)(x))
            case _ => throw new IllegalArgumentException ("Too many join conditions for SAE")
        }
    }

    private def compileUnion[DomainA <: AnyRef, DomainB >: DomainA <: AnyRef, Range <: AnyRef](relationA: LazyView[DomainA],
                                                                                               relationB: LazyView[DomainB]) =
    {
        null
    }
}