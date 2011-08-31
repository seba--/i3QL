package sae.bytecode.model.dependencies

import sae.bytecode.model.Method

trait calls extends Dependency[Method, Method]
{
    val source : Method
    val target : Method
}
