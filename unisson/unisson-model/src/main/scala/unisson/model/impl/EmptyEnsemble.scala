package unisson.model.impl

import de.tud.cs.st.vespucci.interfaces.{IConstraint, IEnsemble}


/**
 *
 * Author: Ralf Mitschke
 * Date: 24.02.12
 * Time: 11:03
 *
 */
object EmptyEnsemble  extends IEnsemble
{
    def getParent = null

    def getDescription = ""

    def getName = "@EmptyEnsemble"

    def getQuery = "empty"

    def getSourceConnections = new java.util.HashSet[IConstraint](0)

    def getTargetConnections = new java.util.HashSet[IConstraint](0)

    def getInnerEnsembles = new java.util.HashSet[IEnsemble](0)
}