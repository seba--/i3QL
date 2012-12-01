package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElement
import sae.collections.{Conversions, QueryResult}
import org.junit.Test
import de.tud.cs.st.bat.resolved.ObjectType
import sae.bytecode.model.dependencies.{`extends`}
import sae.bytecode.model.instructions.{putfield, push, invokespecial}
import sae.bytecode.{BytecodeDatabase}
import de.tud.cs.st.vespucci.interfaces.{IViolation}

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:22
 *
 */
class TestUnissonDatabaseViolations
    extends ShouldMatchers
{


    @Test
    def testNotAllowedViolation() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembles = Set (ensembleA, ensembleB)


        val c = NotAllowedConstraint ("field_type", ensembleA, ensembleB)

        val constraints = Set (
            c
        )

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val obj = ObjectType ("java/lang/Object")
        val superConst = MethodReference (obj, "<init>", Nil, VoidType ())
        val a = ObjectType ("test/A")
        val initA = MethodDeclaration (a, "<init>", Nil, VoidType ())
        val b = ObjectType ("test/B")
        val field = FieldDeclaration (a, "myB", b)


        bc.typeDeclarations.element_added (a)
        bc.`extends`.element_added (`extends` (a, obj))


        bc.methodDeclarations.element_added (initA)
        bc.instructions.element_added (invokespecial (initA, 1, superConst))
        bc.instructions.element_added (push (initA, 3, null, obj))
        bc.instructions.element_added (putfield (initA, 4, FieldReference (a, "myB", b)))

        bc.fieldDeclarations.element_added (field)

        bc.typeDeclarations.element_added (b)

        result.asList.sorted should be (
            List (
                Violation (
                    c,
                    ensembleA,
                    ensembleB,
                    SourceElement (field),
                    SourceElement (b),
                    "field_type",
                    "test"
                )
            )
        )

    }


    @Test
    def testNotAllowedNoViolation() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembles = Set (ensembleA, ensembleB)


        val c = NotAllowedConstraint ("all", ensembleA, ensembleB)

        val constraints = Set (
            c
        )

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)


        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val fieldRef = FieldDeclaration (a, "myA", a)
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRef)

        result.asList should be (Nil)

    }


    @Test
    def testLocalIncomingViolation() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = IncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraints = Set (
            constraint
        )

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)

        bc.typeDeclarations.element_added (a)


        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)

        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalIncomingMultipleContexts() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembleD = Ensemble ("D", "class_with_members('test','D')")

        val constraintBToA = IncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraintDToA = IncomingConstraint ("field_type", ensembleD, ensembleA)

        val global = Repository (Set (ensembleA, ensembleB, ensembleC, ensembleD))
        val modelA = Concern (Set (ensembleA, ensembleB, ensembleC), Set (constraintBToA), "contextBToA")
        val modelB = Concern (Set (ensembleA, ensembleD, ensembleC), Set (constraintDToA), "contextDToA")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (modelA)
        db.addSlice (modelB)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        val d = ObjectType ("test/D")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration (d, "fieldInD", a)

        bc.typeDeclarations.element_added (a)

        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)

        bc.typeDeclarations.element_added (d)
        bc.fieldDeclarations.element_added (fieldRefDToA)


        result.asList.sorted should be (
            List (
                Violation (
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "contextBToA"
                ),
                Violation (
                    constraintDToA,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "contextDToA"
                )
            )
        )

    }

    @Test
    def testLocalDifferentIncomingViolations() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraintBToA = IncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraintAToC = IncomingConstraint ("field_type", ensembleA, ensembleC)

        val constraints = Set (constraintBToA, constraintAToC)

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldBToA", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldCToA", a)
        val fieldRefAToC = FieldDeclaration (a, "fieldAToC", c)
        val fieldRefBToC = FieldDeclaration (b, "fieldAToC", c)


        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToC)

        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.fieldDeclarations.element_added (fieldRefBToC)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)

        result.asList.sorted should be (
            List (
                Violation (
                    constraintAToC,
                    ensembleB,
                    ensembleC,
                    SourceElement (fieldRefBToC),
                    SourceElement (c),
                    "field_type",
                    "test"
                ),
                Violation (
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalIncomingViolationWithExcessDependencies() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = IncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraints = Set (
            constraint
        )

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)

        val fieldRefAToA = FieldDeclaration (a, "selfFieldInA", a)

        val fieldRefAToB = FieldDeclaration (a, "excessFieldInA", b)

        val fieldRefBToC = FieldDeclaration (b, "excessFieldInB", c)

        val fieldRefCToB = FieldDeclaration (c, "excessFieldInC", b)

        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToA)
        bc.fieldDeclarations.element_added (fieldRefAToB)

        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.fieldDeclarations.element_added (fieldRefBToC)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)
        bc.fieldDeclarations.element_added (fieldRefCToB)

        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalIncomingNoViolation() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraints = Set (
            IncomingConstraint ("field_type", ensembleB, ensembleA),
            IncomingConstraint ("field_type", ensembleC, ensembleA)
        )

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)

        bc.typeDeclarations.element_added (a)


        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)

        result.asList should be (Nil)

    }

    @Test
    def testLocalIncomingNoViolationFromGlobal() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")

        val global = Repository (
            Set (ensembleA, ensembleB, ensembleC)
        )

        val model = Concern (
            Set (ensembleA, ensembleB),
            Set (IncomingConstraint ("field_type", ensembleB, ensembleA)),
            "test"
        )

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)

        bc.typeDeclarations.element_added (a)


        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)

        result.asList should be (Nil)

    }

    @Test
    def testGlobalIncomingViolation() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = GlobalIncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraints = Set (constraint)

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)

        bc.typeDeclarations.element_added (a)


        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)

        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }


    @Test
    def testGlobalDifferentIncomingViolations() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraintBToA = GlobalIncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraintAToC = GlobalIncomingConstraint ("field_type", ensembleA, ensembleC)

        val constraints = Set (constraintBToA, constraintAToC)

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldBToA", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldCToA", a)
        val fieldRefAToC = FieldDeclaration (a, "fieldAToC", c)
        val fieldRefBToC = FieldDeclaration (b, "fieldAToC", c)


        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToC)

        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.fieldDeclarations.element_added (fieldRefBToC)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)

        result.asList.sorted should be (
            List (
                Violation (
                    constraintAToC,
                    ensembleB,
                    ensembleC,
                    SourceElement (fieldRefBToC),
                    SourceElement (c),
                    "field_type",
                    "test"
                ),
                Violation (
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testGlobalIncomingViolationWithoutLocalEnsembleMention() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = GlobalIncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraints = Set (constraint)

        val global = Repository (ensembles)
        val model = Concern (Set (ensembleA, ensembleB), constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)

        bc.typeDeclarations.element_added (a)


        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)

        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }


    @Test
    def testGlobalIncomingViolationWithExcessDependencies() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = GlobalIncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraints = Set (
            constraint
        )

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)

        val fieldRefAToA = FieldDeclaration (a, "selfFieldInA", a)

        val fieldRefAToB = FieldDeclaration (a, "excessFieldInA", b)

        val fieldRefBToC = FieldDeclaration (b, "excessFieldInB", c)

        val fieldRefCToB = FieldDeclaration (c, "excessFieldInC", b)

        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToA)
        bc.fieldDeclarations.element_added (fieldRefAToB)

        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.fieldDeclarations.element_added (fieldRefBToC)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)
        bc.fieldDeclarations.element_added (fieldRefCToB)

        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }


    @Test
    def testGlobalIncomingMultipleContexts() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")

        val constraintBToA = GlobalIncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraintCToA = GlobalIncomingConstraint ("field_type", ensembleC, ensembleA)

        val global = Repository (Set (ensembleA, ensembleB, ensembleC))
        val modelA = Concern (Set (ensembleA, ensembleB), Set (constraintBToA), "contextBToA")
        val modelB = Concern (Set (ensembleA, ensembleC), Set (constraintCToA), "contextCToA")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (modelA)
        db.addSlice (modelB)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)

        bc.typeDeclarations.element_added (a)

        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)


        result.asList.sorted should be (
            List (
                Violation (
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "contextBToA"
                ),
                Violation (
                    constraintCToA,
                    ensembleB,
                    ensembleA,
                    SourceElement (fieldRefBToA),
                    SourceElement (a),
                    "field_type",
                    "contextCToA"
                )
            )
        )

    }


    @Test
    def testMixofGlobalLocalIncoming() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembleD = Ensemble ("D", "class_with_members('test','D')")
        val ensembleE = Ensemble ("E", "class_with_members('test','E')")

        val constraintBToA = GlobalIncomingConstraint ("field_type", ensembleB, ensembleA)
        val constraintCToA = IncomingConstraint ("field_type", ensembleC, ensembleA)

        val global = Repository (Set (ensembleA, ensembleB, ensembleC, ensembleD, ensembleE))
        val model = Concern (Set (ensembleA, ensembleB, ensembleC, ensembleD), Set (constraintBToA, constraintCToA), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        val d = ObjectType ("test/D")
        val e = ObjectType ("test/E")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration (d, "fieldInD", a)
        val fieldRefEToA = FieldDeclaration (e, "fieldInE", a)

        bc.typeDeclarations.element_added (a)

        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)

        bc.typeDeclarations.element_added (d)
        bc.fieldDeclarations.element_added (fieldRefDToA)

        bc.typeDeclarations.element_added (e)
        bc.fieldDeclarations.element_added (fieldRefEToA)


        result.asList.sorted should be (
            List (
                Violation (
                    constraintBToA,
                    ensembleD,
                    ensembleA,
                    SourceElement (fieldRefDToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                ),
                Violation (
                    constraintBToA,
                    ensembleE,
                    ensembleA,
                    SourceElement (fieldRefEToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                ),
                Violation (
                    constraintCToA,
                    ensembleD,
                    ensembleA,
                    SourceElement (fieldRefDToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }


    @Test
    def testLocalOutgoingViolation() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint ("field_type", ensembleA, ensembleB)

        val global = Repository (ensembles)

        val model = Concern (ensembles, Set (constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)

        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)

        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)


        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalOutgoingMultipleContexts() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembleD = Ensemble ("D", "class_with_members('test','D')")

        val constraintAToB = OutgoingConstraint ("field_type", ensembleA, ensembleB)
        val constraintAToD = OutgoingConstraint ("field_type", ensembleA, ensembleD)

        val global = Repository (Set (ensembleA, ensembleB, ensembleC, ensembleD))
        val modelA = Concern (Set (ensembleA, ensembleB, ensembleC), Set (constraintAToB), "contextAToB")
        val modelB = Concern (Set (ensembleA, ensembleC, ensembleD), Set (constraintAToD), "contextAToD")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (modelA)
        db.addSlice (modelB)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        val d = ObjectType ("test/D")

        val fieldRefAToB = FieldDeclaration (a, "fieldAToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldAToC", c)
        val fieldRefAToD = FieldDeclaration (a, "fieldAToD", d)

        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)
        bc.fieldDeclarations.element_added (fieldRefAToD)

        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)
        bc.typeDeclarations.element_added (d)

        result.asList.sorted should be (
            List (
                Violation (
                    constraintAToB,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "contextAToB"
                ),
                Violation (
                    constraintAToD,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "contextAToD"
                )
            )
        )

    }

    @Test
    def testLocalDifferentOutgoingViolations() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraintAToB = OutgoingConstraint ("field_type", ensembleA, ensembleB)
        val constraintBToC = OutgoingConstraint ("field_type", ensembleB, ensembleC)

        val constraints = Set (constraintAToB, constraintBToC)

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)

        val fieldRefBToA = FieldDeclaration (b, "fieldBToA", a)
        val fieldRefBToC = FieldDeclaration (b, "fieldBToC", c)
        val fieldRefCToA = FieldDeclaration (c, "fieldCToA", a)
        val fieldRefCToB = FieldDeclaration (c, "fieldCToB", b)

        val fieldRefAToA = FieldDeclaration (a, "selfFieldInA", a)


        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToA)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)


        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.fieldDeclarations.element_added (fieldRefBToC)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)
        bc.fieldDeclarations.element_added (fieldRefCToB)

        result.asList.sorted should be (
            List (
                Violation (
                    constraintAToB,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "test"
                ),
                Violation (
                    constraintBToC,
                    ensembleB,
                    ensembleA,
                    SourceElement (fieldRefBToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalOutgoingViolationWithExcessDependencies() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint ("field_type", ensembleA, ensembleB)

        val global = Repository (ensembles)

        val model = Concern (ensembles, Set (constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)

        val fieldRefBToA = FieldDeclaration (b, "fieldBToA", a)
        val fieldRefBToC = FieldDeclaration (b, "fieldBToC", c)
        val fieldRefCToA = FieldDeclaration (c, "fieldCToA", a)
        val fieldRefCToB = FieldDeclaration (c, "fieldCToB", b)

        val fieldRefAToA = FieldDeclaration (a, "selfFieldInA", a)


        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToA)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)


        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.fieldDeclarations.element_added (fieldRefBToC)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)
        bc.fieldDeclarations.element_added (fieldRefCToB)


        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testLocalOutgoingNoViolation() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraintToB = OutgoingConstraint ("field_type", ensembleA, ensembleB)
        val constraintToC = OutgoingConstraint ("field_type", ensembleA, ensembleC)

        val global = Repository (ensembles)

        val model = Concern (ensembles, Set (constraintToB, constraintToC), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)

        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)

        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)


        result.asList should be (Nil)
    }


    @Test
    def testLocalOutgoingNoViolationToGlobal() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint ("field_type", ensembleA, ensembleB)

        val global = Repository (ensembles)

        val model = Concern (Set (ensembleA, ensembleB), Set (constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)

        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)

        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)


        result.asList should be (Nil)

    }

    @Test
    def testGlobalOutgoingViolation() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = GlobalOutgoingConstraint ("field_type", ensembleA, ensembleB)
        val constraints = Set (constraint)

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)

        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)

        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)


        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "test"
                )
            )
        )

    }


    @Test
    def testGlobalDifferentOutgoingViolations() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraintAToB = GlobalOutgoingConstraint ("field_type", ensembleA, ensembleB)
        val constraintBToC = GlobalOutgoingConstraint ("field_type", ensembleB, ensembleC)

        val constraints = Set (constraintAToB, constraintBToC)

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)

        val fieldRefBToA = FieldDeclaration (b, "fieldBToA", a)
        val fieldRefBToC = FieldDeclaration (b, "fieldBToC", c)
        val fieldRefCToA = FieldDeclaration (c, "fieldCToA", a)
        val fieldRefCToB = FieldDeclaration (c, "fieldCToB", b)

        val fieldRefAToA = FieldDeclaration (a, "selfFieldInA", a)


        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToA)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)


        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.fieldDeclarations.element_added (fieldRefBToC)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)
        bc.fieldDeclarations.element_added (fieldRefCToB)

        result.asList.sorted should be (
            List (
                Violation (
                    constraintAToB,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "test"
                ),
                Violation (
                    constraintBToC,
                    ensembleB,
                    ensembleA,
                    SourceElement (fieldRefBToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

    }

    @Test
    def testGlobalOutgoingViolationWithoutLocalEnsembleMention() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = GlobalOutgoingConstraint ("field_type", ensembleA, ensembleB)
        val constraints = Set (constraint)

        val global = Repository (ensembles)
        val model = Concern (Set (ensembleA, ensembleB), constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)

        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)

        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)

        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "test"
                )
            )
        )

    }


    @Test
    def testGlobalOutgoingViolationWithExcessDependencies() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint ("field_type", ensembleA, ensembleB)
        val constraints = Set (
            constraint
        )

        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)

        val fieldRefBToA = FieldDeclaration (b, "fieldBToA", a)
        val fieldRefBToC = FieldDeclaration (b, "fieldBToC", c)
        val fieldRefCToA = FieldDeclaration (c, "fieldCToA", a)
        val fieldRefCToB = FieldDeclaration (c, "fieldCToB", b)

        val fieldRefAToA = FieldDeclaration (a, "selfFieldInA", a)


        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToA)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)


        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.fieldDeclarations.element_added (fieldRefBToC)

        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)
        bc.fieldDeclarations.element_added (fieldRefCToB)


        result.asList should be (
            List (
                Violation (
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "test"
                )
            )
        )
    }


    @Test
    def testGlobalOutgoingMultipleContexts() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")

        val constraintAToB = GlobalOutgoingConstraint ("field_type", ensembleA, ensembleB)
        val constraintAToC = GlobalOutgoingConstraint ("field_type", ensembleA, ensembleC)

        val global = Repository (Set (ensembleA, ensembleB, ensembleC))
        val modelA = Concern (Set (ensembleA, ensembleB), Set (constraintAToB), "contextAToB")
        val modelB = Concern (Set (ensembleA, ensembleC), Set (constraintAToC), "contextAToC")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (modelA)
        db.addSlice (modelB)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")

        val fieldRefAToB = FieldDeclaration (a, "fieldAToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldAToC", c)


        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)


        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)

        result.asList.sorted should be (
            List (
                Violation (
                    constraintAToB,
                    ensembleA,
                    ensembleC,
                    SourceElement (fieldRefAToC),
                    SourceElement (c),
                    "field_type",
                    "contextAToB"
                ),
                Violation (
                    constraintAToC,
                    ensembleA,
                    ensembleB,
                    SourceElement (fieldRefAToB),
                    SourceElement (b),
                    "field_type",
                    "contextAToC"
                )
            )
        )
    }


    @Test
    def testMixofGlobalLocalOutgoing() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembleD = Ensemble ("D", "class_with_members('test','D')")
        val ensembleE = Ensemble ("E", "class_with_members('test','E')")

        val constraintAToB = GlobalOutgoingConstraint ("field_type", ensembleA, ensembleB)
        val constraintAToC = OutgoingConstraint ("field_type", ensembleA, ensembleC)

        val global = Repository (Set (ensembleA, ensembleB, ensembleC, ensembleD, ensembleE))
        val model = Concern (Set (ensembleA, ensembleB, ensembleC, ensembleD), Set (constraintAToB, constraintAToC), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult (db.violations)

        db.addSlice (model)
        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        val d = ObjectType ("test/D")
        val e = ObjectType ("test/E")

        val fieldRefAToB = FieldDeclaration (a, "fieldToB", b)
        val fieldRefAToC = FieldDeclaration (a, "fieldToC", c)
        val fieldRefAToD = FieldDeclaration (a, "fieldToD", d)
        val fieldRefAToE = FieldDeclaration (a, "fieldToE", e)

        bc.typeDeclarations.element_added (a)
        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefAToC)
        bc.fieldDeclarations.element_added (fieldRefAToD)
        bc.fieldDeclarations.element_added (fieldRefAToE)

        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)
        bc.typeDeclarations.element_added (d)
        bc.typeDeclarations.element_added (e)

        result.asList.sorted should be (
            List (
                Violation (
                    constraintAToB,
                    ensembleA,
                    ensembleD,
                    SourceElement (fieldRefAToD),
                    SourceElement (d),
                    "field_type",
                    "test"
                ),
                Violation (
                    constraintAToB,
                    ensembleA,
                    ensembleE,
                    SourceElement (fieldRefAToE),
                    SourceElement (e),
                    "field_type",
                    "test"
                ),
                Violation (
                    constraintAToC,
                    ensembleA,
                    ensembleD,
                    SourceElement (fieldRefAToD),
                    SourceElement (d),
                    "field_type",
                    "test"
                )
            )
        )

    }

}