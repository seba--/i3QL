package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElement
import sae.collections.{Conversions, QueryResult}
import org.junit.Test
import sae.bytecode.model.{Method, Field}
import de.tud.cs.st.bat.{VoidType, ObjectType}
import sae.bytecode.model.dependencies.{`extends`}
import sae.bytecode.model.instructions.{putfield, push, invokespecial}
import sae.bytecode.{BytecodeDatabase}
import de.tud.cs.st.vespucci.model.{IConstraint, IEnsemble}
import de.tud.cs.st.vespucci.interfaces.{ICodeElement, IViolation}

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


    implicit def violationOrdering(implicit
                                   constraintOrdering: Ordering[IConstraint],
                                   ensembleOrdering: Ordering[IEnsemble],
                                   elementOrdering: Ordering[ICodeElement]
                                          ): Ordering[IViolation] = {
        new Ordering[IViolation]
        {
            def compare(x: IViolation, y: IViolation): Int = {
                val constraintOrder = constraintOrdering.compare(x.getConstraint, y.getConstraint)
                if (constraintOrder != 0) return constraintOrder

                val sourceEnsembleOrder = ensembleOrdering.compare(x.getSourceEnsemble, y.getSourceEnsemble)
                if (sourceEnsembleOrder != 0) return sourceEnsembleOrder

                val targetEnsembleOrder = ensembleOrdering.compare(x.getTargetEnsemble, y.getTargetEnsemble)
                if (targetEnsembleOrder != 0) return targetEnsembleOrder

                val sourceElementOrder = elementOrdering.compare(x.getSourceElement, y.getSourceElement)
                if (sourceElementOrder != 0) return sourceElementOrder

                val targetElementOrder = elementOrdering.compare(x.getTargetElement, y.getTargetElement)
                if (targetElementOrder != 0) return targetElementOrder

                0
            }
        }
    }

    implicit def constraintOrdering(implicit ensembleOrdering: Ordering[IEnsemble]): Ordering[IConstraint] = {
        new Ordering[IConstraint]
        {
            def compare(x: IConstraint, y: IConstraint): Int = {
                val sourceOrder = ensembleOrdering.compare(x.getSource, y.getSource)
                if (sourceOrder != 0) return sourceOrder

                val targetOrder = ensembleOrdering.compare(x.getTarget, y.getTarget)
                if (targetOrder != 0) return targetOrder

                x.getDependencyKind.compare(y.getDependencyKind)
            }
        }
    }

    implicit def ensembleOrdering: Ordering[IEnsemble] = new Ordering[IEnsemble] {
        def compare(x: IEnsemble, y: IEnsemble): Int =
            x.getName.compare(y.getName)
    }

    implicit def elementOrdering: Ordering[ICodeElement] = new Ordering[ICodeElement] {
        def compare(x: ICodeElement, y: ICodeElement): Int = {
            val pnCompare = x.getPackageIdentifier.compareTo(y.getPackageIdentifier)
            if (pnCompare != 0) return pnCompare
            val snCompare = x.getSimpleClassName.compareTo(y.getSimpleClassName)
            if (snCompare != 0) return snCompare
            0
        }
    }

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

        result.asList.sorted should be(
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

        val constraintBToA = IncomingConstraint("field_type", ensembleB, ensembleA)
        val constraintDToA = IncomingConstraint("field_type", ensembleD, ensembleA)

        val global = GlobalArchitectureModel(Set(ensembleA, ensembleB, ensembleC, ensembleD))
        val modelA = ArchitectureModel(Set(ensembleA, ensembleB, ensembleC), Set(constraintBToA), "contextA")
        val modelB = ArchitectureModel(Set(ensembleA, ensembleD, ensembleC), Set(constraintDToA), "contextB")

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
                    constraintDToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    ""
                ),
                Violation(
                    constraintBToA,
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
    def testLocalDifferentIncomingViolations() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraintBToA = IncomingConstraint("field_type", ensembleB, ensembleA)
        val constraintAToC = IncomingConstraint("field_type", ensembleA, ensembleC)

        val constraints = Set(constraintBToA, constraintAToC)

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = Field(b, "fieldBToA", a)
        val fieldRefCToA = Field(c, "fieldCToA", a)
        val fieldRefAToC = Field(a, "fieldAToC", c)
        val fieldRefBToC = Field(b, "fieldAToC", c)


        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToC)

        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)
        bc.classfile_fields.element_added(fieldRefBToC)

        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)

        result.asList should be(
            List(
                Violation(
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    ""
                ),
                Violation(
                    constraintAToC,
                    ensembleB,
                    ensembleC,
                    SourceElement(fieldRefBToC),
                    SourceElement(c),
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

    @Test
    def testGlobalIncomingViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = GlobalIncomingConstraint("field_type", ensembleB, ensembleA)
        val constraints = Set(constraint)

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
    def testGlobalDifferentIncomingViolations() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraintBToA = GlobalIncomingConstraint("field_type", ensembleB, ensembleA)
        val constraintAToC = GlobalIncomingConstraint("field_type", ensembleA, ensembleC)

        val constraints = Set(constraintBToA, constraintAToC)

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefBToA = Field(b, "fieldBToA", a)
        val fieldRefCToA = Field(c, "fieldCToA", a)
        val fieldRefAToC = Field(a, "fieldAToC", c)
        val fieldRefBToC = Field(b, "fieldAToC", c)


        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToC)

        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)
        bc.classfile_fields.element_added(fieldRefBToC)

        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)

        result.asList should be(
            List(
                Violation(
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    ""
                ),
                Violation(
                    constraintAToC,
                    ensembleB,
                    ensembleC,
                    SourceElement(fieldRefBToC),
                    SourceElement(c),
                    ""
                )
            )
        )

    }

    @Test
    def testGlobalIncomingViolationWithoutLocalEnsembleMention() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = GlobalIncomingConstraint("field_type", ensembleB, ensembleA)
        val constraints = Set(constraint)

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(Set(ensembleA, ensembleB), constraints, "test")

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
    def testGlobalIncomingViolationWithExcessDependencies() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = GlobalIncomingConstraint("field_type", ensembleB, ensembleA)
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
    def testGlobalIncomingMultipleContexts() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)

        val constraintBToA = GlobalIncomingConstraint("field_type", ensembleB, ensembleA)
        val constraintCToA = GlobalIncomingConstraint("field_type", ensembleC, ensembleA)

        val global = GlobalArchitectureModel(Set(ensembleA, ensembleB, ensembleC))
        val modelA = ArchitectureModel(Set(ensembleA, ensembleB), Set(constraintBToA), "contextAandB")
        val modelB = ArchitectureModel(Set(ensembleA, ensembleC), Set(constraintCToA), "contextAandC")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(modelA)
        db.addModel(modelB)
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
                    constraintBToA,
                    ensembleC,
                    ensembleA,
                    SourceElement(fieldRefCToA),
                    SourceElement(a),
                    ""
                ),
                Violation(
                    constraintCToA,
                    ensembleB,
                    ensembleA,
                    SourceElement(fieldRefBToA),
                    SourceElement(a),
                    ""
                )
            )
        )

    }


    @Test
    def testMixofGlobalLocalIncoming() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembleD = Ensemble("D", "class_with_members('test','D')", Set.empty)
        val ensembleE = Ensemble("E", "class_with_members('test','E')", Set.empty)

        val constraintBToA = GlobalIncomingConstraint("field_type", ensembleB, ensembleA)
        val constraintCToA = IncomingConstraint("field_type", ensembleC, ensembleA)

        val global = GlobalArchitectureModel(Set(ensembleA, ensembleB, ensembleC, ensembleD, ensembleE))
        val model = ArchitectureModel(Set(ensembleA, ensembleB, ensembleC, ensembleD), Set(constraintBToA, constraintCToA), "context")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")
        val e = ObjectType("test/E")

        val fieldRefBToA = Field(b, "fieldInB", a)
        val fieldRefCToA = Field(c, "fieldInC", a)
        val fieldRefDToA = Field(d, "fieldInD", a)
        val fieldRefEToA = Field(e, "fieldInE", a)

        bc.classfiles.element_added(a)

        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)

        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)

        bc.classfiles.element_added(d)
        bc.classfile_fields.element_added(fieldRefDToA)

        bc.classfiles.element_added(e)
        bc.classfile_fields.element_added(fieldRefEToA)


        result.asList should be(
            List(
                Violation(
                    constraintBToA,
                    ensembleE,
                    ensembleA,
                    SourceElement(fieldRefEToA),
                    SourceElement(a),
                    ""
                ),
                Violation(
                    constraintBToA,
                    ensembleD,
                    ensembleA,
                    SourceElement(fieldRefDToA),
                    SourceElement(a),
                    ""
                ),
                Violation(
                    constraintCToA,
                    ensembleD,
                    ensembleA,
                    SourceElement(fieldRefDToA),
                    SourceElement(a),
                    ""
                )
            )
        )

    }


    @Test
    def testLocalOutgoingViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint("field_type", ensembleA, ensembleB)

        val global = GlobalArchitectureModel(ensembles)

        val model = ArchitectureModel(ensembles, Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)

        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)

        bc.classfiles.element_added(b)
        bc.classfiles.element_added(c)


        result.asList should be(
            List(
                Violation(
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                )
            )
        )

    }

    @Test
    def testLocalOutgoingMultipleContexts() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembleD = Ensemble("D", "class_with_members('test','D')", Set.empty)

        val constraintAToB = OutgoingConstraint("field_type", ensembleA, ensembleB)
        val constraintAToD = OutgoingConstraint("field_type", ensembleA, ensembleD)

        val global = GlobalArchitectureModel(Set(ensembleA, ensembleB, ensembleC, ensembleD))
        val modelA = ArchitectureModel(Set(ensembleA, ensembleB, ensembleC), Set(constraintAToB), "contextAToB")
        val modelB = ArchitectureModel(Set(ensembleA, ensembleC, ensembleD), Set(constraintAToD), "contextAToD")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(modelA)
        db.addModel(modelB)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")

        val fieldRefAToB = Field(a, "fieldAToB", b)
        val fieldRefAToC = Field(a, "fieldAToC", c)
        val fieldRefAToD = Field(a, "fieldAToD", d)

        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)
        bc.classfile_fields.element_added(fieldRefAToD)

        bc.classfiles.element_added(b)
        bc.classfiles.element_added(c)
        bc.classfiles.element_added(d)

        result.asList should be(
            List(
                Violation(
                    constraintAToD,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                ),
                Violation(
                    constraintAToB,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                )
            )
        )

    }

    @Test
    def testLocalDifferentOutgoingViolations() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraintAToB = OutgoingConstraint("field_type", ensembleA, ensembleB)
        val constraintBToC = OutgoingConstraint("field_type", ensembleB, ensembleC)

        val constraints = Set(constraintAToB, constraintBToC)

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "context")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)

        val fieldRefBToA = Field(b, "fieldBToA", a)
        val fieldRefBToC = Field(b, "fieldBToC", c)
        val fieldRefCToA = Field(c, "fieldCToA", a)
        val fieldRefCToB = Field(c, "fieldCToB", b)

        val fieldRefAToA = Field(a, "selfFieldInA", a)


        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToA)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)


        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)
        bc.classfile_fields.element_added(fieldRefBToC)

        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)
        bc.classfile_fields.element_added(fieldRefCToB)

        result.asList should be(
            List(
                Violation(
                    constraintBToC,
                    ensembleB,
                    ensembleA,
                    SourceElement(fieldRefBToA),
                    SourceElement(a),
                    ""
                ),
                Violation(
                    constraintAToB,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                )
            )
        )

    }

    @Test
    def testLocalOutgoingViolationWithExcessDependencies() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint("field_type", ensembleA, ensembleB)

        val global = GlobalArchitectureModel(ensembles)

        val model = ArchitectureModel(ensembles, Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)

        val fieldRefBToA = Field(b, "fieldBToA", a)
        val fieldRefBToC = Field(b, "fieldBToC", c)
        val fieldRefCToA = Field(c, "fieldCToA", a)
        val fieldRefCToB = Field(c, "fieldCToB", b)

        val fieldRefAToA = Field(a, "selfFieldInA", a)


        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToA)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)


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
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                )
            )
        )

    }

    @Test
    def testLocalOutgoingNoViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraintToB = OutgoingConstraint("field_type", ensembleA, ensembleB)
        val constraintToC = OutgoingConstraint("field_type", ensembleA, ensembleC)

        val global = GlobalArchitectureModel(ensembles)

        val model = ArchitectureModel(ensembles, Set(constraintToB, constraintToC), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)

        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)

        bc.classfiles.element_added(b)
        bc.classfiles.element_added(c)


        result.asList should be(Nil)
    }


    @Test
    def testLocalOutgoingNoViolationToGlobal() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint("field_type", ensembleA, ensembleB)

        val global = GlobalArchitectureModel(ensembles)

        val model = ArchitectureModel(Set(ensembleA, ensembleB), Set(constraint), "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)

        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)

        bc.classfiles.element_added(b)
        bc.classfiles.element_added(c)


        result.asList should be(Nil)

    }

    @Test
    def testGlobalOutgoingViolation() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = GlobalOutgoingConstraint("field_type", ensembleA, ensembleB)
        val constraints = Set(constraint)

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "test")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)

        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)

        bc.classfiles.element_added(b)
        bc.classfiles.element_added(c)


        result.asList should be(
            List(
                Violation(
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                )
            )
        )

    }


    @Test
    def testGlobalDifferentOutgoingViolations() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraintAToB = GlobalOutgoingConstraint("field_type", ensembleA, ensembleB)
        val constraintBToC = GlobalOutgoingConstraint("field_type", ensembleB, ensembleC)

        val constraints = Set(constraintAToB, constraintBToC)

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "context")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)

        val fieldRefBToA = Field(b, "fieldBToA", a)
        val fieldRefBToC = Field(b, "fieldBToC", c)
        val fieldRefCToA = Field(c, "fieldCToA", a)
        val fieldRefCToB = Field(c, "fieldCToB", b)

        val fieldRefAToA = Field(a, "selfFieldInA", a)


        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToA)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)


        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRefBToA)
        bc.classfile_fields.element_added(fieldRefBToC)

        bc.classfiles.element_added(c)
        bc.classfile_fields.element_added(fieldRefCToA)
        bc.classfile_fields.element_added(fieldRefCToB)

        result.asList should be(
            List(
                Violation(
                    constraintBToC,
                    ensembleB,
                    ensembleA,
                    SourceElement(fieldRefBToA),
                    SourceElement(a),
                    ""
                ),
                Violation(
                    constraintAToB,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                )
            )
        )

    }

    @Test
    def testGlobalOutgoingViolationWithoutLocalEnsembleMention() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = GlobalOutgoingConstraint("field_type", ensembleA, ensembleB)
        val constraints = Set(constraint)

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(Set(ensembleA, ensembleB), constraints, "context")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)

        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)

        bc.classfiles.element_added(b)
        bc.classfiles.element_added(c)

        result.asList should be(
            List(
                Violation(
                    constraint,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                )
            )
        )

    }


    @Test
    def testGlobalOutgoingViolationWithExcessDependencies() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembles = Set(ensembleA, ensembleB, ensembleC)

        val constraint = OutgoingConstraint("field_type", ensembleA, ensembleB)
        val constraints = Set(
            constraint
        )

        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "context")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)

        val fieldRefBToA = Field(b, "fieldBToA", a)
        val fieldRefBToC = Field(b, "fieldBToC", c)
        val fieldRefCToA = Field(c, "fieldCToA", a)
        val fieldRefCToB = Field(c, "fieldCToB", b)

        val fieldRefAToA = Field(a, "selfFieldInA", a)


        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToA)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)


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
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                )
            )
        )
    }


    @Test
    def testGlobalOutgoingMultipleContexts() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)

        val constraintAToB = GlobalOutgoingConstraint("field_type", ensembleA, ensembleB)
        val constraintAToC = GlobalOutgoingConstraint("field_type", ensembleA, ensembleC)

        val global = GlobalArchitectureModel(Set(ensembleA, ensembleB, ensembleC))
        val modelA = ArchitectureModel(Set(ensembleA, ensembleB), Set(constraintAToB), "contextAToB")
        val modelB = ArchitectureModel(Set(ensembleA, ensembleC), Set(constraintAToC), "contextAToC")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(modelA)
        db.addModel(modelB)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")

        val fieldRefAToB = Field(a, "fieldAToB", b)
        val fieldRefAToC = Field(a, "fieldAToC", c)


        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)


        bc.classfiles.element_added(b)
        bc.classfiles.element_added(c)

        result.asList should be(
            List(
                Violation(
                    constraintAToC,
                    ensembleA,
                    ensembleB,
                    SourceElement(fieldRefAToB),
                    SourceElement(b),
                    ""
                ),
                Violation(
                    constraintAToB,
                    ensembleA,
                    ensembleC,
                    SourceElement(fieldRefAToC),
                    SourceElement(c),
                    ""
                )
            )
        )
    }


    @Test
    def testMixofGlobalLocalOutgoing() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')", Set.empty)
        val ensembleB = Ensemble("B", "class_with_members('test','B')", Set.empty)
        val ensembleC = Ensemble("C", "class_with_members('test','C')", Set.empty)
        val ensembleD = Ensemble("D", "class_with_members('test','D')", Set.empty)
        val ensembleE = Ensemble("E", "class_with_members('test','E')", Set.empty)

        val constraintAToB = GlobalOutgoingConstraint("field_type", ensembleA, ensembleB)
        val constraintAToC = OutgoingConstraint("field_type", ensembleA, ensembleC)

        val global = GlobalArchitectureModel(Set(ensembleA, ensembleB, ensembleC, ensembleD, ensembleE))
        val model = ArchitectureModel(Set(ensembleA, ensembleB, ensembleC, ensembleD), Set(constraintAToB, constraintAToC), "context")

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)
        db.addGlobalModel(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")
        val e = ObjectType("test/E")

        val fieldRefAToB = Field(a, "fieldToB", b)
        val fieldRefAToC = Field(a, "fieldToC", c)
        val fieldRefAToD = Field(a, "fieldToD", d)
        val fieldRefAToE = Field(a, "fieldToE", e)

        bc.classfiles.element_added(a)
        bc.classfile_fields.element_added(fieldRefAToB)
        bc.classfile_fields.element_added(fieldRefAToC)
        bc.classfile_fields.element_added(fieldRefAToD)
        bc.classfile_fields.element_added(fieldRefAToE)

        bc.classfiles.element_added(b)
        bc.classfiles.element_added(c)
        bc.classfiles.element_added(d)
        bc.classfiles.element_added(e)

        result.asList should be(
            List(
                Violation(
                    constraintAToB,
                    ensembleA,
                    ensembleD,
                    SourceElement(fieldRefAToD),
                    SourceElement(d),
                    ""
                ),
                Violation(
                    constraintAToC,
                    ensembleA,
                    ensembleD,
                    SourceElement(fieldRefAToD),
                    SourceElement(d),
                    ""
                ),
                Violation(
                    constraintAToB,
                    ensembleA,
                    ensembleE,
                    SourceElement(fieldRefAToE),
                    SourceElement(e),
                    ""
                )
            )
        )

    }

}