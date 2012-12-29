package unisson.model

import impl.{Repository, Ensemble}
import kinds.primitive.FieldTypeKind
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElementFactory
import org.junit.Test
import org.junit.Assert._
import de.tud.cs.st.bat._
import resolved.{MethodDescriptor, VoidType, ObjectType}
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure._
import sae.bytecode.instructions.INVOKESPECIAL
import sae.bytecode.structure.InheritanceRelation
import sae.bytecode.instructions.PUTFIELD
import UnissonOrdering._
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


    @Test
    def testFilterSelfDependency() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_dependencies

        val ensembleA = Ensemble ("Test", "package('test')")
        val ensembles = Set (ensembleA)

        val global = Repository (ensembles)

        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val fieldBFromA = FieldDeclaration (a, "myB", b)
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldBFromA)


        db.ensemble_dependencies.asList should be (Nil)

    }


    @Test
    def testDirectFieldRefDependency() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_dependencies

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembles = Set (ensembleA, ensembleB)

        val global = Repository (ensembles)

        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val fieldBFromA = FieldDeclaration (a, "myB", b)
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldBFromA)

        db.ensemble_dependencies.asList should be (
            List (
                (ensembleA, ensembleB, SourceElementFactory (fieldBFromA), SourceElementFactory (b), FieldTypeKind.asVespucciString)
            )
        )

    }


    @Test
    def testUnmodeledElementNoDependency() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_dependencies

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembles = Set (ensembleA, ensembleB)


        val global = Repository (ensembles)

        db.setRepository (global)

        val obj = ObjectType ("java/lang/Object")
        val a = ObjectType ("test/A")
        val aConstructor = MethodDeclaration (a, "<init>", VoidType, Nil)
        val b = ObjectType ("test/B")
        val fieldBFromA = FieldDeclaration (a, "myB", b)

        bc.typeDeclarations.element_added (a)
        bc.classInheritance.element_added (InheritanceRelation (a, obj))


        bc.methodDeclarations.element_added (aConstructor)
        bc.instructions.element_added (INVOKESPECIAL (aConstructor, resolved.INVOKESPECIAL (obj, "<init>", MethodDescriptor (Nil, VoidType)), 1, 1))
        //bc.instructions.element_added (PUSH (aConstructor, null, obj, 3, 2))
        bc.instructions.element_added (PUTFIELD (aConstructor, resolved.PUTFIELD (a, "myB", b), 4, 3))

        bc.fieldDeclarations.element_added (fieldBFromA)

        bc.typeDeclarations.element_added (b)

        db.ensemble_dependencies.asList should be (
            List (
                (ensembleA, ensembleB, SourceElementFactory (fieldBFromA), SourceElementFactory (b), FieldTypeKind.asVespucciString)
            )
        )

    }

    @Test
    def testMultipleDirectDependencies() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_dependencies

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembleD = Ensemble ("D", "class_with_members('test','D')")

        val global = Repository (Set (ensembleA, ensembleB, ensembleC, ensembleD))

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



        db.ensemble_dependencies.asList.sorted should be (
            List (
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA), SourceElementFactory (a), FieldTypeKind.asVespucciString),
                (ensembleC, ensembleA, SourceElementFactory (fieldRefCToA), SourceElementFactory (a), FieldTypeKind.asVespucciString),
                (ensembleD, ensembleA, SourceElementFactory (fieldRefDToA), SourceElementFactory (a), FieldTypeKind.asVespucciString)
            )
        )

    }


    @Test
    def testTwoLevelChildrenDerivedIncomingDependencies() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_elements
        db.ensemble_dependencies
        db.ensemble_elements
        db.ensemble_dependencies

        val ensembleA1 = Ensemble ("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble ("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble ("A", "derived", ensembleA1, ensembleA2)
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembles = Set (ensembleA, ensembleB)


        db.setRepository (Repository (ensembles))

        val a1 = ObjectType ("test/A1")
        val a2 = ObjectType ("test/A2")
        val b = ObjectType ("test/B")

        val fieldRefBToA1 = FieldDeclaration (b, "fieldA1InB", a1)
        val fieldRefBToA2 = FieldDeclaration (b, "fieldA2InB", a2)

        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)

        bc.fieldDeclarations.element_added (fieldRefBToA1)
        bc.fieldDeclarations.element_added (fieldRefBToA2)



        assertEquals(
            List (
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleB, SourceElementFactory (b)),
                (ensembleB, SourceElementFactory (fieldRefBToA1)),
                (ensembleB, SourceElementFactory (fieldRefBToA2))
            ),
            db.ensemble_elements.asList.sorted
        )


        assertEquals(
            List (
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA1), SourceElementFactory (a1), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA, SourceElementFactory (fieldRefBToA2), SourceElementFactory (a2), FieldTypeKind.asVespucciString),
                (ensembleB, ensembleA1, SourceElementFactory (fieldRefBToA1), SourceElementFactory (a1), FieldTypeKind
                    .asVespucciString),
                (ensembleB, ensembleA2, SourceElementFactory (fieldRefBToA2), SourceElementFactory (a2), FieldTypeKind.asVespucciString)
            ),
            db.ensemble_dependencies.asList.sorted
        )
    }


    @Test
    def testThreeLevelChildrenDerivedIncomingDependencies() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_elements
        db.ensemble_dependencies

        val ensembleA1 = Ensemble ("A1", "class('test.inner','A1')")
        val ensembleA2 = Ensemble ("A2", "class('test.inner','A2')")

        val ensembleA4 = Ensemble ("A4", "class('test.inner.inner','A4')")
        val ensembleA5 = Ensemble ("A5", "class('test.inner.inner','A5')")
        val ensembleA3 = Ensemble ("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble ("A", "derived", ensembleA1, ensembleA2, ensembleA3)
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembles = Set (ensembleA, ensembleB)


        db.setRepository (Repository (ensembles))

        val a1 = ObjectType ("test/inner/A1")
        val a2 = ObjectType ("test/inner/A2")

        val a4 = ObjectType ("test/inner/inner/A4")
        val a5 = ObjectType ("test/inner/inner/A5")
        val b = ObjectType ("test/B")


        val fieldRefBToA1 = FieldDeclaration (b, "fieldA1InB", a1)
        val fieldRefBToA2 = FieldDeclaration (b, "fieldA2InB", a2)
        val fieldRefBToA4 = FieldDeclaration (b, "fieldA4InB", a4)
        val fieldRefBToA5 = FieldDeclaration (b, "fieldA5InB", a5)




        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)
        bc.typeDeclarations.element_added (a4)
        bc.typeDeclarations.element_added (a5)


        bc.fieldDeclarations.element_added (fieldRefBToA1)
        bc.fieldDeclarations.element_added (fieldRefBToA2)
        bc.fieldDeclarations.element_added (fieldRefBToA4)
        bc.fieldDeclarations.element_added (fieldRefBToA5)



        assertEquals(
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
            ),
            db.ensemble_elements.asList.sorted
        )


        assertEquals(
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
            ),
            db.ensemble_dependencies.asList.sorted
        )
    }


    @Test
    def testParentChildDependencyExclusion() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_elements
        db.ensemble_dependencies

        val ensembleA1 = Ensemble ("A1", "class_with_members('test','A1')")
        val ensembleA2 = Ensemble ("A2", "class_with_members('test','A2')")
        val ensembleA = Ensemble ("A", "derived", ensembleA1, ensembleA2)
        val ensembles = Set (ensembleA)


        db.setRepository (Repository (ensembles))

        val a1 = ObjectType ("test/A1")
        val a2 = ObjectType ("test/A2")

        val fieldRefA1ToA2 = FieldDeclaration (a1, "fieldA1InB", a2)
        val fieldRefA2ToA1 = FieldDeclaration (a2, "fieldA2InB", a1)

        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)

        bc.fieldDeclarations.element_added (fieldRefA1ToA2)
        bc.fieldDeclarations.element_added (fieldRefA2ToA1)



        assertEquals (
            List (
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (fieldRefA1ToA2)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA, SourceElementFactory (fieldRefA2ToA1)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA1, SourceElementFactory (fieldRefA1ToA2)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleA2, SourceElementFactory (fieldRefA2ToA1))
            ),
            db.ensemble_elements.asList.sorted
        )


        assertEquals (
            List (
                (ensembleA1, ensembleA2, SourceElementFactory (fieldRefA1ToA2), SourceElementFactory (a2), FieldTypeKind
                    .asVespucciString),
                (ensembleA2, ensembleA1, SourceElementFactory (fieldRefA2ToA1), SourceElementFactory (a1), FieldTypeKind
                    .asVespucciString)
            ),
            db.ensemble_dependencies.asList.sorted
        )
    }

    @Test
    def testTransitiveChildrenDependencyExclusion() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_elements
        db.ensemble_dependencies

        val ensembleA1 = Ensemble ("A1", "class_with_members('test.inner','A1')")
        val ensembleA2 = Ensemble ("A2", "class_with_members('test.inner','A2')")

        val ensembleA4 = Ensemble ("A4", "class_with_members('test.inner.inner','A4')")
        val ensembleA5 = Ensemble ("A5", "class_with_members('test.inner.inner','A5')")
        val ensembleA3 = Ensemble ("A3", "derived", ensembleA4, ensembleA5)
        val ensembleA = Ensemble ("A", "derived", ensembleA1, ensembleA2, ensembleA3)
        val ensembles = Set (ensembleA)


        db.setRepository (Repository (ensembles))

        val a1 = ObjectType ("test/inner/A1")
        val a2 = ObjectType ("test/inner/A2")

        val a4 = ObjectType ("test/inner/inner/A4")
        val a5 = ObjectType ("test/inner/inner/A5")


        val fieldRefA1ToA2 = FieldDeclaration (a1, "fieldA2InA1", a2)
        val fieldRefA1ToA4 = FieldDeclaration (a1, "fieldA4InA1", a4)
        val fieldRefA1ToA5 = FieldDeclaration (a1, "fieldA5InA1", a5)
        val fieldRefA2ToA1 = FieldDeclaration (a2, "fieldA1InA2", a1)
        val fieldRefA4ToA1 = FieldDeclaration (a4, "fieldA1InA4", a1)
        val fieldRefA4ToA5 = FieldDeclaration (a4, "fieldA5InA4", a5)
        val fieldRefA5ToA2 = FieldDeclaration (a5, "fieldA2InA5", a2)


        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)
        bc.typeDeclarations.element_added (a4)
        bc.typeDeclarations.element_added (a5)



        bc.fieldDeclarations.element_added (fieldRefA1ToA2)
        bc.fieldDeclarations.element_added (fieldRefA1ToA4)
        bc.fieldDeclarations.element_added (fieldRefA1ToA5)
        bc.fieldDeclarations.element_added (fieldRefA2ToA1)
        bc.fieldDeclarations.element_added (fieldRefA4ToA1)
        bc.fieldDeclarations.element_added (fieldRefA4ToA5)
        bc.fieldDeclarations.element_added (fieldRefA5ToA2)

        assertEquals (
            List (
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (fieldRefA1ToA2)),
                (ensembleA, SourceElementFactory (fieldRefA1ToA4)),
                (ensembleA, SourceElementFactory (fieldRefA1ToA5)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA, SourceElementFactory (fieldRefA2ToA1)),
                (ensembleA, SourceElementFactory (a4)),
                (ensembleA, SourceElementFactory (fieldRefA4ToA1)),
                (ensembleA, SourceElementFactory (fieldRefA4ToA5)),
                (ensembleA, SourceElementFactory (a5)),
                (ensembleA, SourceElementFactory (fieldRefA5ToA2)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA1, SourceElementFactory (fieldRefA1ToA2)),
                (ensembleA1, SourceElementFactory (fieldRefA1ToA4)),
                (ensembleA1, SourceElementFactory (fieldRefA1ToA5)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleA2, SourceElementFactory (fieldRefA2ToA1)),
                (ensembleA3, SourceElementFactory (a4)),
                (ensembleA3, SourceElementFactory (fieldRefA4ToA1)),
                (ensembleA3, SourceElementFactory (fieldRefA4ToA5)),
                (ensembleA3, SourceElementFactory (a5)),
                (ensembleA3, SourceElementFactory (fieldRefA5ToA2)),
                (ensembleA4, SourceElementFactory (a4)),
                (ensembleA4, SourceElementFactory (fieldRefA4ToA1)),
                (ensembleA4, SourceElementFactory (fieldRefA4ToA5)),
                (ensembleA5, SourceElementFactory (a5)),
                (ensembleA5, SourceElementFactory (fieldRefA5ToA2))
            ),
            db.ensemble_elements.asList.sorted
        )



        assertEquals (
            List (
                (ensembleA1, ensembleA2, SourceElementFactory (fieldRefA1ToA2), SourceElementFactory (a2), FieldTypeKind
                    .asVespucciString),
                (ensembleA1, ensembleA3, SourceElementFactory (fieldRefA1ToA4), SourceElementFactory (a4), FieldTypeKind
                    .asVespucciString),
                (ensembleA1, ensembleA3, SourceElementFactory (fieldRefA1ToA5), SourceElementFactory (a5), FieldTypeKind
                    .asVespucciString),
                (ensembleA1, ensembleA4, SourceElementFactory (fieldRefA1ToA4), SourceElementFactory (a4), FieldTypeKind
                    .asVespucciString),
                (ensembleA1, ensembleA5, SourceElementFactory (fieldRefA1ToA5), SourceElementFactory (a5), FieldTypeKind
                    .asVespucciString),
                (ensembleA2, ensembleA1, SourceElementFactory (fieldRefA2ToA1), SourceElementFactory (a1), FieldTypeKind
                    .asVespucciString),
                (ensembleA3, ensembleA1, SourceElementFactory (fieldRefA4ToA1), SourceElementFactory (a1), FieldTypeKind
                    .asVespucciString),
                (ensembleA3, ensembleA2, SourceElementFactory (fieldRefA5ToA2), SourceElementFactory (a2), FieldTypeKind
                    .asVespucciString),
                (ensembleA4, ensembleA1, SourceElementFactory (fieldRefA4ToA1), SourceElementFactory (a1), FieldTypeKind
                    .asVespucciString),
                (ensembleA4, ensembleA5, SourceElementFactory (fieldRefA4ToA5), SourceElementFactory (a5), FieldTypeKind
                    .asVespucciString),
                (ensembleA5, ensembleA2, SourceElementFactory (fieldRefA5ToA2), SourceElementFactory (a2), FieldTypeKind
                    .asVespucciString)
            ),
            db.ensemble_dependencies.asList.sorted
        )
    }


    @Test
    def testDoubleDependency() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_dependency_count

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembles = Set (ensembleA, ensembleB)

        val global = Repository (ensembles)

        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val field1BFromA = FieldDeclaration (a, "myB1", b)
        val field2BFromA = FieldDeclaration (a, "myB2", b)
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (field1BFromA)
        bc.fieldDeclarations.element_added (field2BFromA)

        db.ensemble_dependencies.asList should be (
            List (
                (ensembleA, ensembleB, SourceElementFactory (field1BFromA), SourceElementFactory (b), FieldTypeKind.asVespucciString),
                (ensembleA, ensembleB, SourceElementFactory (field2BFromA), SourceElementFactory (b), FieldTypeKind.asVespucciString)
            )
        )

        db.ensemble_dependency_count.asList should be (
            List (
                (ensembleA, ensembleB, 2)
            )
        )
    }


    @Test
    def testDoubleDependencyCount() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)
        db.ensemble_dependency_count

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembles = Set (ensembleA, ensembleB)

        val global = Repository (ensembles)

        db.setRepository (global)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val field1BFromA = FieldDeclaration (a, "myB1", b)
        val field2BFromA = FieldDeclaration (a, "myB2", b)
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (field1BFromA)
        bc.fieldDeclarations.element_added (field2BFromA)

        db.ensemble_dependency_count.asList should be (
            List (
                (ensembleA, ensembleB, 2)
            )
        )
    }
}