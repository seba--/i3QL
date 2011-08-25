package sae

trait Size {
    /**
     * Returns the size of the view in terms of elements.
     * This can be a costly operation.
     * Implementors should cache the value in a self-maintained view, but clients can not rely on this.
     */
    def size : Int
}