package unisson.model

import impl._
import kinds.primitive.FieldTypeKind
import org.scalatest.matchers.ShouldMatchers
import de.tud.cs.st.bat.resolved.ObjectType
import unisson.query.code_model.SourceElementFactory
import org.junit.{Assert, Test}
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure.FieldDeclaration
import UnissonOrdering._

/**
 *
 * Author: Ralf Mitschke
 * Date: 18.01.12
 *
 * Test the correctness of finding violations w.r.t. inner ensembles
 */
class TestUnissonDatabaseNestingViolations
    extends ShouldMatchers
{

    @Test
    def testInternalNotAllowedViolation() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.violations

        val ensembleA1 = Ensemble ("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble ("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble ("A2", "derived", ensembleA1, ensembleA2)
        val ensembles = Set (ensembleA)

        val sliceName = "test"

        val c = NotAllowedConstraint ("field_type", ensembleA1, ensembleA2)
        val constraints = Set (c)

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, sliceName)

        db.setRepository (global)
        db.addSlice (model)


        val a1 = ObjectType ("test/A1")
        val a2 = ObjectType ("test/A2")
        val fieldRefA1ToA2 = FieldDeclaration (a1, "myA2", a2)


        bc.typeDeclarations.element_added (a1)
        bc.fieldDeclarations.element_added (fieldRefA1ToA2)
        bc.typeDeclarations.element_added (a2)

        db.violations.asList.sorted should be (
            List (
                Violation (
                    c,
                    ensembleA1,
                    ensembleA2,
                    SourceElementFactory (fieldRefA1ToA2),
                    SourceElementFactory (a2),
                    FieldTypeKind.asVespucciString,
                    sliceName
                )
            )
        )

    }


    @Test
    def testLocalIncomingExemptsAllSourceChildren() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.violations

        val ensembleA1 = Ensemble ("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble ("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble ("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble ("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble ("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble ("A", "derived", ensembleA1, ensembleA2, ensembleA3)

        val ensembleB = Ensemble ("B", "class_with_members('test','B')")

        val ensembles = Set (ensembleA, ensembleB)


        db.setRepository (Repository (ensembles))

        db.addSlice (Concern (ensembles, Set (IncomingConstraint (FieldTypeKind
            .asVespucciString, ensembleA, ensembleB)), "test"))

        val b = ObjectType ("test/B")

        val a1 = ObjectType ("test/inner/A1")
        val a2 = ObjectType ("test/inner/A2")

        val a4 = ObjectType ("test/inner/inner/A4")
        val a5 = ObjectType ("test/inner/inner/A5")


        val fieldRefA1ToB = FieldDeclaration (a1, "fieldInA1", b)
        val fieldRefA2ToB = FieldDeclaration (a2, "fieldInA2", b)
        val fieldRefA4ToB = FieldDeclaration (a4, "fieldInA4", b)
        val fieldRefA5ToB = FieldDeclaration (a5, "fieldInA5", b)


        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)
        bc.typeDeclarations.element_added (a4)
        bc.typeDeclarations.element_added (a5)

        bc.typeDeclarations.element_added (b)

        bc.fieldDeclarations.element_added (fieldRefA1ToB)
        bc.fieldDeclarations.element_added (fieldRefA2ToB)
        bc.fieldDeclarations.element_added (fieldRefA4ToB)
        bc.fieldDeclarations.element_added (fieldRefA5ToB)

        db.ensemble_elements.asList.sorted should be (
            List (
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (fieldRefA1ToB)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA, SourceElementFactory (fieldRefA2ToB)),
                (ensembleA, SourceElementFactory (a4)),
                (ensembleA, SourceElementFactory (fieldRefA4ToB)),
                (ensembleA, SourceElementFactory (a5)),
                (ensembleA, SourceElementFactory (fieldRefA5ToB)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA1, SourceElementFactory (fieldRefA1ToB)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleA2, SourceElementFactory (fieldRefA2ToB)),
                (ensembleA3, SourceElementFactory (a4)),
                (ensembleA3, SourceElementFactory (fieldRefA4ToB)),
                (ensembleA3, SourceElementFactory (a5)),
                (ensembleA3, SourceElementFactory (fieldRefA5ToB)),
                (ensembleA4, SourceElementFactory (a4)),
                (ensembleA4, SourceElementFactory (fieldRefA4ToB)),
                (ensembleA5, SourceElementFactory (a5)),
                (ensembleA5, SourceElementFactory (fieldRefA5ToB)),
                (ensembleB, SourceElementFactory (b))
            )
        )

        db.ensemble_dependencies.asList.sorted should be (
            List (
                (ensembleA, ensembleB, SourceElementFactory (fieldRefA1ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElementFactory (fieldRefA2ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElementFactory (fieldRefA4ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElementFactory (fieldRefA5ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA1, ensembleB, SourceElementFactory (fieldRefA1ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA2, ensembleB, SourceElementFactory (fieldRefA2ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA3, ensembleB, SourceElementFactory (fieldRefA4ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA3, ensembleB, SourceElementFactory (fieldRefA5ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA4, ensembleB, SourceElementFactory (fieldRefA4ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA5, ensembleB, SourceElementFactory (fieldRefA5ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString)
            )
        )

        db.slice_constraints.asList should not be (Nil)

        db.violations.asList should be (Nil)
    }


    @Test
    def testLocalIncomingViolatesForAllSourceChildren() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.violations

        val ensembleA1 = Ensemble ("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble ("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble ("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble ("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble ("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble ("A", "derived", ensembleA1, ensembleA2, ensembleA3)

        val ensembleB = Ensemble ("B", "class_with_members('test','B')")

        val ensembleC = Ensemble ("C", "class_with_members('test','C')")

        val ensembles = Set (ensembleA, ensembleB, ensembleC)


        db.setRepository (Repository (ensembles))

        val constraint = IncomingConstraint (FieldTypeKind.asVespucciString, ensembleC, ensembleB)
        val contextName = "test"
        db.addSlice (Concern (ensembles, Set (constraint), contextName))

        val b = ObjectType ("test/B")

        val c = ObjectType ("test/C")

        val a1 = ObjectType ("test/inner/A1")
        val a2 = ObjectType ("test/inner/A2")

        val a4 = ObjectType ("test/inner/inner/A4")
        val a5 = ObjectType ("test/inner/inner/A5")


        val fieldRefA1ToB = FieldDeclaration (a1, "fieldInA1", b)
        val fieldRefA2ToB = FieldDeclaration (a2, "fieldInA2", b)
        val fieldRefA4ToB = FieldDeclaration (a4, "fieldInA4", b)
        val fieldRefA5ToB = FieldDeclaration (a5, "fieldInA5", b)

        val fieldRefCToB = FieldDeclaration (c, "fieldInC", b)

        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)
        bc.typeDeclarations.element_added (a4)
        bc.typeDeclarations.element_added (a5)

        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)

        bc.fieldDeclarations.element_added (fieldRefA1ToB)
        bc.fieldDeclarations.element_added (fieldRefA2ToB)
        bc.fieldDeclarations.element_added (fieldRefA4ToB)
        bc.fieldDeclarations.element_added (fieldRefA5ToB)

        bc.fieldDeclarations.element_added (fieldRefCToB)

        db.ensemble_elements.asList.sorted should be (
            List (
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (fieldRefA1ToB)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA, SourceElementFactory (fieldRefA2ToB)),
                (ensembleA, SourceElementFactory (a4)),
                (ensembleA, SourceElementFactory (fieldRefA4ToB)),
                (ensembleA, SourceElementFactory (a5)),
                (ensembleA, SourceElementFactory (fieldRefA5ToB)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA1, SourceElementFactory (fieldRefA1ToB)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleA2, SourceElementFactory (fieldRefA2ToB)),
                (ensembleA3, SourceElementFactory (a4)),
                (ensembleA3, SourceElementFactory (fieldRefA4ToB)),
                (ensembleA3, SourceElementFactory (a5)),
                (ensembleA3, SourceElementFactory (fieldRefA5ToB)),
                (ensembleA4, SourceElementFactory (a4)),
                (ensembleA4, SourceElementFactory (fieldRefA4ToB)),
                (ensembleA5, SourceElementFactory (a5)),
                (ensembleA5, SourceElementFactory (fieldRefA5ToB)),
                (ensembleB, SourceElementFactory (b)),
                (ensembleC, SourceElementFactory (c)),
                (ensembleC, SourceElementFactory (fieldRefCToB))
            )
        )

        db.ensemble_dependencies.asList.sorted should be (
            List (
                (ensembleA, ensembleB, SourceElementFactory (fieldRefA1ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElementFactory (fieldRefA2ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElementFactory (fieldRefA4ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElementFactory (fieldRefA5ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA1, ensembleB, SourceElementFactory (fieldRefA1ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA2, ensembleB, SourceElementFactory (fieldRefA2ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA3, ensembleB, SourceElementFactory (fieldRefA4ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA3, ensembleB, SourceElementFactory (fieldRefA5ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA4, ensembleB, SourceElementFactory (fieldRefA4ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA5, ensembleB, SourceElementFactory (fieldRefA5ToB), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleC, ensembleB, SourceElementFactory (fieldRefCToB), SourceElementFactory (b), FieldTypeKind.asVespucciString)
            )
        )

        db.slice_constraints.asList should not be (Nil)

        db.violations.asList.sorted should be (
            List (
                Violation (
                    constraint,
                    ensembleA, ensembleB,
                    SourceElementFactory (fieldRefA1ToB), SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleA, ensembleB,
                    SourceElementFactory (fieldRefA2ToB), SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleA, ensembleB,
                    SourceElementFactory (fieldRefA4ToB), SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleA, ensembleB,
                    SourceElementFactory (fieldRefA5ToB), SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleA1, ensembleB,
                    SourceElementFactory (fieldRefA1ToB), SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleA2, ensembleB, SourceElementFactory (fieldRefA2ToB),
                    SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleA3, ensembleB,
                    SourceElementFactory (fieldRefA4ToB), SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleA3, ensembleB,
                    SourceElementFactory (fieldRefA5ToB), SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleA4, ensembleB,
                    SourceElementFactory (fieldRefA4ToB), SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleA5, ensembleB,
                    SourceElementFactory (fieldRefA5ToB), SourceElementFactory (b),
                    FieldTypeKind.asVespucciString,
                    contextName
                )
            )
        )
    }


    @Test
    def testLocalOutgoingExemptsAllTargetChildren() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.violations

        val ensembleA1 = Ensemble ("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble ("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble ("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble ("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble ("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble ("A", "derived", ensembleA1, ensembleA2, ensembleA3)

        val ensembleB = Ensemble ("B", "class_with_members('test','B')")

        val ensembles = Set (ensembleA, ensembleB)


        db.setRepository (Repository (ensembles))

        db.addSlice (Concern (ensembles, Set (OutgoingConstraint (FieldTypeKind
            .asVespucciString, ensembleB, ensembleA)), "test"))

        val b = ObjectType ("test/B")

        val a1 = ObjectType ("test/inner/A1")
        val a2 = ObjectType ("test/inner/A2")

        val a4 = ObjectType ("test/inner/inner/A4")
        val a5 = ObjectType ("test/inner/inner/A5")


        val fieldRefBToA1 = FieldDeclaration (b, "fieldA1", a1)
        val fieldRefBToA2 = FieldDeclaration (b, "fieldA2", a2)
        val fieldRefBToA4 = FieldDeclaration (b, "fieldA4", a4)
        val fieldRefBToA5 = FieldDeclaration (b, "fieldA5", a5)


        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)
        bc.typeDeclarations.element_added (a4)
        bc.typeDeclarations.element_added (a5)

        bc.typeDeclarations.element_added (b)

        bc.fieldDeclarations.element_added (fieldRefBToA1)
        bc.fieldDeclarations.element_added (fieldRefBToA2)
        bc.fieldDeclarations.element_added (fieldRefBToA4)
        bc.fieldDeclarations.element_added (fieldRefBToA5)

        db.ensemble_elements.asList.sorted should be (
            List (
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA, SourceElementFactory (a4)),
                (ensembleA, SourceElementFactory (a5)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleA3, SourceElementFactory (a4)),
                (ensembleA3, SourceElementFactory (a5)),
                (ensembleA4, SourceElementFactory (a4)),
                (ensembleA5, SourceElementFactory (a5)),
                (ensembleB, SourceElementFactory (b)),
                (ensembleB, SourceElementFactory (fieldRefBToA1)),
                (ensembleB, SourceElementFactory (fieldRefBToA2)),
                (ensembleB, SourceElementFactory (fieldRefBToA4)),
                (ensembleB, SourceElementFactory (fieldRefBToA5))
            )
        )


        Assert.assertEquals (
            db.ensemble_dependencies.asList.sorted,
            List (
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA1), SourceElementFactory (a1), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA2), SourceElementFactory (a2), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA4), SourceElementFactory (a4), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA5), SourceElementFactory (a5), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA1, SourceElementFactory (fieldRefBToA1), SourceElementFactory (a1), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA2, SourceElementFactory (fieldRefBToA2), SourceElementFactory (a2), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA3, SourceElementFactory (fieldRefBToA4), SourceElementFactory (a4), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA3, SourceElementFactory (fieldRefBToA5), SourceElementFactory (a5), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA4, SourceElementFactory (fieldRefBToA4), SourceElementFactory (a4), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA5, SourceElementFactory (fieldRefBToA5), SourceElementFactory (a5), FieldTypeKind.asVespucciString)
            )
        )

        db.slice_constraints.asList should not be (Nil)

        db.violations.asList should be (Nil)
    }

    @Test
    def testLocalOutgoingViolatesForAllTargetChildren() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.violations

        val ensembleA1 = Ensemble ("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble ("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble ("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble ("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble ("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble ("A", "derived", ensembleA1, ensembleA2, ensembleA3)

        val ensembleB = Ensemble ("B", "class_with_members('test','B')")

        val ensembleC = Ensemble ("C", "class_with_members('test','C')")

        val ensembles = Set (ensembleA, ensembleB, ensembleC)


        db.setRepository (Repository (ensembles))

        val constraint = OutgoingConstraint (FieldTypeKind.asVespucciString, ensembleB, ensembleC)
        val contextName = "test"
        db.addSlice (Concern (ensembles, Set (constraint), contextName))

        val b = ObjectType ("test/B")

        val c = ObjectType ("test/C")

        val a1 = ObjectType ("test/inner/A1")
        val a2 = ObjectType ("test/inner/A2")

        val a4 = ObjectType ("test/inner/inner/A4")
        val a5 = ObjectType ("test/inner/inner/A5")


        val fieldRefBToA1 = FieldDeclaration (b, "fieldA1", a1)
        val fieldRefBToA2 = FieldDeclaration (b, "fieldA2", a2)
        val fieldRefBToA4 = FieldDeclaration (b, "fieldA4", a4)
        val fieldRefBToA5 = FieldDeclaration (b, "fieldA5", a5)

        val fieldRefBToC = FieldDeclaration (b, "fieldC", c)

        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)
        bc.typeDeclarations.element_added (a4)
        bc.typeDeclarations.element_added (a5)

        bc.typeDeclarations.element_added (b)

        bc.typeDeclarations.element_added (c)

        bc.fieldDeclarations.element_added (fieldRefBToA1)
        bc.fieldDeclarations.element_added (fieldRefBToA2)
        bc.fieldDeclarations.element_added (fieldRefBToA4)
        bc.fieldDeclarations.element_added (fieldRefBToA5)

        bc.fieldDeclarations.element_added (fieldRefBToC)

        Assert.assertEquals (
            db.ensemble_elements.asList.sorted,
            List (
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA, SourceElementFactory (a4)),
                (ensembleA, SourceElementFactory (a5)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleA3, SourceElementFactory (a4)),
                (ensembleA3, SourceElementFactory (a5)),
                (ensembleA4, SourceElementFactory (a4)),
                (ensembleA5, SourceElementFactory (a5)),
                (ensembleB, SourceElementFactory (b)),
                (ensembleB, SourceElementFactory (fieldRefBToA1)),
                (ensembleB, SourceElementFactory (fieldRefBToA2)),
                (ensembleB, SourceElementFactory (fieldRefBToA4)),
                (ensembleB, SourceElementFactory (fieldRefBToA5)),
                (ensembleB, SourceElementFactory (fieldRefBToC)),
                (ensembleC, SourceElementFactory (c))
            )
        )


        Assert.assertEquals (
            db.ensemble_dependencies.asList.sorted,
            List (
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA1), SourceElementFactory (a1), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA2), SourceElementFactory (a2), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA4), SourceElementFactory (a4), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA5), SourceElementFactory (a5), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA1, SourceElementFactory (fieldRefBToA1), SourceElementFactory (a1), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA2, SourceElementFactory (fieldRefBToA2), SourceElementFactory (a2), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA3, SourceElementFactory (fieldRefBToA4), SourceElementFactory (a4), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA3, SourceElementFactory (fieldRefBToA5), SourceElementFactory (a5), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA4, SourceElementFactory (fieldRefBToA4), SourceElementFactory (a4), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA5, SourceElementFactory (fieldRefBToA5), SourceElementFactory (a5), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleC, SourceElementFactory (fieldRefBToC), SourceElementFactory (c), FieldTypeKind.asVespucciString)
            )
        )

        db.slice_constraints.asList should not be (Nil)

        Assert.assertEquals (db.violations.asList.sorted,
            List (
                Violation (
                    constraint,
                    ensembleB, ensembleA,
                    SourceElementFactory (fieldRefBToA1), SourceElementFactory (a1),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleB, ensembleA,
                    SourceElementFactory (fieldRefBToA2), SourceElementFactory (a2),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleB, ensembleA,
                    SourceElementFactory (fieldRefBToA4), SourceElementFactory (a4),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleB, ensembleA,
                    SourceElementFactory (fieldRefBToA5), SourceElementFactory (a5),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleB, ensembleA1,
                    SourceElementFactory (fieldRefBToA1), SourceElementFactory (a1),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleB, ensembleA2,
                    SourceElementFactory (fieldRefBToA2), SourceElementFactory (a2),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleB, ensembleA3,
                    SourceElementFactory (fieldRefBToA4), SourceElementFactory (a4),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleB, ensembleA3,
                    SourceElementFactory (fieldRefBToA5), SourceElementFactory (a5),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleB, ensembleA4,
                    SourceElementFactory (fieldRefBToA4), SourceElementFactory (a4),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation (
                    constraint,
                    ensembleB, ensembleA5,
                    SourceElementFactory (fieldRefBToA5), SourceElementFactory (a5),
                    FieldTypeKind.asVespucciString,
                    contextName
                )
            )
        )
    }
}


