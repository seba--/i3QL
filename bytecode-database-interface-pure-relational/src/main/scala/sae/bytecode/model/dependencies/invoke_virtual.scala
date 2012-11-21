package sae.bytecode.model.dependencies

import sae.bytecode.model.{MethodDeclaration, MethodReference}


case class invoke_virtual(source: MethodDeclaration, target: MethodReference)
        extends calls