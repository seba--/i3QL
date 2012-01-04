package unisson.model

import mock.vespucci.{ArchitectureModel, NotAllowedConstraint, Ensemble}
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElement
import sae.collections.{Conversions, QueryResult}
import de.tud.cs.st.vespucci.model.IEnsemble
import de.tud.cs.st.vespucci.interfaces.{ICodeElement, IViolation}
import org.junit.{Ignore, Test}
import sae.bytecode.{Database, BytecodeDatabase}
import sae.bytecode.model.{Method, Field}
import de.tud.cs.st.bat.{VoidType, ObjectType}
import sae.bytecode.model.dependencies.{invoke_special, `extends`}
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

        val model = ArchitectureModel(ensembles, constraints)

        val result: QueryResult[(IEnsemble, SourceElement[AnyRef])] = Conversions.lazyViewToResult(db.ensembleElements)

        db.addModel(model)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        bc.classfiles.element_added(a)
        bc.classfiles.element_added(b)

        implicit val sort = new Ordering[(IEnsemble, SourceElement[AnyRef])]{
            def compare(x: (IEnsemble, SourceElement[AnyRef]), y: (IEnsemble, SourceElement[AnyRef])) : Int = {
                val e = x._1.getName.compare(y._1.getName)
                if(e != 0)
                {
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

        val model = ArchitectureModel(ensembles, constraints)

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)

        val obj = ObjectType("java/lang/Object")
        val superConst = Method(obj, "<init>", Nil, VoidType())
        val a = ObjectType("test/A")
        val initA = Method(a, "<init>", Nil, VoidType())
        val b = ObjectType("test/B")
        val fieldRef = Field(a, "myB", b)


        bc.classfiles.element_added(a)
        bc.`extends`.element_added(`extends`(a, obj) )


        bc.classfile_methods.element_added(initA)
        bc.instructions.element_added(invokespecial(initA, 1, superConst))
        bc.instructions.element_added(push(initA, 3, null, obj))
        bc.instructions.element_added(putfield(initA, 4, fieldRef ))

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

        val model = ArchitectureModel(ensembles, constraints)

        val result: QueryResult[IViolation] = Conversions.lazyViewToResult(db.violations)

        db.addModel(model)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val fieldRef = Field(a, "myA", a)
        bc.classfiles.element_added(a)
        bc.classfiles.element_added(b)
        bc.classfile_fields.element_added(fieldRef)

        result.asList should be(Nil)

    }
}