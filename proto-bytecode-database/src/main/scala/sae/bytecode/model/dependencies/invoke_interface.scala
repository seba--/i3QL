package sae.bytecode.model.dependencies

import sae.bytecode.model.Method

case class invoke_interface(source : Method, target : Method)
        extends Dependency[Method, Method]