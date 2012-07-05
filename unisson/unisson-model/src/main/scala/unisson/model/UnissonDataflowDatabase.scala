package unisson.model

import kinds.DependencyKind
import kinds.primitive.DataFlowKind
import sae.bytecode.Database
import sae.bytecode.model.dependencies.Dependency
import unisson.query.code_model.SourceElement
import collection.JavaConversions
import sae.LazyView
import de.tud.cs.st.vespucci.interfaces.IPair
import soot.bridge.{ISootCodeElement, DataFlowEvaluator}
import de.tud.cs.st.vespucci.model.{IArchitectureModel, IEnsemble}
import sae.collections.{Table, QueryResult}

/**
 *
 * Author: Ralf Mitschke
 * Date: 15.03.12
 * Time: 10:40
 *
 */
class UnissonDataflowDatabase(bc: Database, val dataFlowEvaluator: DataFlowEvaluator)
        extends UnissonDatabase(bc) with IUnissonArchitectureModelDatabase
{

    import sae.syntax.RelationalAlgebraSyntax._

    def dataflow_dependencies: LazyView[(DependencyKind, Dependency[AnyRef, AnyRef])] = dataflow_view

    private val dataflow_view: DataFlowView = new DataFlowView()

    protected val kind_and_dependency_view_with_data_flow = kind_and_dependency_view ∪ dataflow_dependencies

    override def kind_and_dependency = kind_and_dependency_view_with_data_flow


    private case class DataFlowDependency(source: AnyRef, target: AnyRef) extends Dependency[AnyRef, AnyRef]

    private class DataFlowView extends Table[(DependencyKind, Dependency[AnyRef, AnyRef])]
    {
        lazy val elements: QueryResult[IPair[IEnsemble, ISootCodeElement]] =
            Π((t: (IEnsemble, SourceElement[AnyRef])) =>
                new IPair[IEnsemble, ISootCodeElement]
                {
                    def getFirst = t._1

                    def getSecond = t._2.asInstanceOf[ISootCodeElement]
                })(leaf_ensemble_elements)

        def update() {
            foreach(this -= _) // clear the table
            val list = JavaConversions.seqAsJavaList(elements.asList)
            val sootResults = dataFlowEvaluator.determineDataFlows(list)
            val keys = JavaConversions.iterableAsScalaIterable(sootResults.keySet())
            for (key <- keys; SourceElement(source) = key) {
                val dataflows = JavaConversions.iterableAsScalaIterable(sootResults.get(key))
                for (SourceElement(target) <- dataflows) {
                    this +=(DataFlowKind, DataFlowDependency(source, target))
                }
            }
        }
    }

    override def addGlobalModel(model: IArchitectureModel) = {
        super.addGlobalModel(model)
        dataflow_view.update()
    }

    override def removeGlobalModel(model: IArchitectureModel) = {
        super.removeGlobalModel(model)
        dataflow_view.update()
    }

    override def updateGlobalModel(oldModel: IArchitectureModel, newModel: IArchitectureModel) = {
        super.updateGlobalModel(oldModel, newModel)
        dataflow_view.update()
    }

    def updateDataFlows() {
        dataflow_view.update()
    }
}


