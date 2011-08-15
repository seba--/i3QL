package unisson.hibernate_3_6

import sae.collections.QueryResult
import sae.bytecode.model.dependencies.create
import unisson.Queries._
import sae.bytecode.BytecodeDatabase
import unisson.{EnsembleDefinition, SourceElement}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.08.11 11:51
 *
 */

class action_sad_as_for_comprehension(db: BytecodeDatabase)
    extends hibernate_3_6_ensemble_definitions(db)
        with EnsembleDefinition
{

    def directTranslation = for( elem <- db.create;
                   if( `org.hibernate.action`.exists( target(elem) ) );
                   if( !`org.hibernate.action`.exists( source(elem) ) );
                    if( !lock.exists(source(elem)) );
                    if( !`org.hibernate.engine`.exists(source(elem)) );
                    if( !`org.hibernate.event`.exists(source(elem)) )
                 ) yield elem

    def scalaWay = for{ p <- packages
                            if( p.name != "org.hibernate.action" )
                            if( p.name != "lock" )
                            if( p.name != "org.hibernate.engine" )
                            if( p.name != "org.hibernate.event" )
                        cl <- p.classes
                        m <- cl.methods
                        Create(t) <- m.instructions // pattern matching, needs case class
                        if( t.package.name == "org.hibernate.action")
                    } yield (m, t)

/*
    val incoming_violation_create: QueryResult[create] =
        ( (db.create, target _) ⋉ (identity(_:SourceElement[AnyRef]), `org.hibernate.action`) ) ∩
        ( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.action`) ) ∩
        ( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), lock) ) ∩
        ( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.event`) ) ∩
        //( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), HQL) ) ∩
        ( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.engine`) )
*/
}