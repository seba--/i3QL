package unisson.model

import mock.vespucci.{ArchitectureModel, GlobalArchitectureModel, IncomingConstraint, Ensemble}
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import sae.bytecode.{BytecodeDatabase, MaterializedDatabase}
import de.tud.cs.st.vespucci.interfaces.IViolation
import sae.collections.{Conversions, QueryResult}
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.Field
import unisson.query.code_model.SourceElement

/**
 *
 * Author: Ralf Mitschke
 * Date: 18.01.12
 * Time: 12:05
 *
 */
class TestUnissonDatabaseUpdates
        extends ShouldMatchers
{

    import Ordering._

    @Test
    def testGlobalModelQueryChange() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = IncomingConstraint("field_type", ensembleB, ensembleA)
        val constraints = Set(
            constraint
        )

        val globalModelV0 = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(globalModelV0)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")
        val e = ObjectType("test/E")
        val fieldRefBToA = Field(b, "fieldInB", a)
        val fieldRefCToA = Field(c, "fieldInC", a)
        val fieldRefDToA = Field(d, "fieldInD", a)

        bc.classfiles.element_added(a)
        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)
        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)
        bc.classfiles.element_added(d)
        bc.classfile_fields.element_added(fieldRefDToA)
        bc.classfiles.element_added(e)

        result.asList.sorted should be(
            List(
                Violation(
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    ""
                )
            )
        )

        val newEnsembleC= Ensemble("C", "class_with_members('test','D')", Set.empty)
        val globalModelV1 = GlobalArchitectureModel(
            ensembleA, ensembleB, newEnsembleC
        )

        db.updateGlobalModel(globalModelV0, globalModelV1)

        result.asList.sorted should be(
            List(
                Violation(
                    constraint,
                    newEnsembleC,
                    ensembleA,
                    SourceElement(fieldRefDToA),
                    SourceElement(a),
                    ""
                )
            )
        )
    }

}