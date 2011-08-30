package unisson.ast

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 17:41
 *
 */

case class WithoutQuery(left: UnissonQuery, right: UnissonQuery)
        extends UnissonQuery
{

}