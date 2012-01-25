package unisson.model.constraints

import unisson.model.kinds.DependencyKind
import de.tud.cs.st.vespucci.model.{IEnsemble, IConstraint}


/**
 *
 * Author: Ralf Mitschke
 * Date: 31.12.11
 * Time: 15:34
 *
 */
case class NormalizedConstraintImpl(
                                           kind: DependencyKind,
                                           constraintType: ConstraintType.Value,
                                           source: IEnsemble,
                                           target: IEnsemble,
                                           context: String
                                           ) extends NormalizedConstraint
{
    var origin: IConstraint = null
}