package sae.bytecode.model.dependencies

import sae.bytecode.model.MethodReference

trait calls extends Dependency[MethodReference, MethodReference]
{
    val source : MethodReference
    val target : MethodReference
}
