package sae.syntax.sql

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 21:11
 *
 */
trait STARTING_FROM_CLAUSE[Domain <: AnyRef]
{

    def SELECT[Range <: AnyRef](projection: Domain => Range) : FROM_CLAUSE[Domain, Range]

    def SELECT(x:STAR) : FROM_CLAUSE[Domain, Domain]


}