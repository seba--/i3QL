package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElement
import org.junit.{Ignore, Test}
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure.{MethodDeclaration, FieldDeclaration, MethodReference, InheritanceRelation}
import sae.bytecode.instructions.{PUTFIELD, INVOKESPECIAL}
import de.tud.cs.st.bat._
import resolved.{MethodDescriptor, VoidType, ObjectType}
import UnissonOrdering._

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




    @Test
    def testNotAllowedAllViolation() {
        val bc = BATDatabaseFactory.create()
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

        val result = sae.relationToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val obj = ObjectType("java/lang/Object")
        val superConst = MethodReference(obj, "<init>", Nil, VoidType)
        val a = ObjectType("test/A")
        val initA = MethodDeclaration(a, "<init>", VoidType, Nil)
        val b = ObjectType("test/B")

        val field = FieldDeclaration(a, "myB", b)

        bc.typeDeclarations.element_added(a)
        bc.classInheritance.element_added(InheritanceRelation(a, obj))


        bc.methodDeclarations.element_added(initA)
        bc.instructions.element_added (INVOKESPECIAL (initA, resolved.INVOKESPECIAL (obj, "<init>", MethodDescriptor (Nil, VoidType)), 1, 1))
        //bc.instructions.element_added (PUSH (initA, null, obj, 3, 2))
        bc.instructions.element_added (PUTFIELD (initA, resolved.PUTFIELD (a, "myB", b), 4, 3))


        bc.fieldDeclarations.element_added(field)

        bc.typeDeclarations.element_added(b)

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
        val bc = BATDatabaseFactory.create()
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

        val result = sae.relationToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = FieldDeclaration(b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration(c, "fieldInC", a)

        bc.typeDeclarations.element_added(a)


        bc.typeDeclarations.element_added(b)
        bc.fieldDeclarations.element_added(fieldRefBToA)

        bc.typeDeclarations.element_added(c)
        bc.fieldDeclarations.element_added(fieldRefCToA)

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
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembleD = Ensemble("D", "class_with_members('test','D')")

        val constraintBToA = IncomingConstraint("all", ensembleB, ensembleA)
        val constraintDToA = IncomingConstraint("field_type", ensembleD, ensembleA)

        val global = Repository(Set(ensembleA, ensembleB, ensembleC, ensembleD))
        val model = Concern(Set(ensembleA, ensembleB, ensembleC, ensembleD), Set(constraintBToA, constraintDToA), "test")

        val result = sae.relationToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")

        val fieldRefBToA = FieldDeclaration(b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration(c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration(d, "fieldInD", a)

        bc.typeDeclarations.element_added(a)

        bc.typeDeclarations.element_added(b)
        bc.fieldDeclarations.element_added(fieldRefBToA)

        bc.typeDeclarations.element_added(c)
        bc.fieldDeclarations.element_added(fieldRefCToA)

        bc.typeDeclarations.element_added(d)
        bc.fieldDeclarations.element_added(fieldRefDToA)


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
        val bc = BATDatabaseFactory.create()
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

        val result = sae.relationToResult(db.violations)

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

        bc.typeDeclarations.element_added(a)

        bc.typeDeclarations.element_added(b)
        bc.fieldDeclarations.element_added(fieldRefBToA)

        bc.typeDeclarations.element_added(c)
        bc.fieldDeclarations.element_added(fieldRefCToA)

        bc.typeDeclarations.element_added(d)
        bc.fieldDeclarations.element_added(fieldRefDToA)


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
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = GlobalIncomingConstraint("all", ensembleB, ensembleA)
        val constraints = Set(constraint)

        val global = Repository(ensembles)
        val model = Concern(ensembles, constraints, "test")

        val result = sae.relationToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = FieldDeclaration(b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration(c, "fieldInC", a)

        bc.typeDeclarations.element_added(a)


        bc.typeDeclarations.element_added(b)
        bc.fieldDeclarations.element_added(fieldRefBToA)

        bc.typeDeclarations.element_added(c)
        bc.fieldDeclarations.element_added(fieldRefCToA)

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
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembleD = Ensemble("D", "class_with_members('test','D')")

        val constraintBToA = GlobalIncomingConstraint("all", ensembleB, ensembleA)
        val constraintDToA = GlobalIncomingConstraint("field_type", ensembleD, ensembleA)

        val global = Repository(Set(ensembleA, ensembleB, ensembleC, ensembleD))
        val model = Concern(Set(ensembleA, ensembleB, ensembleD), Set(constraintBToA, constraintDToA), "test")

        val result =sae.relationToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")

        val fieldRefBToA = FieldDeclaration(b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration(c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration(d, "fieldInD", a)

        bc.typeDeclarations.element_added(a)

        bc.typeDeclarations.element_added(b)
        bc.fieldDeclarations.element_added(fieldRefBToA)

        bc.typeDeclarations.element_added(c)
        bc.fieldDeclarations.element_added(fieldRefCToA)

        bc.typeDeclarations.element_added(d)
        bc.fieldDeclarations.element_added(fieldRefDToA)


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
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint("all", ensembleA, ensembleB)

        val global = Repository(ensembles)

        val model = Concern(ensembles, Set(constraint), "test")

        val result = sae.relationToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = FieldDeclaration(a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration(a, "fieldToC", c)

        bc.typeDeclarations.element_added(a)
        bc.fieldDeclarations.element_added(fieldRefAToB)
        bc.fieldDeclarations.element_added(fieldRefAToC)

        bc.typeDeclarations.element_added(b)
        bc.typeDeclarations.element_added(c)


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
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint("all", ensembleA, ensembleB)

        val global = Repository(ensembles)

        val model = Concern(ensembles, Set(constraint), "test")

        val result = sae.relationToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = FieldDeclaration(a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration(a, "fieldToC", c)

        bc.typeDeclarations.element_added(a)
        bc.fieldDeclarations.element_added(fieldRefAToB)
        bc.fieldDeclarations.element_added(fieldRefAToC)

        bc.typeDeclarations.element_added(b)
        bc.typeDeclarations.element_added(c)


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
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = GlobalOutgoingConstraint("all", ensembleA, ensembleB)
        val constraints = Set(constraint)

        val global = Repository(ensembles)
        val model = Concern(ensembles, constraints, "test")

        val result = sae.relationToResult(db.violations)

        db.addSlice(model)
        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = FieldDeclaration(a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration(a, "fieldToC", c)

        bc.typeDeclarations.element_added(a)
        bc.fieldDeclarations.element_added(fieldRefAToB)
        bc.fieldDeclarations.element_added(fieldRefAToC)

        bc.typeDeclarations.element_added(b)
        bc.typeDeclarations.element_added(c)


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