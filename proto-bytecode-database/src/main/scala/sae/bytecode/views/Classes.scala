package sae
package bytecode
package views

import sae.Observable
import sae.bytecode.model._

class Classes extends LazyView[ClassFile]
{
    
    def lazy_foreach[T](f : (ClassFile) => T) = {}
    
}

/**
 * a singleton table of all classes
 */
//object classes extends Classes