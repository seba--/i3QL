package unisson.model.mock.vespucci

import collection.JavaConversions
import de.tud.cs.st.vespucci.model.{IConstraint, IEnsemble}

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class Ensemble(name: String, query : String, innerEnsembles : Set[IEnsemble])
    extends IEnsemble
{
    var sourceConnections : Set[IConstraint]= Set.empty

    var targetConnections : Set[IConstraint] = Set.empty

    def getDescription = ""

    def getName = name

    def getQuery = query

    def getSourceConnections = JavaConversions.setAsJavaSet(sourceConnections)

    def getTargetConnections = JavaConversions.setAsJavaSet(targetConnections)

    def getInnerEnsembles = JavaConversions.setAsJavaSet(innerEnsembles)

    override def toString = "Ensemble(" + name +")"
}