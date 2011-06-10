package unisson

import hibernate_3_6.{hibernate_3_6_ensemble_definitions, cache_sad, bytecode_sad, action_sad}
import org.junit.Test
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.{Method, Field}

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 14:18
 *
 */

class Hibernate_3_6_Test
{
    @Test
    def test_class_with_members()
    {
        val database = new BytecodeDatabase
        val queries = new Queries(database)
        import queries._
        import sae.syntax.RelationalAlgebraSyntax._

        val SessionFactory : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate", "SessionFactory")

        val SessionFactoryImpl : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate.impl", "SessionFactoryImpl")

        database.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        assertEquals(29, SessionFactory.size)

        /*
        val impl = SessionFactoryImpl.asList.sortBy{
            case SourceElement( ObjectType(name) ) => "1" + name
            case SourceElement( Field(ObjectType(cls), name, _) ) => "2" + cls + name
            case SourceElement( Method(ObjectType(cls), name, _, _) ) => "3" + cls + name
        }
        impl.foreach(println)
        */

        assertEquals(156, SessionFactoryImpl.size)
    }

    @Test
    def count_all_ensemble_elements()
    {
        val database = new BytecodeDatabase

        val ensembles = new hibernate_3_6_ensemble_definitions(database){
            // access each lazy val once to initialize queries
            `org.hibernate.action`
            `org.hibernate.bytecode`
            `org.hibernate.cache`
            `org.hibernate.engine`
            `org.hibernate.event`
            `org.hibernate.intercept`
            `org.hibernate.loader`
            `org.hibernate.persister`
            `org.hibernate.stat`
            `org.hibernate.tool`
            `org.hibernate.tuple`
            GlobalSettings
            HQL
            lock
            Metamodel_Configurator
            Session
        }

        import ensembles._

        database.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        assertEquals(186, `org.hibernate.action`.size) // findall :- 188
        assertEquals(410, `org.hibernate.bytecode`.size) // findall :- 416
        assertEquals(554, `org.hibernate.cache`.size) // findall :- 554
        assertEquals(1655, `org.hibernate.engine`.size) // findall :- 1688
        assertEquals(836, `org.hibernate.event`.size) // findall :- 839
        assertEquals(91, `org.hibernate.intercept`.size) // findall :- 92
        assertEquals(879, `org.hibernate.loader`.size) // findall :- 881
        assertEquals(1283, `org.hibernate.persister`.size) // findall :- 1289
        assertEquals(473, `org.hibernate.stat`.size) // findall :- 473
        assertEquals(275, `org.hibernate.tool`.size) // findall :- 275
        assertEquals(649, `org.hibernate.tuple`.size) // findall :- 650

        // FIXME not correct probably requires inner classes
        //assertEquals(493, GlobalSettings.size) // findall :- 494
        assertEquals(75, lock.size) // findall :- 839
        assertEquals(2315, HQL.size) // findall :- 2329
        // FIXME not correct probably requires inner classes (currently 2031)
        // assertEquals(2518, Metamodel_Configurator.size) // findall :- 2537
        //  FIXME not correct probably requires inner classes (currently 761)
        //assertEquals(763, Session.size) // findall :- 766

    }

    @Test
    def find_action_sad_violation_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new action_sad(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        incoming_engine_to_action_violation.foreach(println)
        assertEquals(0, incoming_engine_to_action_violation.size)

        incoming_event_to_action_violation.foreach(println)
        assertEquals(0, incoming_event_to_action_violation.size)

        incoming_HQL_to_action_violation.foreach(println)
        assertEquals(0, incoming_HQL_to_action_violation.size)

        incoming_lock_to_action_violation.foreach(println)
        assertEquals(0, incoming_lock_to_action_violation.size)

    }


    @Test
    def find_bytecode_sad_violation_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new bytecode_sad(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        incoming_invoke_interface_to_bytecode_violation.foreach(println)
        assertEquals(0, incoming_invoke_interface_to_bytecode_violation.size)

    }

    @Test
    def find_cache_sad_violation_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new cache_sad(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        incoming_invoke_interface_to_cache_violation.foreach(println)
        // TODO get correct value for the expectation
        //assertEquals(0, incoming_invoke_interface_to_cache_violation.size)

    }

}

