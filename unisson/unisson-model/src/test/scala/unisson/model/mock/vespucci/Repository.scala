package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.interfaces.IEnsemble

/**
 *
 * Author: Ralf Mitschke
 * Date: 04.01.12
 * Time: 11:31
 *
 */
object Repository
{

    def apply(ensembles: IEnsemble*) = Concern(ensembles.toSet[IEnsemble], Set.empty, null)

    def apply(ensembles: Set[_ <: IEnsemble]) = Concern(ensembles.toSet[IEnsemble], Set.empty, null)

}