package unisson

import ast.{DependencyConstraint, Ensemble}

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.08.11 12:53
 *
 */

case class Violation(source: Option[Ensemble], sourceElement: SourceElement[AnyRef], target: Option[Ensemble],  targetElement: SourceElement[AnyRef], dependency: DependencyConstraint, kind: String)
{

}