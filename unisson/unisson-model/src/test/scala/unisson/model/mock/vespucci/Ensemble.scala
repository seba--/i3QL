package unisson.model.mock.vespucci

import collection.JavaConversions
import de.tud.cs.st.vespucci.interfaces.{IConstraint, IEnsemble}
import java.util.HashSet

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class EnsembleImpl(name: String, innerEnsembles : Set[IEnsemble])
    extends IEnsemble
{
    var query : String = ""

    var parent : IEnsemble = null

    def getParent = parent

    def getDescription = ""

    def getName = name

    def getQuery = query

    def getSourceConnections = new HashSet[IConstraint]()

    def getTargetConnections = new HashSet[IConstraint]()

    def getInnerEnsembles = JavaConversions.setAsJavaSet(innerEnsembles)

    override def toString = "Ensemble(" + name +")"
}

object Ensemble
{
    def apply(name: String, query : String, innerEnsembles : EnsembleImpl*) :EnsembleImpl =
    {
        val e = EnsembleImpl(name, innerEnsembles.toSet[IEnsemble])
        e.query = query
        for( child <- innerEnsembles)
        {
            child.parent = e
        }
        e
    }
}