package unisson.model

import kinds.primitive.DataFlowKind
import sae.bytecode.Database
import sae.bytecode.model.dependencies.Dependency
import unisson.query.code_model.SourceElement
import collection.JavaConversions
import sae.Relation
import de.tud.cs.st.vespucci.interfaces.{ICodeElement, IPair}
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

    private def dataflow_dependencies: Relation[Dependency[AnyRef, AnyRef]] = dataflow_view

    private lazy val dataflow_view = new DataFlowView()

    override def source_code_dependencies = internal_source_code_dependencies ∪
            dependencyView_to_tupleView(dataflow_dependencies, DataFlowKind)


    private case class DataFlowDependency(source: AnyRef, target: AnyRef) extends Dependency[AnyRef, AnyRef]

    private class DataFlowView extends Table[(Dependency[AnyRef, AnyRef])]
    {
        lazy val elements: QueryResult[IPair[IEnsemble, ISootCodeElement]] =
            Π((t: (IEnsemble, ICodeElement)) =>
                new IPair[IEnsemble, ISootCodeElement]
                {
                    def getFirst = t._1

                    def getSecond = t._2.asInstanceOf[ISootCodeElement]
                })(ensemble_elements)

        def update() {
            foreach(this -= _) // clear the table
            val list = JavaConversions.seqAsJavaList(elements.asList)
            val sootResults = dataFlowEvaluator.determineDataFlows(list)
            val keys = JavaConversions.iterableAsScalaIterable(sootResults.keySet())
            for (key <- keys; SourceElement(source) = key) {
                val dataflows = JavaConversions.iterableAsScalaIterable(sootResults.get(key))
                for (SourceElement(target) <- dataflows) {
                    this += (DataFlowDependency(source, target))
                }
            }
        }
    }

    override def setRepository(model: IArchitectureModel) = {
        super.setRepository(model)
        dataflow_view.update()
    }

    override def unsetRepository(model: IArchitectureModel) = {
        super.unsetRepository(model)
        dataflow_view.update()
    }

    override def updateRepository(oldModel: IArchitectureModel, newModel: IArchitectureModel) = {
        super.updateRepository(oldModel, newModel)
        dataflow_view.update()
    }

    def updateDataFlows() {
        dataflow_view.update()
    }
}


