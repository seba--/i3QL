package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElement
import sae.collections.{Conversions, QueryResult}
import de.tud.cs.st.bat.{VoidType, ObjectType}
import sae.bytecode.model.dependencies.{`extends`}
import sae.bytecode.model.instructions.{putfield, push, invokespecial}
import sae.bytecode.{BytecodeDatabase}
import de.tud.cs.st.vespucci.interfaces.{IViolation}
import org.junit.{Ignore, Test}
import sae.bytecode.model.{FieldReference, FieldDeclaration, MethodReference, MethodDeclaration}

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:22
 *
 */
class TestUnissonDatabaseKinds
        extends ShouldMatchers
{

    import UnissonOrdering._


    @Test
    def testNotAllowedAllViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembles = Set(ensembleA, ensembleB)


        val c = NotAllowedConstraint("all", ensembleA, ensembleB)

        val constraints = Set(
            c
        )

        val global = Repository(ensembles)
        val model = Concern(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val obj = ObjectType("java/lang/Object")
        val superConst = MethodReference(obj, "<init>", Nil, VoidType())
        val a = ObjectType("test/A")
        val initA = MethodDeclaration(a, "<init>", Nil, VoidType())
        val b = ObjectType("test/B")

        val field = FieldDeclaration(a, "myB", b)

        bc.declared_types.element_added(a)
        bc.`extends`.element_added(`extends`(a, obj))


        bc.declared_methods.element_added(initA)
        bc.instructions.element_added(invokespecial(initA, 1, superConst))
        bc.instructions.element_added(push(initA, 3, null, obj))
        bc.instructions.element_added(putfield(initA, 4, FieldReference(a, "myB", b)))

        bc.declared_fields.element_added(field)

        bc.declared_types.element_added(b)

        result.asList.sorted should be(
            List(
                Violation(
                    c,
                    ensembleA,
                    ensembleB,
                    SourceElement(field),
                    SourceElement(b),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalIncomingAllViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = IncomingConstraint("all", ensembleB, ensembleA)
        val constraints = Set(
            constraint
        )

        val global = Repository(ensembles)
        val model = Concern(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = FieldDeclaration(b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration(c, "fieldInC", a)

        bc.declared_types.element_added(a)


        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(fieldRefBToA)

        bc.declared_types.element_added(c)
        bc.declared_fields.element_added(fieldRefCToA)

        result.asList should be(
            List(
                Violation(
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalIncomingMultipleSubsumedKinds() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembleD = Ensemble("D", "class_with_members('test','D')")

        val constraintBToA = IncomingConstraint("all", ensembleB, ensembleA)
        val constraintDToA = IncomingConstraint("field_type", ensembleD, ensembleA)

        val global = Repository(Set(ensembleA, ensembleB, ensembleC, ensembleD))
        val model = Concern(Set(ensembleA, ensembleB, ensembleC, ensembleD), Set(constraintBToA, constraintDToA), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")

        val fieldRefBToA = FieldDeclaration(b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration(c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration(d, "fieldInD", a)

        bc.declared_types.element_added(a)

        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(fieldRefBToA)

        bc.declared_types.element_added(c)
        bc.declared_fields.element_added(fieldRefCToA)

        bc.declared_types.element_added(d)
        bc.declared_fields.element_added(fieldRefDToA)


        result.asList.sorted should be(
            List(
                Violation(
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    "field_type",
                    "test"
                ),
                Violation(
                    constraintDToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Ignore
    @Test
    def testLocalIncomingMultipleDifferentKinds() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembleD = Ensemble("D", "class_with_members('test','D')")

        val constraintBToA = IncomingConstraint("subtype", ensembleB, ensembleA)
        val constraintDToA = IncomingConstraint("signature", ensembleD, ensembleA)

        val global = Repository(Set(ensembleA, ensembleB, ensembleC, ensembleD))
        val modelA = Concern(Set(ensembleA, ensembleB, ensembleC), Set(constraintBToA), "contextBToA")
        val modelB = Concern(Set(ensembleA, ensembleD, ensembleC), Set(constraintDToA), "contextDToA")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(modelA)
        db.addSlice(modelB)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")

        val fieldRefBToA = FieldDeclaration(b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration(c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration(d, "fieldInD", a)

        bc.declared_types.element_added(a)

        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(fieldRefBToA)

        bc.declared_types.element_added(c)
        bc.declared_fields.element_added(fieldRefCToA)

        bc.declared_types.element_added(d)
        bc.declared_fields.element_added(fieldRefDToA)


        result.asList.sorted should be(
            List(
                Violation(
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    "field_type",
                    "contextBToA"
                ),
                Violation(
                    constraintDToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    "field_type",
                    "contextDToA"
                )
            )
        )

    }


    @Test
    def testGlobalIncomingAllViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = GlobalIncomingConstraint("all", ensembleB, ensembleA)
        val constraints = Set(constraint)

        val global = Repository(ensembles)
        val model = Concern(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = FieldDeclaration(b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration(c, "fieldInC", a)

        bc.declared_types.element_added(a)


        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(fieldRefBToA)

        bc.declared_types.element_added(c)
        bc.declared_fields.element_added(fieldRefCToA)

        result.asList should be(
            List(
                Violation(
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testGlobalIncomingMultipleSubsumedKinds() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembleD = Ensemble("D", "class_with_members('test','D')")

        val constraintBToA = GlobalIncomingConstraint("all", ensembleB, ensembleA)
        val constraintDToA = GlobalIncomingConstraint("field_type", ensembleD, ensembleA)

        val global = Repository(Set(ensembleA, ensembleB, ensembleC, ensembleD))
        val model = Concern(Set(ensembleA, ensembleB, ensembleD), Set(constraintBToA, constraintDToA), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")

        val fieldRefBToA = FieldDeclaration(b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration(c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration(d, "fieldInD", a)

        bc.declared_types.element_added(a)

        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(fieldRefBToA)

        bc.declared_types.element_added(c)
        bc.declared_fields.element_added(fieldRefCToA)

        bc.declared_types.element_added(d)
        bc.declared_fields.element_added(fieldRefDToA)


        result.asList.sorted should be(
            List(
                Violation(
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    "field_type",
                    "test"
                ),
                Violation(
                    constraintDToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalOutgoingAllViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint("all", ensembleA, ensembleB)

        val global = Repository(ensembles)

        val model = Concern(ensembles, Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = FieldDeclaration(a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration(a, "fieldToC", c)

        bc.declared_types.element_added(a)
        bc.declared_fields.element_added(fieldRefAToB)
        bc.declared_fields.element_added(fieldRefAToC)

        bc.declared_types.element_added(b)
        bc.declared_types.element_added(c)


        result.asList should be(
            List(
                Violation(
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalOutgoingViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint("all", ensembleA, ensembleB)

        val global = Repository(ensembles)

        val model = Concern(ensembles, Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = FieldDeclaration(a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration(a, "fieldToC", c)

        bc.declared_types.element_added(a)
        bc.declared_fields.element_added(fieldRefAToB)
        bc.declared_fields.element_added(fieldRefAToC)

        bc.declared_types.element_added(b)
        bc.declared_types.element_added(c)


        result.asList should be(
            List(
                Violation(
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testGlobalOutgoingAllViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = GlobalOutgoingConstraint("all", ensembleA, ensembleB)
        val constraints = Set(constraint)

        val global = Repository(ensembles)
        val model = Concern(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = FieldDeclaration(a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration(a, "fieldToC", c)

        bc.declared_types.element_added(a)
        bc.declared_fields.element_added(fieldRefAToB)
        bc.declared_fields.element_added(fieldRefAToC)

        bc.declared_types.element_added(b)
        bc.declared_types.element_added(c)


        result.asList should be(
            List(
                Violation(
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    "field_type",
                    "test"
                )
            )
        )

    }
}