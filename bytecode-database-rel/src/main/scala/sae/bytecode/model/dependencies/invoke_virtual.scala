package sae.bytecode.model.dependencies

import sae.bytecode.model.MethodReference

case class invoke_virtual(source : MethodReference, target : MethodReference)
        extends calls