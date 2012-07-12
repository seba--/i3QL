package unisson.model

import kinds.primitive.FieldTypeKind
import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElement
import sae.collections.Conversions
import org.junit.Test
import de.tud.cs.st.bat.{VoidType, ObjectType}
import sae.bytecode.model.dependencies.`extends`
import sae.bytecode.model.instructions.{putfield, push, invokespecial}
import sae.bytecode.BytecodeDatabase
import sae.bytecode.model._

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:22
 *
 */
class TestUnissonDatabaseEnsembleDependencies
        extends ShouldMatchers
{


    import Conversions._

    import UnissonOrdering._

    @Test
    def testFilterSelfDependency() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("Test", "package('test')")
        val ensembles = Set(ensembleA)

        val global = Repository(ensembles)

        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val fieldBFromA = FieldDeclaration(a, "myB", b)
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(fieldBFromA)


        db.ensemble_dependencies.asList should be(Nil)

    }


    @Test
    def testDirectFieldRefDependency() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembles = Set(ensembleA, ensembleB)

        val global = Repository(ensembles)

        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val fieldBFromA = FieldDeclaration(a, "myB", b)
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(fieldBFromA)

        db.ensemble_dependencies.asList should be(
            List(
                (ensembleA, ensembleB, SourceElement(fieldBFromA), SourceElement(b), FieldTypeKind.asVespucciString)
            )
        )

    }


    @Test
    def testUnmodeledElementNoDependency() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembles = Set(ensembleA, ensembleB)


        val global = Repository(ensembles)

        db.setRepository(global)

        val obj = ObjectType("java/lang/Object")
        val objectConstructor = MethodReference(obj, "<init>", Nil, VoidType())
        val a = ObjectType("test/A")
        val aConstructor = MethodDeclaration(a, "<init>", Nil, VoidType())
        val b = ObjectType("test/B")
        val fieldBFromA = FieldDeclaration(a, "myB", b)

        bc.declared_types.element_added(a)
        bc.`extends`.element_added(`extends`(a, obj))


        bc.declared_methods.element_added(aConstructor)
        bc.instructions.element_added(invokespecial(aConstructor, 1, objectConstructor))
        bc.instructions.element_added(push(aConstructor, 3, null, obj))
        bc.instructions.element_added(putfield(aConstructor, 4, FieldReference(a, "myB", b)))

        bc.declared_fields.element_added(fieldBFromA)

        bc.declared_types.element_added(b)

        db.ensemble_dependencies.asList should be(
            List(
                (ensembleA, ensembleB, SourceElement(fieldBFromA), SourceElement(b), FieldTypeKind.asVespucciString)
            )
        )

    }

    @Test
    def testMultipleDirectDependencies() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembleC = Ensemble("C", "class_with_members('test','C')")
        val ensembleD = Ensemble("D", "class_with_members('test','D')")

        val global = Repository(Set(ensembleA, ensembleB, ensembleC, ensembleD))

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



        db.ensemble_dependencies.asList.sorted should be(
            List(
                (ensembleB, ensembleA, SourceElement(fieldRefBToA), SourceElement(a), FieldTypeKind.asVespucciString),
                (ensembleC, ensembleA, SourceElement(fieldRefCToA), SourceElement(a), FieldTypeKind.asVespucciString),
                (ensembleD, ensembleA, SourceElement(fieldRefDToA), SourceElement(a), FieldTypeKind.asVespucciString)
            )
        )

    }


    @Test
    def testTwoLevelChildrenDerivedIncomingDependencies() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembles = Set(ensembleA, ensembleB)


        db.setRepository(Repository(ensembles))

        val a1 = ObjectType("test/A1")
        val a2 = ObjectType("test/A2")
        val b = ObjectType("test/B")

        val fieldRefBToA1 = FieldDeclaration(b, "fieldA1InB", a1)
        val fieldRefBToA2 = FieldDeclaration(b, "fieldA2InB", a2)

        bc.declared_types.element_added(b)
        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)

        bc.declared_fields.element_added(fieldRefBToA1)
        bc.declared_fields.element_added(fieldRefBToA2)


        db.ensemble_elements.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a1)),
                (ensembleA, SourceElement(a2)),
                (ensembleA1, SourceElement(a1)),
                (ensembleA2, SourceElement(a2)),
                (ensembleB, SourceElement(b)),
                (ensembleB, SourceElement(fieldRefBToA1)),
                (ensembleB, SourceElement(fieldRefBToA2))
            )
        )

        db.ensemble_dependencies.asList.sorted should be(
            List(
                (ensembleB, ensembleA, SourceElement(fieldRefBToA1), SourceElement(a1), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElement(fieldRefBToA2), SourceElement(a2), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA1, SourceElement(fieldRefBToA1), SourceElement(a1), FieldTypeKind
                        .asVespucciString),
                (ensembleB, ensembleA2, SourceElement(fieldRefBToA2), SourceElement(a2), FieldTypeKind.asVespucciString)
            )
        )
    }


    @Test
    def testThreeLevelChildrenDerivedIncomingDependencies() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class('test.inner','A1')")
        val ensembleA2 = Ensemble("A2", "class('test.inner','A2')")

        val ensembleA4 = Ensemble("A4", "class('test.inner.inner','A4')")
        val ensembleA5 = Ensemble("A5", "class('test.inner.inner','A5')")
        val ensembleA3 = Ensemble("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2, ensembleA3)
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembles = Set(ensembleA, ensembleB)


        db.setRepository(Repository(ensembles))

        val a1 = ObjectType("test/inner/A1")
        val a2 = ObjectType("test/inner/A2")

        val a4 = ObjectType("test/inner/inner/A4")
        val a5 = ObjectType("test/inner/inner/A5")
        val b = ObjectType("test/B")


        val fieldRefBToA1 = FieldDeclaration(b, "fieldA1InB", a1)
        val fieldRefBToA2 = FieldDeclaration(b, "fieldA2InB", a2)
        val fieldRefBToA4 = FieldDeclaration(b, "fieldA4InB", a4)
        val fieldRefBToA5 = FieldDeclaration(b, "fieldA5InB", a5)




        bc.declared_types.element_added(b)
        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(a4)
        bc.declared_types.element_added(a5)


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

        db.ensemble_dependencies.asList.sorted should be(
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
    }

}