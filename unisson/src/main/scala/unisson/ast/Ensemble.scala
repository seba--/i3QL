package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 11:08
 *
 */

case class Ensemble(name : String, query : UnissonQuery, subEnsembleNames : Seq[String])
    extends UnissonDefinition
{
    var outgoingConstraints : Seq[DependencyConstraint] = Nil

    var incomingConstraints : Seq[DependencyConstraint] = Nil

    var childEnsembles : Seq[Ensemble] = Nil

    var parentEnsemble : Option[Ensemble] = None
}
