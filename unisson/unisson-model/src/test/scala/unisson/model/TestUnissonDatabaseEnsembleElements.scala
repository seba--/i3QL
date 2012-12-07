package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElement
import org.junit.Test
import de.tud.cs.st.bat.resolved.ObjectType
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure.FieldDeclaration
import UnissonOrdering._

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:22
 *
 */
class TestUnissonDatabaseEnsembleElements
    extends ShouldMatchers
{


    @Test
    def testClassTypeQuery() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class('test','A')")
        val ensembleB = Ensemble ("B", "class('test','B')")
        val ensembles = Set (ensembleA, ensembleB)

        db.setRepository (Repository (ensembles))

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)

        db.ensemble_elements.asList.sorted should be (
            List (
                (ensembleA, SourceElement (a)),
                (ensembleB, SourceElement (b))
            )
        )

    }

    @Test
    def testChildrenAndDerivedParentQuery() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)


        val ensembleA1 = Ensemble ("A1", "class('test','A1')")
        val ensembleA2 = Ensemble ("A2", "class('test','A2')")
        val ensembleA = Ensemble ("A", "derived", ensembleA1, ensembleA2)
        val ensembleB = Ensemble ("B", "class('test','B')")
        val ensembles = Set (ensembleA, ensembleB)

        db.setRepository (Repository (ensembles))

        val a = ObjectType ("test/A")
        val a1 = ObjectType ("test/A1")
        val a2 = ObjectType ("test/A2")
        val b = ObjectType ("test/B")
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)

        db.ensemble_elements.asList.sorted should be (
            List (
                (ensembleA, SourceElement (a1)),
                (ensembleA, SourceElement (a2)),
                (ensembleA1, SourceElement (a1)),
                (ensembleA2, SourceElement (a2)),
                (ensembleB, SourceElement (b))
            )
        )

    }

    @Test
    def testChildrenAndDirectParentQuery() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)


        val ensembleA1 = Ensemble ("A1", "class('test.a','A1')")
        val ensembleA2 = Ensemble ("A2", "class('test.a','A2')")
        val ensembleA = Ensemble ("A", "package('test.a')", ensembleA1, ensembleA2)
        val ensembleB = Ensemble ("B", "class('test','B')")
        val ensembles = Set (ensembleA, ensembleB)

        db.setRepository (Repository (ensembles))

        val a = ObjectType ("test/a/A")
        val a1 = ObjectType ("test/a/A1")
        val a2 = ObjectType ("test/a/A2")
        val b = ObjectType ("test/B")
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (a1)
        bc.typeDeclarations.element_added (a2)

        db.ensemble_elements.asList.sorted should be (
            List (
                (ensembleA, SourceElement (a)),
                (ensembleA, SourceElement (a1)),
                (ensembleA, SourceElement (a2)),
                (ensembleA1, SourceElement (a1)),
                (ensembleA2, SourceElement (a2)),
                (ensembleB, SourceElement (b))
            )
        )

    }

    @Test
    def testClassWithMembersQuery() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)


        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembles = Set (ensembleA, ensembleB)


        db.setRepository (Repository (ensembles))

        val a = ObjectType ("test/A")

        val b = ObjectType ("test/B")

        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefAToB = FieldDeclaration (a, "fieldInA", b)

        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)

        bc.fieldDeclarations.element_added (fieldRefAToB)
        bc.fieldDeclarations.element_added (fieldRefBToA)



        db.ensemble_elements.asList.sorted should be (
            List (
                (ensembleA, SourceElement (a)),
                (ensembleA, SourceElement (fieldRefAToB)),
                (ensembleB, SourceElement (b)),
                (ensembleB, SourceElement (fieldRefBToA))

            )
        )

    }

    @Test
    def testTwoLevelDerivedClassWithMembersQuery() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)


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


        db.ensemble_elements.asList.sorted should be (
            List (
                (ensembleA, SourceElement (a1)),
                (ensembleA, SourceElement (a2)),
                (ensembleA1, SourceElement (a1)),
                (ensembleA2, SourceElement (a2)),
                (ensembleB, SourceElement (b)),
                (ensembleB, SourceElement (fieldRefBToA1)),
                (ensembleB, SourceElement (fieldRefBToA2))
            )
        )

    }
}