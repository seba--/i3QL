package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElement
import sae.collections.{Conversions, QueryResult}
import de.tud.cs.st.vespucci.model.IEnsemble
import de.tud.cs.st.vespucci.interfaces.IViolation
import org.junit.Test
import sae.bytecode.BytecodeDatabase
import sae.bytecode.model.{Method, Field}
import de.tud.cs.st.bat.{VoidType, ObjectType}
import sae.bytecode.model.dependencies.{`extends`}
import sae.bytecode.model.instructions.{putfield, push, invokespecial}

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:22
 *
 */
class TestUnissonDatabase
        extends ShouldMatchers
{

    @Test
    def testEnsembleElements() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class('test','B')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB)


        val c = NotAllowedConstraint("all", ensembleA, ensembleB)

        val constraints = Set(
            c
        )


        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[(IEnsemble, SourceElement[AnyRef])] = Conversions
                .lazyViewToResult(db.global_ensemble_elements)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        bc.classfiles.element_added(a)
        bc.classfiles.element_added(b)

        implicit val sort = new Ordering[(IEnsemble, SourceElement[AnyRef])] {
            def compare(x: (IEnsemble, SourceElement[AnyRef]), y: (IEnsemble, SourceElement[AnyRef])): Int = {
                val e = x._1.getName.compare(y._1.getName)
                if (e != 0) {
                    return e
                }
                SourceElement.compare(x._2, y._2)
            }
        }

        result.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a)),
                (ensembleB, SourceElement(b))
            )
        )

    }


    @Test
    def testNotAllowedViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB)


        val c = NotAllowedConstraint("field_type", ensembleA, ensembleB)

        val constraints = Set(
            c
        )

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val obj = ObjectType("java/lang/Object")
        val superConst = Method(obj, "<init>", Nil, VoidType())
        val a = ObjectType("test/A")
        val initA = Method(a, "<init>", Nil, VoidType())
        val b = ObjectType("test/B")
        val fieldRef = Field(a, "myB", b)


        bc.classfiles.element_added(a)
        bc.`extends`.element_added(`extends`(a, obj))


        bc.classfile_methods.element_added(initA)
        bc.instructions.element_added(invokespecial(initA, 1, superConst))
        bc.instructions.element_added(push(initA, 3, null, obj))
        bc.instructions.element_added(putfield(initA, 4, fieldRef))

        bc.classfile_fields.element_added(fieldRef)

        bc.classfiles.element_added(b)

        result.asList should be(
            List(
                Violation(
                    c,
                    ensembleA,
                    ensembleB,
                    SourceElement(fieldRef),
                    SourceElement(b),
                    ""
                )
            )
        )

    }


    @Test
    def testNotAllowedNoViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB)


        val c = NotAllowedConstraint("all", ensembleA, ensembleB)

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
        val fieldRef = Field(a, "myA", a)
        bc.classfiles.element_added(a)
        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRef)

        result.asList should be(Nil)

    }




    @Test
    def testLocalIncomingViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = IncomingConstraint("field_type", ensembleB, ensembleA)
        val constraints = Set(
            constraint
        )

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = Field(b, "fieldInB", a)
        val fieldRefCToA = Field(c, "fieldInC", a)

        bc.classfiles.element_added(a)


        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)

        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)

        result.asList should be(
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

    }

    @Test
    def testLocalIncomingMultipleContexts() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembleD = Ensemble("D", "class_with_members('test','D')", Set.empty)

        val constraintA = IncomingConstraint("field_type", ensembleB, ensembleA)
        val constraintB = IncomingConstraint("field_type", ensembleD, ensembleA)

        val global = GlobalArchitectureModel(Set(ensembleA, ensembleB, ensembleC, ensembleD))
        val modelA = ArchitectureModel(Set(ensembleA, ensembleB, ensembleC), Set(constraintA), "contextA")
        val modelB = ArchitectureModel(Set(ensembleA, ensembleD, ensembleC), Set(constraintB), "contextB")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(modelA)
        db.addModel(modelB)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")

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


        result.asList should be(
            List(
                Violation(
                    constraintB,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    ""
                ),
                Violation(
                    constraintA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    ""
                )
            )
        )

    }

    @Test
    def testLocalIncomingViolationWithExcessDependencies() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = IncomingConstraint("field_type", ensembleB, ensembleA)
        val constraints = Set(
            constraint
        )

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = Field(b, "fieldInB", a)
        val fieldRefCToA = Field(c, "fieldInC", a)

        val fieldRefAToA = Field(a, "selfFieldInA", a)

        val fieldRefAToB = Field(a, "excessFieldInA", b)

        val fieldRefBToC = Field(b, "excessFieldInB", c)

        val fieldRefCToB = Field(c, "excessFieldInC", b)

        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToA)
        bc.classfile_fields.element_added(fieldRefAToB)

        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)
        bc.classfile_fields.element_added(fieldRefBToC)

        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)
        bc.classfile_fields.element_added(fieldRefCToB)

        result.asList should be(
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

    }

    @Test
    def testLocalIncomingNoViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraints = Set(
            IncomingConstraint("field_type", ensembleB, ensembleA),
            IncomingConstraint("field_type", ensembleC, ensembleA)
        )

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = Field(b, "fieldInB", a)
        val fieldRefCToA = Field(c, "fieldInC", a)

        bc.classfiles.element_added(a)


        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)

        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)

        result.asList should be(Nil)

    }

    @Test
    def testLocalIncomingNoViolationFromGlobal() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)

        val global = GlobalArchitectureModel(
            Set(ensembleA, ensembleB, ensembleC)
        )

        val model = ArchitectureModel(
            Set(ensembleA, ensembleB),
            Set(IncomingConstraint("field_type", ensembleB, ensembleA)),
            "test"
        )

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = Field(b, "fieldInB", a)
        val fieldRefCToA = Field(c, "fieldInC", a)

        bc.classfiles.element_added(a)


        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)

        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)

        result.asList should be(Nil)

    }
}