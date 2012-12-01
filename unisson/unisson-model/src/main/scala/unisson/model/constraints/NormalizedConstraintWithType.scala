package unisson.model.constraints

import unisson.model.kinds.DependencyKind
import de.tud.cs.st.vespucci.interfaces.{IEnsemble, IConstraint}

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.01.12
 * Time: 16:26
 *
 */
case class NormalizedConstraintWithType(origin: IConstraint, constraintType : ConstraintType.Value, kind: DependencyKind, context : String)
        extends NormalizedConstraint
{
    def source: IEnsemble = origin.getSource

    def target: IEnsemble = origin.getTarget

}