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

import sae.syntax.sql.ast.{OrOperator, AndOperator, WhereClauseExpression}
import sae.syntax.sql.ast.predicates.{WhereClauseSequence, Negation, Predicate}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 07.09.12
 * Time: 12:38
 */

object NormalizePredicates
{

    def apply(conditions: Seq[WhereClauseExpression]): Seq[Seq[Predicate]] = {
        disjunctiveNormalForm (conditions)
    }

    /**
     * Normalize the conditions into disjunctive normal form with operator precedence (AND > OR)
     *
     */
    private def disjunctiveNormalForm(conditions: Seq[WhereClauseExpression]): Seq[Seq[Predicate]] = {
        separateCNFOperators (eliminateSubExpressions (conditions))
    }

    private def conjunctiveNormalForm(conditions: Seq[WhereClauseExpression]): Seq[Seq[Predicate]] = {
        null
    }


    private def eliminateSubExpressions(conditions: Seq[WhereClauseExpression]): Seq[WhereClauseExpression] = {
        val eliminatedNegations = conditions.map (_ match {
            case Negation (WhereClauseSequence (subConditions)) => deMorgan (conjunctiveNormalForm (subConditions))
            case x => Seq (x)
        }).flatten

        val afterSubExpressionElimination =
            for (window <- eliminatedNegations.sliding (3)) yield
            {
                window match {
                    case Seq (WhereClauseSequence (subConditions), AndOperator, p: Predicate) =>
                        distributeSubExpression (p, disjunctiveNormalForm (subConditions), append = true)

                    case Seq (p: Predicate, AndOperator, WhereClauseSequence (subConditions)) =>
                        distributeSubExpression (p, disjunctiveNormalForm (subConditions), append = false)

                    case Seq (WhereClauseSequence (subConditions), OrOperator, p: Predicate) =>
                        eliminateSubExpressions (subConditions) ++ Seq (OrOperator, p)

                    case Seq (p1: Predicate, op, p2: Predicate) =>
                        Seq (p1, op)

                    case _ =>
                        Seq ()
                }
            }
        if (eliminatedNegations.last.isInstanceOf[WhereClauseSequence] ||
            eliminatedNegations (eliminatedNegations.size - 3).isInstanceOf[WhereClauseSequence])
        {
            if (eliminatedNegations.last.isInstanceOf[WhereClauseSequence] && eliminatedNegations (eliminatedNegations.size - 2).isInstanceOf[OrOperator.type])
            {
                (afterSubExpressionElimination.toList.take (afterSubExpressionElimination.size - 1) ++
                    Seq(eliminateSubExpressions (eliminatedNegations.last.asInstanceOf[WhereClauseSequence].expressions))
                    ).flatten
            }
            else
            {
                afterSubExpressionElimination.flatten.toList
            }
        }
        else
        {
            afterSubExpressionElimination.flatten.toList ++ Seq (eliminatedNegations.last)
        }
    }

    /**
     * A and (B or C) == A and B or A and C
     */
    private def distributeSubExpression(conjunct: Predicate, dnf: Seq[Seq[Predicate]], append: Boolean): Seq[WhereClauseExpression] =
    {
        val flatConjunctions = for (subConjunctions <- dnf)
        yield
        {
            if (append)
            {
                subConjunctions ++ Seq (AndOperator, conjunct)
            }
            else
            {
                Seq (conjunct, AndOperator) ++ subConjunctions
            }
        }
        flatConjunctions.reduce ((left: Seq[WhereClauseExpression], right: Seq[WhereClauseExpression]) => left ++ Seq (OrOperator) ++ right)
    }

    /**
     * !(A and B) == !A or !B
     *
     * we apply this to a cnf representation: !((A or B or C) and (D or E or F))
     * hence we obtain a dnf representaion: !(A or B or C) or !(D or E or F ) == !A and !B and !C or !D and !E and !F
     */
    private def deMorgan(cnf: Seq[Seq[Predicate]]): Seq[WhereClauseExpression] =
    {
        val disjunctions =
            for (listOfDisjuncts <- cnf)
            yield
            {
                deMorganDisjunctions (listOfDisjuncts)
            }
        disjunctions.reduce ((left: Seq[WhereClauseExpression], right: Seq[WhereClauseExpression]) => left ++ Seq (OrOperator) ++ right)
    }

    /**
     * !(A or B) == !A and !B
     */
    private def deMorganDisjunctions(disjunctions: Seq[Predicate]): Seq[WhereClauseExpression] =
    {
        val predicates = disjunctions.map ((p: Predicate) => Seq (Negation (p)))
        predicates.reduce ((left: Seq[WhereClauseExpression], right: Seq[WhereClauseExpression]) => left ++ Seq (AndOperator) ++ right)
    }

    /**
     * Separates the flat list of operators into a sequence of OR operations that each contain a sequence of AND operations.
     * Parenthesis are already handled by the syntax
     */
    private def separateCNFOperators(rest: Seq[WhereClauseExpression]): Seq[Seq[Predicate]] =
    {
        val andConditions = rest.takeWhile ({
            case AndOperator => true
            case OrOperator => false
            case _ => true
        })
        val restConditions = rest.drop (andConditions.size)
        val andConditionsWithoutOperator = andConditions.collect ({
            case p: Predicate => p
        })
        if (restConditions.isEmpty)
        {
            Seq (andConditionsWithoutOperator)
        }
        else
        {
            Seq (andConditionsWithoutOperator) ++ separateCNFOperators (restConditions.drop (1))
        }
    }

}
