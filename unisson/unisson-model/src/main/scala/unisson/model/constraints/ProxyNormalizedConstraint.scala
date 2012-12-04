package unisson.model.constraints

import unisson.model.kinds.DependencyKind
import de.tud.cs.st.vespucci.interfaces.{IEnsemble, IConstraint}

/**
 *
 * Author: Ralf Mitschke
 * Date: 31.12.11
 * Time: 17:16
 *
 */
case class ProxyNormalizedConstraint(origin: IConstraint, kind: DependencyKind, context : String)
        extends NormalizedConstraint
{

    lazy val constraintType = ConstraintType(origin)

    def source: IEnsemble = origin.getSource

    def target: IEnsemble = origin.getTarget

}