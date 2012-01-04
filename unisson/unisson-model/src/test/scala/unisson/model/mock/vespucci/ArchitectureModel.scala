package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.{IConstraint, IEnsemble, IArchitectureModel}
import collection.JavaConversions


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:25
 *
 */
case class ArchitectureModel(ensembles: Set[_ <:IEnsemble], constraints : Set[_ <: IConstraint])
        extends IArchitectureModel
{
    def getEnsembles = JavaConversions.setAsJavaSet(ensembles).asInstanceOf[java.util.Set[IEnsemble]]

    def getConstraints = JavaConversions.setAsJavaSet(constraints).asInstanceOf[java.util.Set[IConstraint]]
}