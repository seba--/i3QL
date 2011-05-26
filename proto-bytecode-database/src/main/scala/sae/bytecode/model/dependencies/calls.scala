package sae.bytecode.model.dependencies

import sae.bytecode.model.Method

case class calls(source : Method, target : Method)
        extends Dependency[Method, Method]