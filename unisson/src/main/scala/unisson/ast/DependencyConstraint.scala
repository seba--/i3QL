package unisson.ast

import unisson.model.kinds.DependencyKind

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 11:11
 *
 */

trait DependencyConstraint
    extends UnissonDefinition
{

    def sources : Seq[Ensemble]

    def targets : Seq[Ensemble]

    // todo this is not very nice and just a bad hack remove in future
    var origins : Seq[DependencyConstraintEdge] = Nil
}