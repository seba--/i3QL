package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import sae.bytecode.{BytecodeDatabase, MaterializedDatabase}
import sae.collections.{Conversions, QueryResult}
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.Field
import unisson.query.code_model.SourceElement
import org.junit.Test
import de.tud.cs.st.vespucci.model.IEnsemble
import de.tud.cs.st.vespucci.interfaces.{ICodeElement, IViolation}

/**
 *
 * Author: Ralf Mitschke
 * Date: 18.01.12
 * Time: 12:05
 *
 */
class TestUnissonDatabaseNesting
        extends ShouldMatchers
{

    import Ordering._

    @Test
    def testEnsembleElementsForNesting() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')", Set.empty)
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')", Set.empty)
        val ensembleA = Ensemble("A", "derived", Set(ensembleA1, ensembleA2))

        val globalModel = GlobalArchitectureModel(Set(ensembleA))

        val result: QueryResult[(IEnsemble, SourceElement[AnyRef])] = Conversions.lazyViewToResult(db.global_ensemble_elements)

        db.addGlobalModel(globalModel)

        val a1 = ObjectType("test/A1")
        val a2 = ObjectType("test/A2")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA1 = Field(b, "fieldInB", a1)
        val fieldRefBToA2 = Field(b, "fieldInB", a2)
        val fieldRefCToA1 = Field(c, "fieldInC", a1)
        val fieldRefCToA2 = Field(c, "fieldInC", a2)

        bc.classfiles.element_added(a1)
        bc.classfiles.element_added(a2)
        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA1)
        bc.classfile_fields.element_added(fieldRefBToA2)
        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA1)
        bc.classfile_fields.element_added(fieldRefCToA2)

        result.asList.sorted should be(
            List(
                (ensembleA1, SourceElement(a1)),
                (ensembleA2, SourceElement(a2))

            )
        )
    }

    @Test
    def testGlobalIncomingToParentWithViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')", Set.empty)
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')", Set.empty)
        val ensembleA = Ensemble("A", "derived", Set(ensembleA1, ensembleA2))
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)

        val constraint = GlobalIncomingConstraint("field_type", ensembleB, ensembleA)

        val globalModel = GlobalArchitectureModel(Set(ensembleA, ensembleB, ensembleC))
        val model = ArchitectureModel(Set(ensembleA, ensembleB, ensembleC), Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(globalModel)

        val a1 = ObjectType("test/A1")
        val a2 = ObjectType("test/A2")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA1 = Field(b, "fieldInB", a1)
        val fieldRefBToA2 = Field(b, "fieldInB", a2)
        val fieldRefCToA1 = Field(c, "fieldInC", a1)
        val fieldRefCToA2 = Field(c, "fieldInC", a2)

        bc.classfiles.element_added(a1)
        bc.classfiles.element_added(a2)
        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA1)
        bc.classfile_fields.element_added(fieldRefBToA2)
        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA1)
        bc.classfile_fields.element_added(fieldRefCToA2)

        result.asList.sorted should be(
            List(
                Violation(
                    constraint,
                    ensembleC,
                    ensembleA1,
                    SourceElement(fieldRefCToA1),
                    SourceElement(a1),
                    ""
                ),
                Violation(
                    constraint,
                    ensembleC,
                    ensembleA2,
                    SourceElement(fieldRefCToA2),
                    SourceElement(a2),
                    ""
                )
            )
        )
    }

}