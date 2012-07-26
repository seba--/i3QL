package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import sae.bytecode.{BytecodeDatabase, MaterializedDatabase}
import sae.collections.{Conversions, QueryResult}
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.FieldDeclaration
import unisson.query.code_model.SourceElement
import org.junit.{Ignore, Test}
import de.tud.cs.st.vespucci.model.IEnsemble
import de.tud.cs.st.vespucci.interfaces.{ICodeElement, IViolation}

/**
 *
 * Author: Ralf Mitschke
 * Date: 18.01.12
 *
 * Test the addition/removal/update of ensembles w.r.t. to the correct structure as follows:
 * 1. db.ensembles must be the flattened representation of all ensembles
 * 2. db.children must contain correct parent-child relations
 * 3. db.descendants must contain correct descendant relations (i.e., all transitive children)
 */
class TestUnissonDatabaseNestingStructure
        extends ShouldMatchers
{

    import UnissonOrdering._

    import sae.collections.Conversions._

    @Test
    def testAddEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))


        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        db.ensembles.asList.sorted should be(
            List(
                ensembleA,
                ensembleA1,
                ensembleA2
            )
        )

        db.children.asList.sorted should be(
            List(
                (ensembleA, ensembleA1),
                (ensembleA, ensembleA2)
            )
        )

        db.descendants.asList.sorted should be(
            List(
                (ensembleA, ensembleA1),
                (ensembleA, ensembleA2)
            )
        )
    }

    @Test
    def testRemoveEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        db.removeEnsemble(ensembleA)

        db.ensembles.asList.sorted should be(Nil)

        db.children.asList.sorted should be( Nil)

        db.descendants.asList.sorted should be( Nil)
    }

    @Test
    def testUpdateAddEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA3 = Ensemble("A3", "class_with_members('test','A3')")

        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        val ensembleAUpdate = Ensemble("A", "derived", ensembleA3, ensembleA2, ensembleA1)

        db.updateEnsemble(ensembleA, ensembleAUpdate)

        db.ensembles.asList.sorted should be(
            List(
                ensembleAUpdate,
                ensembleA1,
                ensembleA2,
                ensembleA3
            )
        )

        db.children.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA1),
                (ensembleAUpdate, ensembleA2),
                (ensembleAUpdate, ensembleA3)
            )
        )

        db.descendants.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA1),
                (ensembleAUpdate, ensembleA2),
                (ensembleAUpdate, ensembleA3)
            )
        )
    }

    @Test
    def testUpdateRemoveEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")

        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        val ensembleAUpdate = Ensemble("A", "derived", ensembleA1)

        db.updateEnsemble(ensembleA, ensembleAUpdate)

        db.ensembles.asList.sorted should be(
            List(
                ensembleAUpdate,
                ensembleA1
            )
        )

        db.children.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA1)
            )
        )

        db.descendants.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA1)
            )
        )
    }

    @Test
    def testUpdateAddRemoveEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA3 = Ensemble("A3", "class_with_members('test','A3')")

        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        val ensembleAUpdate = Ensemble("A", "derived", ensembleA1, ensembleA3)

        db.updateEnsemble(ensembleA, ensembleAUpdate)

        db.ensembles.asList.sorted should be(
            List(
                ensembleAUpdate,
                ensembleA1,
                ensembleA3
            )
        )

        db.children.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA1),
                (ensembleAUpdate, ensembleA3)
            )
        )

        db.descendants.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA1),
                (ensembleAUpdate, ensembleA3)
            )
        )
    }

    @Test
    def testUpdateChangeEnsembleChildrenQuery() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2Old = Ensemble("A2", "class_with_members('test','A2Old')")
        val ensembleA2New = Ensemble("A2", "class_with_members('test','A2New')")

        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2Old)

        db.addEnsemble(ensembleA)

        val ensembleAUpdate = Ensemble("A", "derived", ensembleA1, ensembleA2New)

        db.updateEnsemble(ensembleA, ensembleAUpdate)

        db.ensembles.asList.sorted should be(
            List(
                ensembleAUpdate,
                ensembleA1,
                ensembleA2New
            )
        )

        val resultingEnsembleA2 = db.ensembles.asList.find(_.getName == "A2").get
        resultingEnsembleA2.getQuery should be("class_with_members('test','A2New')")
    }

    @Test
    def testAddTwoLevelsOfEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA21 = Ensemble("A2.1", "class_with_members('test','A2.1')")
        val ensembleA22 = Ensemble("A2.2", "class_with_members('test','A2.2')")
        val ensembleA2 = Ensemble("A2", "derived", ensembleA21, ensembleA22)
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        db.ensembles.asList.sorted should be(
            List(
                ensembleA,
                ensembleA1,
                ensembleA2,
                ensembleA21,
                ensembleA22
            )
        )

        db.children.asList.sorted should be(
            List(
                (ensembleA, ensembleA1),
                (ensembleA, ensembleA2),
                (ensembleA2, ensembleA21),
                (ensembleA2, ensembleA22)
            )
        )

        db.descendants.asList.sorted should be(
            List(
                (ensembleA, ensembleA1),
                (ensembleA, ensembleA2),
                (ensembleA, ensembleA21),
                (ensembleA, ensembleA22),
                (ensembleA2, ensembleA21),
                (ensembleA2, ensembleA22)
            )
        )
    }

    @Test
    def testRemoveTwoLevelsOfEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA21 = Ensemble("A2.1", "class_with_members('test','A2.1')")
        val ensembleA22 = Ensemble("A2.2", "class_with_members('test','A2.2')")
        val ensembleA2 = Ensemble("A2", "derived", ensembleA21, ensembleA22)
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        db.removeEnsemble(ensembleA)

        db.ensembles.asList.sorted should be(Nil)

        db.children.asList.sorted should be( Nil)

        db.descendants.asList.sorted should be( Nil)
    }

    @Test
    def testUpdateAddAtFirstAndSecondLevelOfEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA21 = Ensemble("A2.1", "class_with_members('test','A2.1')")
        val ensembleA22 = Ensemble("A2.2", "class_with_members('test','A2.2')")
        val ensembleA2 = Ensemble("A2", "derived", ensembleA21, ensembleA22)
        val ensembleA3 = Ensemble("A3", "class_with_members('test','A3')")

        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        val ensembleA23 = Ensemble("A2.3", "class_with_members('test','A2.3')")
        val ensembleA2Update = Ensemble("A2", "derived", ensembleA23, ensembleA22, ensembleA21)
        val ensembleAUpdate = Ensemble("A", "derived", ensembleA3, ensembleA2Update, ensembleA1)

        db.updateEnsemble(ensembleA, ensembleAUpdate)

        db.ensembles.asList.sorted should be(
            List(
                ensembleAUpdate,
                ensembleA1,
                ensembleA2Update,
                ensembleA21,
                ensembleA22,
                ensembleA23,
                ensembleA3
            )
        )

        db.children.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA1),
                (ensembleAUpdate, ensembleA2Update),
                (ensembleAUpdate, ensembleA3),
                (ensembleA2Update, ensembleA21),
                (ensembleA2Update, ensembleA22),
                (ensembleA2Update, ensembleA23)
            )
        )

        db.descendants.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA1),
                (ensembleAUpdate, ensembleA2Update),
                (ensembleAUpdate, ensembleA21),
                (ensembleAUpdate, ensembleA22),
                (ensembleAUpdate, ensembleA23),
                (ensembleAUpdate, ensembleA3),
                (ensembleA2Update, ensembleA21),
                (ensembleA2Update, ensembleA22),
                (ensembleA2Update, ensembleA23)
            )
        )
    }

    @Test
    def testUpdateRemoveAtFirstAndSecondLevelOfEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA21 = Ensemble("A2.1", "class_with_members('test','A2.1')")
        val ensembleA22 = Ensemble("A2.2", "class_with_members('test','A2.2')")
        val ensembleA2 = Ensemble("A2", "derived", ensembleA21, ensembleA22)

        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        val ensembleA2Update = Ensemble("A2", "derived", ensembleA22)
        val ensembleAUpdate = Ensemble("A", "derived", ensembleA2Update)

        db.updateEnsemble(ensembleA, ensembleAUpdate)

        db.ensembles.asList.sorted should be(
            List(
                ensembleAUpdate,
                ensembleA2Update,
                ensembleA22
            )
        )

        db.children.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA2Update),
                (ensembleA2Update, ensembleA22)
            )
        )

        db.descendants.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA2Update),
                (ensembleAUpdate, ensembleA22),
                (ensembleA2Update, ensembleA22)
            )
        )
    }

    @Test
    def testUpdateAddRemoveAtFirstAndSecondLevelOfEnsembleChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA21 = Ensemble("A2.1", "class_with_members('test','A2.1')")
        val ensembleA22 = Ensemble("A2.2", "class_with_members('test','A2.2')")

        val ensembleA2 = Ensemble("A2", "derived", ensembleA21, ensembleA22)
        val ensembleA3 = Ensemble("A3", "class_with_members('test','A3')")

        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        db.addEnsemble(ensembleA)

        val ensembleA23 = Ensemble("A2.3", "class_with_members('test','A2.3')")
        val ensembleA2Update = Ensemble("A2", "derived", ensembleA23, ensembleA22)
        val ensembleAUpdate = Ensemble("A", "derived", ensembleA3, ensembleA2Update)

        db.updateEnsemble(ensembleA, ensembleAUpdate)

        db.ensembles.asList.sorted should be(
            List(
                ensembleAUpdate,
                ensembleA2Update,
                ensembleA22,
                ensembleA23,
                ensembleA3
            )
        )

        db.children.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA2Update),
                (ensembleAUpdate, ensembleA3),
                (ensembleA2Update, ensembleA22),
                (ensembleA2Update, ensembleA23)
            )
        )

        db.descendants.asList.sorted should be(
            List(
                (ensembleAUpdate, ensembleA2Update),
                (ensembleAUpdate, ensembleA22),
                (ensembleAUpdate, ensembleA23),
                (ensembleAUpdate, ensembleA3),
                (ensembleA2Update, ensembleA22),
                (ensembleA2Update, ensembleA23)
            )
        )
    }

    @Test
    def testUpdateChangeAtFirstAndSecondLevelOfEnsembleChildrenQuery() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1Old = Ensemble("A1", "class_with_members('test','A1.Old')")
        val ensembleA21Old = Ensemble("A2.1", "class_with_members('test','A2.1.Old')")
        val ensembleA22 = Ensemble("A2.2", "class_with_members('test','A2.2')")
        val ensembleA2 = Ensemble("A2", "derived", ensembleA21Old, ensembleA22)

        val ensembleA = Ensemble("A", "derived", ensembleA1Old, ensembleA2)

        db.addEnsemble(ensembleA)

        val ensembleA1New = Ensemble("A1", "class_with_members('test','A1.New')")
        val ensembleA21New = Ensemble("A2.1", "class_with_members('test','A2.1.New')")
        val ensembleA2Update = Ensemble("A2", "derived", ensembleA21New, ensembleA22)
        val ensembleAUpdate = Ensemble("A", "derived", ensembleA1New, ensembleA2Update)

        db.updateEnsemble(ensembleA, ensembleAUpdate)

        db.ensembles.asList.sorted should be(
            List(
                ensembleAUpdate,
                ensembleA1New,
                ensembleA2Update,
                ensembleA21New,
                ensembleA22
            )
        )

        val resultingEnsembleA1 = db.ensembles.asList.find(_.getName == "A1").get
        resultingEnsembleA1.getQuery should be("class_with_members('test','A1.New')")


        val resultingEnsembleA21 = db.ensembles.asList.find(_.getName == "A2.1").get
        resultingEnsembleA21.getQuery should be("class_with_members('test','A2.1.New')")

    }


    @Ignore
    @Test
    def testEnsembleElementsForNesting() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(new MaterializedDatabase(bc))

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)

        val globalModel = Repository(Set(ensembleA))

        val result: QueryResult[(IEnsemble, ICodeElement)] = Conversions
                .lazyViewToResult(db.ensemble_elements)

        db.setRepository(globalModel)

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

    @Ignore
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

        val globalModel = Repository(Set(ensembleA, ensembleB, ensembleC))
        val model = Concern(Set(ensembleA, ensembleB, ensembleC), Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(globalModel)

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

    @Ignore
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

        val globalModel = Repository(Set(ensembleA, ensembleB))
        val model = Concern(Set(ensembleA, ensembleB), Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(globalModel)

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

    @Ignore
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

        val globalModel = Repository(Set(ensembleA, ensembleB))
        val model = Concern(Set(ensembleA), Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(globalModel)

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