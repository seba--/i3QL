package unisson.model

import impl.{Repository, Ensemble}
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElementFactory
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
        val ensemble_elements = sae.relationToResult(db.ensemble_elements) 

        val ensembleA = Ensemble ("A", "class('test','A')")
        val ensembleB = Ensemble ("B", "class('test','B')")
        val ensembles = Set (ensembleA, ensembleB)

        db.setRepository (Repository (ensembles))

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)

        ensemble_elements.asList .sorted should be (
            List (
                (ensembleA, SourceElementFactory (a)),
                (ensembleB, SourceElementFactory (b))
            )
        )

    }

    @Test
    def testChildrenAndDerivedParentQuery() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)
        val ensemble_elements = sae.relationToResult(db.ensemble_elements)

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

        ensemble_elements.asList .sorted should be (
            List (
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleB, SourceElementFactory (b))
            )
        )

    }

    @Test
    def testChildrenAndDirectParentQuery() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)
        val ensemble_elements = sae.relationToResult(db.ensemble_elements) 

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

        ensemble_elements.asList .sorted should be (
            List (
                (ensembleA, SourceElementFactory (a)),
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleB, SourceElementFactory (b))
            )
        )

    }

    @Test
    def testClassWithMembersQuery() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)
        val ensemble_elements = sae.relationToResult(db.ensemble_elements) 

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



        ensemble_elements.asList .sorted should be (
            List (
                (ensembleA, SourceElementFactory (a)),
                (ensembleA, SourceElementFactory (fieldRefAToB)),
                (ensembleB, SourceElementFactory (b)),
                (ensembleB, SourceElementFactory (fieldRefBToA))

            )
        )

    }

    @Test
    def testTwoLevelDerivedClassWithMembersQuery() {
        val bc = BATDatabaseFactory.create()
        val db = new UnissonDatabase (bc)
        val ensemble_elements = sae.relationToResult(db.ensemble_elements) 

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


        ensemble_elements.asList .sorted should be (
            List (
                (ensembleA, SourceElementFactory (a1)),
                (ensembleA, SourceElementFactory (a2)),
                (ensembleA1, SourceElementFactory (a1)),
                (ensembleA2, SourceElementFactory (a2)),
                (ensembleB, SourceElementFactory (b)),
                (ensembleB, SourceElementFactory (fieldRefBToA1)),
                (ensembleB, SourceElementFactory (fieldRefBToA2))
            )
        )

    }
}