package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.IEnsemble

/**
 *
 * Author: Ralf Mitschke
 * Date: 04.01.12
 * Time: 11:31
 *
 */
object GlobalArchitectureModel
{

    def apply(ensembles: IEnsemble*) = ArchitectureModel(ensembles.toSet[IEnsemble], Set.empty, null)

    def apply(ensembles: Set[_ <: IEnsemble]) = ArchitectureModel(ensembles.toSet[IEnsemble], Set.empty, null)

}