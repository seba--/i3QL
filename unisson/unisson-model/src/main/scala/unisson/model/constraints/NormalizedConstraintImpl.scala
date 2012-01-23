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
class NormalizedConstraintImpl(
                                      val origin: IConstraint,
                                      val kind: DependencyKind,
                                      val constraintType: ConstraintType.Value,
                                      val source: IEnsemble,
                                      val target: IEnsemble,
                                      val context: String
                                      ) extends NormalizedConstraint
{

}