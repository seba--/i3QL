package sae.bytecode.model.dependencies

/**
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:46
 */

trait Dependency[+S <: AnyRef, +T <:AnyRef]
{
    type SourceType = S

    type TargetType = T

    val source : S

    val target : T
}