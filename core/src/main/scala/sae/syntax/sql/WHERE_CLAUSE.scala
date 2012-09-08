package sae.syntax.sql


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 *
 * The top level where clause has its own type since we can compile this to a query of type Range
 */
trait WHERE_CLAUSE[Domain <: AnyRef, Range <: AnyRef]
    extends SQL_QUERY[Range]
{

    def AND(predicate: Domain => Boolean): WHERE_CLAUSE[Domain, Range]

    def OR(predicate: Domain => Boolean): WHERE_CLAUSE[Domain, Range]

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0): WHERE_CLAUSE[Domain, Range]

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0): WHERE_CLAUSE[Domain, Range]

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[Domain]): WHERE_CLAUSE[Domain, Range]

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[Domain]): WHERE_CLAUSE[Domain, Range]
}
