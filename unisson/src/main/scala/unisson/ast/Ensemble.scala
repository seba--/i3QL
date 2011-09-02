package unisson.ast

/**
 *
 * Author: Ralf Mitschke
 * Created: 02.09.11 14:32
 *
 */

case class Ensemble(
                       name: String,
                       query: UnissonQuery,
                       contextParameters: List[String],
                       children: Seq[Ensemble]
                   )
        extends UnissonDefinition
{
    children.foreach(
        _.parentRef = Some(this)
    )

    private var parentRef: Option[Ensemble] = None

    def parent: Option[Ensemble] = parentRef

    var outgoingConnections: Seq[DependencyConstraint] = Nil

    var incomingConnections: Seq[DependencyConstraint] = Nil

}