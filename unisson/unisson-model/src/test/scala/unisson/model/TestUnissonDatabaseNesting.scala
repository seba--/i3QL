package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import sae.bytecode.{BytecodeDatabase, MaterializedDatabase}
import sae.collections.{Conversions, QueryResult}
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.FieldDeclaration
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

    import UnissonOrdering._

    @Test
    def testEnsembleElementsForNesting() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        val globalModel = GlobalArchitectureModel(Set(ensembleA))

        val result: QueryResult[(IEnsemble, ICodeElement)] = Conversions
                .lazyViewToResult(db.ensemble_elements)

        db.addGlobalModel(globalModel)

        val a1 = ObjectType("test/A1")
        val a2 = ObjectType("test/A2")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA1 = FieldDeclaration(b, "fieldInB", a1)
        val fieldRefBToA2 = FieldDeclaration(b, "fieldInB", a2)
        val fieldRefCToA1 = FieldDeclaration(c, "fieldInC", a1)
        val fieldRefCToA2 = FieldDeclaration(c, "fieldInC", a2)

        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(fieldRefBToA1)
        bc.declared_fields.element_added(fieldRefBToA2)
        bc.declared_types.element_added(c)
        bc.declared_fields.element_added(fieldRefCToA1)
        bc.declared_fields.element_added(fieldRefCToA2)

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

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")

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

        val fieldRefBToA1 = FieldDeclaration(b, "fieldInB", a1)
        val fieldRefBToA2 = FieldDeclaration(b, "fieldInB", a2)
        val fieldRefCToA1 = FieldDeclaration(c, "fieldInC", a1)
        val fieldRefCToA2 = FieldDeclaration(c, "fieldInC", a2)

        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(fieldRefBToA1)
        bc.declared_fields.element_added(fieldRefBToA2)
        bc.declared_types.element_added(c)
        bc.declared_fields.element_added(fieldRefCToA1)
        bc.declared_fields.element_added(fieldRefCToA2)

        result.asList.sorted should be(
            List(
                Violation(
                    constraint,
                    ensembleC,
                    ensembleA1,
                    SourceElement(fieldRefCToA1),
                    SourceElement(a1),
                    "field_type",
                    "test"
                ),
                Violation(
                    constraint,
                    ensembleC,
                    ensembleA2,
                    SourceElement(fieldRefCToA2),
                    SourceElement(a2),
                    "field_type",
                    "test"
                )
            )
        )
    }

    @Test
    def testGlobalIncomingWithChildrenOnlyWithoutViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        val ensembleB1 = Ensemble("B1", "class_with_members('test','B1')")
        val ensembleB2 = Ensemble("B2", "class_with_members('test','B2')")
        val ensembleB = Ensemble("B", "derived", ensembleB1, ensembleB2)

        val constraint = GlobalIncomingConstraint("field_type", ensembleB, ensembleA)

        val globalModel = GlobalArchitectureModel(Set(ensembleA, ensembleB))
        val model = ArchitectureModel(Set(ensembleA, ensembleB), Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(globalModel)

        val a1 = ObjectType("test/A1")
        val a2 = ObjectType("test/A2")
        val b1 = ObjectType("test/B1")
        val b2 = ObjectType("test/B2")

        val fieldRefB1ToA1 = FieldDeclaration(b1, "fieldB1ToA1", a1)
        val fieldRefB1ToA2 = FieldDeclaration(b1, "fieldB1ToA2", a2)
        val fieldRefB2ToA1 = FieldDeclaration(b2, "fieldB2ToA1", a1)
        val fieldRefB2ToA2 = FieldDeclaration(b2, "fieldB2ToA2", a2)

        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(b1)
        bc.declared_fields.element_added(fieldRefB1ToA1)
        bc.declared_fields.element_added(fieldRefB1ToA2)
        bc.declared_types.element_added(b2)
        bc.declared_fields.element_added(fieldRefB2ToA1)
        bc.declared_fields.element_added(fieldRefB2ToA2)

        result.asList.sorted should be(Nil)

    }

    @Test
    def testGlobalIncomingWithChildrenOnlyWithViolations() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        val ensembleB1 = Ensemble("B1", "class_with_members('test','B1')")
        val ensembleB2 = Ensemble("B2", "class_with_members('test','B2')")
        val ensembleB = Ensemble("B", "derived", ensembleB1, ensembleB2)

        val constraint = GlobalIncomingConstraint("field_type", EmptyEnsemble, ensembleA)

        val globalModel = GlobalArchitectureModel(Set(ensembleA, ensembleB))
        val model = ArchitectureModel(Set(ensembleA), Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(globalModel)

        val a1 = ObjectType("test/A1")
        val a2 = ObjectType("test/A2")
        val b1 = ObjectType("test/B1")
        val b2 = ObjectType("test/B2")

        val fieldRefB1ToA1 = FieldDeclaration(b1, "fieldB1ToA1", a1)
        val fieldRefB1ToA2 = FieldDeclaration(b1, "fieldB1ToA2", a2)
        val fieldRefB2ToA1 = FieldDeclaration(b2, "fieldB2ToA1", a1)
        val fieldRefB2ToA2 = FieldDeclaration(b2, "fieldB2ToA2", a2)

        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(b1)
        bc.declared_fields.element_added(fieldRefB1ToA1)
        bc.declared_fields.element_added(fieldRefB1ToA2)
        bc.declared_types.element_added(b2)
        bc.declared_fields.element_added(fieldRefB2ToA1)
        bc.declared_fields.element_added(fieldRefB2ToA2)

        result.asList.sorted should be(
            List(
                Violation(
                    constraint,
                    ensembleB1,
                    ensembleA1,
                    SourceElement(fieldRefB1ToA1),
                    SourceElement(a1),
                    "field_type",
                    "test"
                ),
                Violation(
                    constraint,
                    ensembleB1,
                    ensembleA2,
                    SourceElement(fieldRefB1ToA2),
                    SourceElement(a2),
                    "field_type",
                    "test"
                ),
                Violation(
                    constraint,
                    ensembleB2,
                    ensembleA1,
                    SourceElement(fieldRefB2ToA1),
                    SourceElement(a1),
                    "field_type",
                    "test"
                ),
                Violation(
                    constraint,
                    ensembleB2,
                    ensembleA2,
                    SourceElement(fieldRefB2ToA2),
                    SourceElement(a2),
                    "field_type",
                    "test"
                )
            )
        )
    }

}