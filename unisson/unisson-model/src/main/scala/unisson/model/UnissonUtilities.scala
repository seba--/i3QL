package unisson.model

import de.tud.cs.st.vespucci.interfaces.IEnsemble

/**
 *
 * Author: Ralf Mitschke
 * Date: 25.06.12
 * Time: 11:08
 *
 */
object UnissonUtilities
{

    /**
     * @return transitive closure over the children of <code>ensemble</code>.
     */
    def descendants(ensemble: IEnsemble): scala.collection.mutable.Set[IEnsemble] = {
        import scala.collection.JavaConverters._
        val children: scala.collection.mutable.Set[IEnsemble] = ensemble.getInnerEnsembles.asScala
        children ++ (
            for (child <- children;
                 descendant <- descendants (child)
            ) yield descendant
            )
    }


}