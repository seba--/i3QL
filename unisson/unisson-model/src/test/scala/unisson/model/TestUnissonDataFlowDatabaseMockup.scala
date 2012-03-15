package unisson.model

import mock.soot.MockDataFlowEvaluator
import mock.vespucci.{ArchitectureModel, GlobalArchitectureModel, NotAllowedConstraint, Ensemble}
import org.junit.Test
import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.vespucci.interfaces.IViolation
import sae.collections.{Conversions, QueryResult}
import de.tud.cs.st.bat.ObjectType
import unisson.query.code_model.SourceElement
import org.scalatest.matchers.ShouldMatchers

/**
 *
 * Author: Ralf Mitschke
 * Date: 15.03.12
 * Time: 11:08
 *
 */
class TestUnissonDataFlowDatabaseMockup extends ShouldMatchers
{
    @Test
    def testNotAllowedAllViolation() {
        val bc = new BytecodeDatabase()
        val df = new MockDataFlowEvaluator()
        val db = new UnissonDataflowDatabase(bc, df)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembles = Set(ensembleA, ensembleB)

        val c = NotAllowedConstraint("dataflow", ensembleA, ensembleB)

        val constraints = Set(
            c
        )

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")

        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)

        db.updateDataFlows()

        result.asList should be(
            List(
                Violation(
                    c,
                    ensembleA,
                    ensembleB,
                    SourceElement(a),
                    SourceElement(b),
                    "dataflow",
                    "test"
                )
            )
        )
    }
}