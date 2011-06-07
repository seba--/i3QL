package unisson

import hibernate_3_6.action_sad
import sae.collections.QueryResult
import org.junit.Test
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.{create, parameter, Dependency, invoke_interface}

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 14:18
 *
 */

class Hibernate_3_6_Test
{

    @Test
    def count_action_ensemble_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new action_sad(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        assertEquals(186, `org.hibernate.action`.size) // findall :- 188
        assertEquals(836, `org.hibernate.event`.size) // findall :- 839
        assertEquals(75, lock.size) // findall :- 839
        assertEquals(2315, HQL.size) // findall :- 2329
        assertEquals(1655, `org.hibernate.engine`.size) // findall :- 1688

    }

    @Test
    def find_action_violation_elements()
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
}

