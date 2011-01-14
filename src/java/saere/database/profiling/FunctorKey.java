package saere.database.profiling;

import java.io.Serializable;

import saere.database.index.FunctorLabel;

/**
 * A serializable key ({@link FunctorLabel} is not serializable).
 * 
 * @author David Sullivan
 * @version 1.0, 11/25/2010
 */
public class FunctorKey implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String functor;
	private final int arity;
	
	public FunctorKey(String functor, int arity) {
		this.functor = functor;
		this.arity = arity;
	}
	
	public String functor() {
		return functor;
	}
	
	public int arity() {
		return arity;
	}
	
	@Override
	public int hashCode() {
		return functor.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof FunctorKey && functor.equals(((FunctorKey) obj).functor) && arity == ((FunctorKey) obj).arity;
	}
}