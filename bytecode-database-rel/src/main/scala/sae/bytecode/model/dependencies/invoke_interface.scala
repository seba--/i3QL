package sae.bytecode.model.dependencies

import sae.bytecode.model.{MethodDeclaration, MethodReference}


case class invoke_interface(source: MethodDeclaration, target: MethodReference)
        extends calls