package prologre;

public interface GoalIterator {

    /**
     * If this method succeeds - i.e., true is returned - at least one solution exists. The solution
     * is stored in the variables (bindings) passed to the unify method.
     * <p>
     * If false is returned, all further calls to this method will also return false. Furthermore,
     * all Variables must have the same state as before the call (i.e., it must not be the case that
     * some variables that were free before the call become instantiated).
     * </p>
     */
    boolean next();

}
