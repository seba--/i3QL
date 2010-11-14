package saere.database.predicate.full;

import saere.database.predicate.DatabasePredicate;

/**
 * Marker interface to signal that the implementing {@link DatabasePredicate} 
 * depends on a database that guarantees no collison of terms (which makes 
 * <i>real</i> unification unnecessary).
 * 
 * @author David Sullivan
 * @version 0.1, 11/1/2010
 */
public interface NoCollision {

}
