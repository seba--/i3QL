package unisson.model

import kinds.primitive.FieldTypeKind
import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.FieldDeclaration
import unisson.query.code_model.SourceElement
import org.junit.{Assert, Test}


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

    import UnissonOrdering._

    import sae.collections.Conversions._

    @Test
    def testInternalNotAllowedViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A2", "derived", ensembleA1, ensembleA2)
        val ensembles = Set(ensembleA)

        val sliceName = "test"

        val c = NotAllowedConstraint("field_type", ensembleA1, ensembleA2)
        val constraints = Set(c)

        val global = Repository(ensembles)
        val model = Concern(ensembles, constraints, sliceName)

        db.setRepository(global)
        db.addSlice(model)


        val a1 = ObjectType("test/A1")
        val a2 = ObjectType("test/A2")
        val fieldRefA1ToA2 = FieldDeclaration(a1, "myA2", a2)


        bc.declared_types.element_added(a1)
        bc.declared_fields.element_added(fieldRefA1ToA2)
        bc.declared_types.element_added(a2)

        db.violations.asList.sorted should be(
            List(
                Violation(
                    c,
                    ensembleA1,
                    ensembleA2,
                    SourceElement(fieldRefA1ToA2),
                    SourceElement(a2),
                    FieldTypeKind.asVespucciString,
                    sliceName
                )
            )
        )

    }


    @Test
    def testLocalIncomingExemptsAllSourceChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2, ensembleA3)

        val ensembleB = Ensemble("B", "class_with_members('test','B')")

        val ensembles = Set(ensembleA, ensembleB)


        db.setRepository(Repository(ensembles))

        db.addSlice(Concern(ensembles, Set(IncomingConstraint(FieldTypeKind
                .asVespucciString, ensembleA, ensembleB)), "test"))

        val b = ObjectType("test/B")

        val a1 = ObjectType("test/inner/A1")
        val a2 = ObjectType("test/inner/A2")

        val a4 = ObjectType("test/inner/inner/A4")
        val a5 = ObjectType("test/inner/inner/A5")


        val fieldRefA1ToB = FieldDeclaration(a1, "fieldInA1", b)
        val fieldRefA2ToB = FieldDeclaration(a2, "fieldInA2", b)
        val fieldRefA4ToB = FieldDeclaration(a4, "fieldInA4", b)
        val fieldRefA5ToB = FieldDeclaration(a5, "fieldInA5", b)


        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(a4)
        bc.declared_types.element_added(a5)

        bc.declared_types.element_added(b)

        bc.declared_fields.element_added(fieldRefA1ToB)
        bc.declared_fields.element_added(fieldRefA2ToB)
        bc.declared_fields.element_added(fieldRefA4ToB)
        bc.declared_fields.element_added(fieldRefA5ToB)

        db.ensemble_elements.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a1)),
                (ensembleA, SourceElement(fieldRefA1ToB)),
                (ensembleA, SourceElement(a2)),
                (ensembleA, SourceElement(fieldRefA2ToB)),
                (ensembleA, SourceElement(a4)),
                (ensembleA, SourceElement(fieldRefA4ToB)),
                (ensembleA, SourceElement(a5)),
                (ensembleA, SourceElement(fieldRefA5ToB)),
                (ensembleA1, SourceElement(a1)),
                (ensembleA1, SourceElement(fieldRefA1ToB)),
                (ensembleA2, SourceElement(a2)),
                (ensembleA2, SourceElement(fieldRefA2ToB)),
                (ensembleA3, SourceElement(a4)),
                (ensembleA3, SourceElement(fieldRefA4ToB)),
                (ensembleA3, SourceElement(a5)),
                (ensembleA3, SourceElement(fieldRefA5ToB)),
                (ensembleA4, SourceElement(a4)),
                (ensembleA4, SourceElement(fieldRefA4ToB)),
                (ensembleA5, SourceElement(a5)),
                (ensembleA5, SourceElement(fieldRefA5ToB)),
                (ensembleB, SourceElement(b))
            )
        )

        db.ensemble_dependencies.asList.sorted should be(
            List(
                (ensembleA, ensembleB, SourceElement(fieldRefA1ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElement(fieldRefA2ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElement(fieldRefA4ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElement(fieldRefA5ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA1, ensembleB, SourceElement(fieldRefA1ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA2, ensembleB, SourceElement(fieldRefA2ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA3, ensembleB, SourceElement(fieldRefA4ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA3, ensembleB, SourceElement(fieldRefA5ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA4, ensembleB, SourceElement(fieldRefA4ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA5, ensembleB, SourceElement(fieldRefA5ToB), SourceElement(b), FieldTypeKind.asVespucciString)
            )
        )

        db.slice_constraints.asList should not be (Nil)

        db.violations.asList should be(Nil)
    }


    @Test
    def testLocalIncomingViolatesForAllSourceChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2, ensembleA3)

        val ensembleB = Ensemble("B", "class_with_members('test','B')")

        val ensembleC = Ensemble("C", "class_with_members('test','C')")

        val ensembles = Set(ensembleA, ensembleB, ensembleC)


        db.setRepository(Repository(ensembles))

        val constraint = IncomingConstraint(FieldTypeKind.asVespucciString, ensembleC, ensembleB)
        val contextName = "test"
        db.addSlice(Concern(ensembles, Set(constraint), contextName))

        val b = ObjectType("test/B")

        val c = ObjectType("test/C")

        val a1 = ObjectType("test/inner/A1")
        val a2 = ObjectType("test/inner/A2")

        val a4 = ObjectType("test/inner/inner/A4")
        val a5 = ObjectType("test/inner/inner/A5")


        val fieldRefA1ToB = FieldDeclaration(a1, "fieldInA1", b)
        val fieldRefA2ToB = FieldDeclaration(a2, "fieldInA2", b)
        val fieldRefA4ToB = FieldDeclaration(a4, "fieldInA4", b)
        val fieldRefA5ToB = FieldDeclaration(a5, "fieldInA5", b)

        val fieldRefCToB = FieldDeclaration(c, "fieldInC", b)

        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(a4)
        bc.declared_types.element_added(a5)

        bc.declared_types.element_added(b)
        bc.declared_types.element_added(c)

        bc.declared_fields.element_added(fieldRefA1ToB)
        bc.declared_fields.element_added(fieldRefA2ToB)
        bc.declared_fields.element_added(fieldRefA4ToB)
        bc.declared_fields.element_added(fieldRefA5ToB)

        bc.declared_fields.element_added(fieldRefCToB)

        db.ensemble_elements.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a1)),
                (ensembleA, SourceElement(fieldRefA1ToB)),
                (ensembleA, SourceElement(a2)),
                (ensembleA, SourceElement(fieldRefA2ToB)),
                (ensembleA, SourceElement(a4)),
                (ensembleA, SourceElement(fieldRefA4ToB)),
                (ensembleA, SourceElement(a5)),
                (ensembleA, SourceElement(fieldRefA5ToB)),
                (ensembleA1, SourceElement(a1)),
                (ensembleA1, SourceElement(fieldRefA1ToB)),
                (ensembleA2, SourceElement(a2)),
                (ensembleA2, SourceElement(fieldRefA2ToB)),
                (ensembleA3, SourceElement(a4)),
                (ensembleA3, SourceElement(fieldRefA4ToB)),
                (ensembleA3, SourceElement(a5)),
                (ensembleA3, SourceElement(fieldRefA5ToB)),
                (ensembleA4, SourceElement(a4)),
                (ensembleA4, SourceElement(fieldRefA4ToB)),
                (ensembleA5, SourceElement(a5)),
                (ensembleA5, SourceElement(fieldRefA5ToB)),
                (ensembleB, SourceElement(b)),
                (ensembleC, SourceElement(c)),
                (ensembleC, SourceElement(fieldRefCToB))
            )
        )

        db.ensemble_dependencies.asList.sorted should be(
            List(
                (ensembleA, ensembleB, SourceElement(fieldRefA1ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElement(fieldRefA2ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElement(fieldRefA4ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElement(fieldRefA5ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA1, ensembleB, SourceElement(fieldRefA1ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA2, ensembleB, SourceElement(fieldRefA2ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA3, ensembleB, SourceElement(fieldRefA4ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA3, ensembleB, SourceElement(fieldRefA5ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA4, ensembleB, SourceElement(fieldRefA4ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA5, ensembleB, SourceElement(fieldRefA5ToB), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleC, ensembleB, SourceElement(fieldRefCToB), SourceElement(b), FieldTypeKind.asVespucciString)
            )
        )

        db.slice_constraints.asList should not be (Nil)

        db.violations.asList.sorted should be(
            List(
                Violation(
                    constraint,
                    ensembleA, ensembleB,
                    SourceElement(fieldRefA1ToB), SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleA, ensembleB,
                    SourceElement(fieldRefA2ToB), SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleA, ensembleB,
                    SourceElement(fieldRefA4ToB), SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleA, ensembleB,
                    SourceElement(fieldRefA5ToB), SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleA1, ensembleB,
                    SourceElement(fieldRefA1ToB), SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleA2, ensembleB, SourceElement(fieldRefA2ToB),
                    SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleA3, ensembleB,
                    SourceElement(fieldRefA4ToB), SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleA3, ensembleB,
                    SourceElement(fieldRefA5ToB), SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleA4, ensembleB,
                    SourceElement(fieldRefA4ToB), SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleA5, ensembleB,
                    SourceElement(fieldRefA5ToB), SourceElement(b),
                    FieldTypeKind.asVespucciString,
                    contextName
                )
            )
        )
    }


    @Test
    def testLocalOutgoingExemptsAllTargetChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2, ensembleA3)

        val ensembleB = Ensemble("B", "class_with_members('test','B')")

        val ensembles = Set(ensembleA, ensembleB)


        db.setRepository(Repository(ensembles))

        db.addSlice(Concern(ensembles, Set(OutgoingConstraint(FieldTypeKind
                .asVespucciString, ensembleB, ensembleA)), "test"))

        val b = ObjectType("test/B")

        val a1 = ObjectType("test/inner/A1")
        val a2 = ObjectType("test/inner/A2")

        val a4 = ObjectType("test/inner/inner/A4")
        val a5 = ObjectType("test/inner/inner/A5")


        val fieldRefBToA1 = FieldDeclaration(b, "fieldA1", a1)
        val fieldRefBToA2 = FieldDeclaration(b, "fieldA2", a2)
        val fieldRefBToA4 = FieldDeclaration(b, "fieldA4", a4)
        val fieldRefBToA5 = FieldDeclaration(b, "fieldA5", a5)


        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(a4)
        bc.declared_types.element_added(a5)

        bc.declared_types.element_added(b)

        bc.declared_fields.element_added(fieldRefBToA1)
        bc.declared_fields.element_added(fieldRefBToA2)
        bc.declared_fields.element_added(fieldRefBToA4)
        bc.declared_fields.element_added(fieldRefBToA5)

        db.ensemble_elements.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a1)),
                (ensembleA, SourceElement(a2)),
                (ensembleA, SourceElement(a4)),
                (ensembleA, SourceElement(a5)),
                (ensembleA1, SourceElement(a1)),
                (ensembleA2, SourceElement(a2)),
                (ensembleA3, SourceElement(a4)),
                (ensembleA3, SourceElement(a5)),
                (ensembleA4, SourceElement(a4)),
                (ensembleA5, SourceElement(a5)),
                (ensembleB, SourceElement(b)),
                (ensembleB, SourceElement(fieldRefBToA1)),
                (ensembleB, SourceElement(fieldRefBToA2)),
                (ensembleB, SourceElement(fieldRefBToA4)),
                (ensembleB, SourceElement(fieldRefBToA5))
            )
        )


        Assert.assertEquals(
            db.ensemble_dependencies.asList.sorted,
            List(
                (ensembleB, ensembleA, SourceElement(fieldRefBToA1), SourceElement(a1), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElement(fieldRefBToA2), SourceElement(a2), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElement(fieldRefBToA4), SourceElement(a4), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElement(fieldRefBToA5), SourceElement(a5), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA1, SourceElement(fieldRefBToA1), SourceElement(a1), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA2, SourceElement(fieldRefBToA2), SourceElement(a2), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA3, SourceElement(fieldRefBToA4), SourceElement(a4), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA3, SourceElement(fieldRefBToA5), SourceElement(a5), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA4, SourceElement(fieldRefBToA4), SourceElement(a4), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA5, SourceElement(fieldRefBToA5), SourceElement(a5), FieldTypeKind.asVespucciString)
            )
        )

        db.slice_constraints.asList should not be (Nil)

        db.violations.asList should be(Nil)
    }

    @Test
    def testLocalOutgoingViolatesForAllTargetChildren() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2, ensembleA3)

        val ensembleB = Ensemble("B", "class_with_members('test','B')")

        val ensembleC = Ensemble("C", "class_with_members('test','C')")

        val ensembles = Set(ensembleA, ensembleB, ensembleC)


        db.setRepository(Repository(ensembles))

        val constraint = OutgoingConstraint(FieldTypeKind.asVespucciString, ensembleB, ensembleC)
        val contextName = "test"
        db.addSlice(Concern(ensembles, Set(constraint), contextName))

        val b = ObjectType("test/B")

        val c = ObjectType("test/C")

        val a1 = ObjectType("test/inner/A1")
        val a2 = ObjectType("test/inner/A2")

        val a4 = ObjectType("test/inner/inner/A4")
        val a5 = ObjectType("test/inner/inner/A5")


        val fieldRefBToA1 = FieldDeclaration(b, "fieldA1", a1)
        val fieldRefBToA2 = FieldDeclaration(b, "fieldA2", a2)
        val fieldRefBToA4 = FieldDeclaration(b, "fieldA4", a4)
        val fieldRefBToA5 = FieldDeclaration(b, "fieldA5", a5)

        val fieldRefBToC = FieldDeclaration(b, "fieldC", c)

        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(a4)
        bc.declared_types.element_added(a5)

        bc.declared_types.element_added(b)

        bc.declared_types.element_added(c)

        bc.declared_fields.element_added(fieldRefBToA1)
        bc.declared_fields.element_added(fieldRefBToA2)
        bc.declared_fields.element_added(fieldRefBToA4)
        bc.declared_fields.element_added(fieldRefBToA5)

        bc.declared_fields.element_added(fieldRefBToC)

        Assert.assertEquals(
            db.ensemble_elements.asList.sorted,
            List(
                (ensembleA, SourceElement(a1)),
                (ensembleA, SourceElement(a2)),
                (ensembleA, SourceElement(a4)),
                (ensembleA, SourceElement(a5)),
                (ensembleA1, SourceElement(a1)),
                (ensembleA2, SourceElement(a2)),
                (ensembleA3, SourceElement(a4)),
                (ensembleA3, SourceElement(a5)),
                (ensembleA4, SourceElement(a4)),
                (ensembleA5, SourceElement(a5)),
                (ensembleB, SourceElement(b)),
                (ensembleB, SourceElement(fieldRefBToA1)),
                (ensembleB, SourceElement(fieldRefBToA2)),
                (ensembleB, SourceElement(fieldRefBToA4)),
                (ensembleB, SourceElement(fieldRefBToA5)),
                (ensembleB, SourceElement(fieldRefBToC)),
                (ensembleC, SourceElement(c))
            )
        )


        Assert.assertEquals(
            db.ensemble_dependencies.asList.sorted,
            List(
                (ensembleB, ensembleA, SourceElement(fieldRefBToA1), SourceElement(a1), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElement(fieldRefBToA2), SourceElement(a2), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElement(fieldRefBToA4), SourceElement(a4), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElement(fieldRefBToA5), SourceElement(a5), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA1, SourceElement(fieldRefBToA1), SourceElement(a1), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA2, SourceElement(fieldRefBToA2), SourceElement(a2), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA3, SourceElement(fieldRefBToA4), SourceElement(a4), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA3, SourceElement(fieldRefBToA5), SourceElement(a5), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA4, SourceElement(fieldRefBToA4), SourceElement(a4), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA5, SourceElement(fieldRefBToA5), SourceElement(a5), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleC, SourceElement(fieldRefBToC), SourceElement(c), FieldTypeKind.asVespucciString)
            )
        )

        db.slice_constraints.asList should not be (Nil)

        Assert.assertEquals(db.violations.asList.sorted,
            List(
                Violation(
                    constraint,
                    ensembleB, ensembleA,
                    SourceElement(fieldRefBToA1), SourceElement(a1),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleB, ensembleA,
                    SourceElement(fieldRefBToA2), SourceElement(a2),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleB,ensembleA,
                    SourceElement(fieldRefBToA4), SourceElement(a4),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleB, ensembleA,
                    SourceElement(fieldRefBToA5), SourceElement(a5),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleB, ensembleA1,
                    SourceElement(fieldRefBToA1), SourceElement(a1),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleB, ensembleA2,
                    SourceElement(fieldRefBToA2), SourceElement(a2),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleB, ensembleA3,
                    SourceElement(fieldRefBToA4), SourceElement(a4),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleB, ensembleA3,
                    SourceElement(fieldRefBToA5), SourceElement(a5),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleB, ensembleA4,
                    SourceElement(fieldRefBToA4), SourceElement(a4),
                    FieldTypeKind.asVespucciString,
                    contextName
                ),
                Violation(
                    constraint,
                    ensembleB, ensembleA5,
                    SourceElement(fieldRefBToA5), SourceElement(a5),
                    FieldTypeKind.asVespucciString,
                    contextName
                )
            )
        )
    }
}


