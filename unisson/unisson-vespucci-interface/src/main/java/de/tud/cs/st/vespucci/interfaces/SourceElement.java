package de.tud.cs.st.vespucci.interfaces;

/**
 * @author Ralf Mitschke
 */
public interface SourceElement<T extends Object>
        extends ICodeElement {

    public T element();
}
