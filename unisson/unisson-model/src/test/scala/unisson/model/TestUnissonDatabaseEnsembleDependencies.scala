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


    @Test
    def testParentChildDependencyExclusion() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)
        val ensembles = Set(ensembleA)


        db.setRepository(Repository(ensembles))

        val a1 = ObjectType("test/A1")
        val a2 = ObjectType("test/A2")

        val fieldRefA1ToA2 = FieldDeclaration(a1, "fieldA1InB", a2)
        val fieldRefA2ToA1 = FieldDeclaration(a2, "fieldA2InB", a1)

        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)

        bc.declared_fields.element_added(fieldRefA1ToA2)
        bc.declared_fields.element_added(fieldRefA2ToA1)


        db.ensemble_elements.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a1)),
                (ensembleA, SourceElement(fieldRefA1ToA2)),
                (ensembleA, SourceElement(a2)),
                (ensembleA, SourceElement(fieldRefA2ToA1)),
                (ensembleA1, SourceElement(a1)),
                (ensembleA1, SourceElement(fieldRefA1ToA2)),
                (ensembleA2, SourceElement(a2)),
                (ensembleA2, SourceElement(fieldRefA2ToA1))
            )
        )

        db.ensemble_dependencies.asList.sorted should be(
            List(
                (ensembleA1, ensembleA2, SourceElement(fieldRefA1ToA2), SourceElement(a2), FieldTypeKind
                        .asVespucciString),
                (ensembleA2, ensembleA1, SourceElement(fieldRefA2ToA1), SourceElement(a1), FieldTypeKind
                        .asVespucciString)
            )
        )
    }

    @Test
    def testTransitiveChildrenDependencyExclusion() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2, ensembleA3)
        val ensembles = Set(ensembleA)


        db.setRepository(Repository(ensembles))

        val a1 = ObjectType("test/inner/A1")
        val a2 = ObjectType("test/inner/A2")

        val a4 = ObjectType("test/inner/inner/A4")
        val a5 = ObjectType("test/inner/inner/A5")


        val fieldRefA1ToA2 = FieldDeclaration(a1, "fieldA2InA1", a2)
        val fieldRefA1ToA4 = FieldDeclaration(a1, "fieldA4InA1", a4)
        val fieldRefA1ToA5 = FieldDeclaration(a1, "fieldA5InA1", a5)
        val fieldRefA2ToA1 = FieldDeclaration(a2, "fieldA1InA2", a1)
        val fieldRefA4ToA1 = FieldDeclaration(a4, "fieldA1InA4", a1)
        val fieldRefA4ToA5 = FieldDeclaration(a4, "fieldA5InA4", a5)
        val fieldRefA5ToA2 = FieldDeclaration(a5, "fieldA2InA5", a2)


        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)
        bc.declared_types.element_added(a4)
        bc.declared_types.element_added(a5)



        bc.declared_fields.element_added(fieldRefA1ToA2)
        bc.declared_fields.element_added(fieldRefA1ToA4)
        bc.declared_fields.element_added(fieldRefA1ToA5)
        bc.declared_fields.element_added(fieldRefA2ToA1)
        bc.declared_fields.element_added(fieldRefA4ToA1)
        bc.declared_fields.element_added(fieldRefA4ToA5)
        bc.declared_fields.element_added(fieldRefA5ToA2)

        db.ensemble_elements.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a1)),
                (ensembleA, SourceElement(fieldRefA1ToA2)),
                (ensembleA, SourceElement(fieldRefA1ToA4)),
                (ensembleA, SourceElement(fieldRefA1ToA5)),
                (ensembleA, SourceElement(a2)),
                (ensembleA, SourceElement(fieldRefA2ToA1)),
                (ensembleA, SourceElement(a4)),
                (ensembleA, SourceElement(fieldRefA4ToA1)),
                (ensembleA, SourceElement(fieldRefA4ToA5)),
                (ensembleA, SourceElement(a5)),
                (ensembleA, SourceElement(fieldRefA5ToA2)),
                (ensembleA1, SourceElement(a1)),
                (ensembleA1, SourceElement(fieldRefA1ToA2)),
                (ensembleA1, SourceElement(fieldRefA1ToA4)),
                (ensembleA1, SourceElement(fieldRefA1ToA5)),
                (ensembleA2, SourceElement(a2)),
                (ensembleA2, SourceElement(fieldRefA2ToA1)),
                (ensembleA3, SourceElement(a4)),
                (ensembleA3, SourceElement(fieldRefA4ToA1)),
                (ensembleA3, SourceElement(fieldRefA4ToA5)),
                (ensembleA3, SourceElement(a5)),
                (ensembleA3, SourceElement(fieldRefA5ToA2)),
                (ensembleA4, SourceElement(a4)),
                (ensembleA4, SourceElement(fieldRefA4ToA1)),
                (ensembleA4, SourceElement(fieldRefA4ToA5)),
                (ensembleA5, SourceElement(a5)),
                (ensembleA5, SourceElement(fieldRefA5ToA2))
            )
        )

        db.ensemble_dependencies.asList.sorted should be(
            List(
                (ensembleA1, ensembleA2, SourceElement(fieldRefA1ToA2), SourceElement(a2), FieldTypeKind
                        .asVespucciString),
                (ensembleA1, ensembleA3, SourceElement(fieldRefA1ToA4), SourceElement(a4), FieldTypeKind
                        .asVespucciString),
                (ensembleA1, ensembleA3, SourceElement(fieldRefA1ToA5), SourceElement(a5), FieldTypeKind
                        .asVespucciString),
                (ensembleA1, ensembleA4, SourceElement(fieldRefA1ToA4), SourceElement(a4), FieldTypeKind
                        .asVespucciString),
                (ensembleA1, ensembleA5, SourceElement(fieldRefA1ToA5), SourceElement(a5), FieldTypeKind
                        .asVespucciString),
                (ensembleA2, ensembleA1, SourceElement(fieldRefA2ToA1), SourceElement(a1), FieldTypeKind
                        .asVespucciString),
                (ensembleA3, ensembleA1, SourceElement(fieldRefA4ToA1), SourceElement(a1), FieldTypeKind
                        .asVespucciString),
                (ensembleA3, ensembleA2, SourceElement(fieldRefA5ToA2), SourceElement(a2), FieldTypeKind
                        .asVespucciString),
                (ensembleA4, ensembleA1, SourceElement(fieldRefA4ToA1), SourceElement(a1), FieldTypeKind
                        .asVespucciString),
                (ensembleA4, ensembleA5, SourceElement(fieldRefA4ToA5), SourceElement(a5), FieldTypeKind
                        .asVespucciString),
                (ensembleA5, ensembleA2, SourceElement(fieldRefA5ToA2), SourceElement(a2), FieldTypeKind
                        .asVespucciString)
            )
        )
    }


    @Test
    def testDoubleDependency() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembles = Set(ensembleA, ensembleB)

        val global = Repository(ensembles)

        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val field1BFromA = FieldDeclaration(a, "myB1", b)
        val field2BFromA = FieldDeclaration(a, "myB2", b)
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(field1BFromA)
        bc.declared_fields.element_added(field2BFromA)

        db.ensemble_dependencies.asList should be(
            List(
                (ensembleA, ensembleB, SourceElement(field1BFromA), SourceElement(b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElement(field2BFromA), SourceElement(b), FieldTypeKind.asVespucciString)
            )
        )

        db.ensemble_dependency_count.asList should be(
            List(
                (ensembleA, ensembleB, 2)
            )
        )
    }


    @Test
    def testDoubleDependencyCount() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class_with_members('test','A')")
        val ensembleB = Ensemble("B", "class_with_members('test','B')")
        val ensembles = Set(ensembleA, ensembleB)

        val global = Repository(ensembles)

        db.setRepository(global)

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val field1BFromA = FieldDeclaration(a, "myB1", b)
        val field2BFromA = FieldDeclaration(a, "myB2", b)
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)
        bc.declared_fields.element_added(field1BFromA)
        bc.declared_fields.element_added(field2BFromA)

        db.ensemble_dependency_count.asList should be(
            List(
                (ensembleA, ensembleB, 2)
            )
        )
    }
}