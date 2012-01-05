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
trait NormalizedConstraint
{

    def origin: IConstraint

    def kind: DependencyKind

    def constraintType: ConstraintType.Value

    def source: IEnsemble

    def target: IEnsemble

    def context : String

}