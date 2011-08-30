package unisson.ast

import java.lang.IllegalStateException

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 11:11
 *
 */

trait DependencyConstraint
    extends UnissonDefinition
{
    val architecture: String

    val sourceName: String

    val sourceParams: List[String]

    val targetName: String

    val targetParams: List[String]

    val kinds: List[String]

    var source : Option[Ensemble] = None

    var target : Option[Ensemble] = None
}