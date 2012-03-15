package unisson.model.mock.soot

import de.tud.cs.st.vespucci.model.IEnsemble
import de.tud.cs.st.vespucci.interfaces.IPair
import collection.immutable.Map
import java.util.List
import soot.bridge.{ISootCodeElement, DataFlowEvaluator}

/**
 *
 * Author: Ralf Mitschke
 * Date: 15.03.12
 * Time: 11:10
 *
 */
class MockDataFlowEvaluator
        extends DataFlowEvaluator
{

    def determineDataFlows(extension: List[IPair[IEnsemble, ISootCodeElement]]) =
    {
        var dataflows : Map[ISootCodeElement, java.util.Set[ISootCodeElement]]= Map.empty
        import scala.collection.JavaConversions._
        for( e1 <- extension)
        {
            var targets = new java.util.HashSet[ISootCodeElement]()
            for(e2 <- extension; if(e1 != e2)){
                targets.add(e2.getSecond)
            }
            dataflows += { e1.getSecond -> targets}
        }
        dataflows
    }

}