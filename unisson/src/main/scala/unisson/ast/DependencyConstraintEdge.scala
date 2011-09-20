package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 11:11
 *
 */

trait DependencyConstraintEdge
        extends UnissonDefinition
{
    val architecture: String

    val sourceName: String

    val sourceParams: List[String]

    val targetName: String

    val targetParams: List[String]

    val kinds: List[String]
}