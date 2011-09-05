package unisson

import ast._
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model._

/**
 *
 * Author: Ralf Mitschke
 * Created: 05.09.11 09:59
 *
 */

object Utilities
{

    def ensembleToString(ensemble: Ensemble)(implicit delimiter: String, checker: ArchitectureChecker) =
    {

        val outgoing = ensemble.outgoingConnections.collect(
                (c: DependencyConstraint) => c match {
                case OutgoingConstraint(
                _,
                targets,
                kind
                ) => "outgoing" + delimiter + kind + delimiter + ensembleListToString(targets) + delimiter + checker.violations(
                    c
                ).size
                case NotAllowedConstraint(
                _,
                target,
                kind
                ) => "not_allowed" + delimiter + kind + delimiter + target.name + delimiter + checker.violations(c).size
                case ExpectedConstraint(
                _,
                target,
                kind
                ) => "expected" + delimiter + kind + delimiter + target.name + delimiter + checker.violations(c).size
            }
        )

        val incoming = ensemble.incomingConnections.collect(
                (c: DependencyConstraint) => c match {
                case IncomingConstraint(
                sources,
                _,
                kind
                ) => "incoming" + delimiter + kind + delimiter + ensembleListToString(sources) + delimiter + checker.violations(
                    c
                ).size
                case NotAllowedConstraint(
                source,
                _,
                kind
                ) => "not_allowed" + delimiter + kind + delimiter + source.name + delimiter + checker.violations(c).size
                case ExpectedConstraint(
                source,
                _,
                kind
                ) => "expected" + delimiter + kind + delimiter + source.name + delimiter + checker.violations(c).size
            }
        )

        val result = ensemble.name + delimiter + checker.ensembleElements(ensemble).size +
                (
                        if (!outgoing.isEmpty) {
                            outgoing.reduceRight(delimiter + delimiter + _ + "\n" + _)
                        } else {
                            ""
                        }
                        ) +
                (
                        if (!incoming.isEmpty) {
                            incoming.reduceRight(delimiter + delimiter + _ + "\n" + _)
                        }
                        else {
                            ""
                        }
                        )

        result
    }

    def ensembleListToString(ensembles: Seq[Ensemble]): String =
    {
        ensembles.map(_.name).reduceLeft(_ + " | " + _)
    }

    /**
     * Converts the violation to a delimited string
     * sourceEnsemble | targetEnsemble | constraintType| violationType | sourceElement | targetElement
     */
    def violationToString(violation: Violation)(implicit delimiter: String, checker: ArchitectureChecker): String =
    {
        val data = violation match {
            case Violation(
            source,
            sourceElement,
            target,
            targetElement,
            constraint,
            kind
            ) => List(
                if (source == None) {
                    ensembleListToString(ensmblesForElement(sourceElement))
                } else {
                    source.get.name
                },
                if (target == None) {
                    ensembleListToString(ensmblesForElement(targetElement))
                } else {
                    target.get.name
                },
                constraintType(constraint),
                kind,
                elementToString(sourceElement),
                elementToString(targetElement)
            )
        }
        data.foldRight("")(_ + delimiter + _)
    }


    def constraintType(constraint: DependencyConstraint): String =
    {
        constraint match {
            case NotAllowedConstraint(_, _, kind) => "not_allowed(" + kind + ")"
            case ExpectedConstraint(_, _, kind) => "expected(" + kind + ")"
            case IncomingConstraint(_, _, kind) => "incoming(" + kind + ")"
            case OutgoingConstraint(_, _, kind) => "outgoing(" + kind + ")"
        }
    }

    def elementToString[T](elem: SourceElement[T])(implicit delimiter: String): String =
    {
        elem match {
            case SourceElement(ObjectType(name)) => "class" + delimiter + name
            case SourceElement(
            Method(
            decl,
            name,
            params,
            ret
            )
            ) => "method" + delimiter + decl.toJava + "." + name + "(" + (params.foldLeft("")(_ + ", " + _.toJava)).drop(
                2
            ) + ")" + ": " + ret.toJava

            case SourceElement(
            Field(
            decl,
            name,
            typ
            )
            ) => "field" + delimiter + decl.toJava + "." + name + ": " + typ.toJava
        }
    }

    def ensmblesForElement(sourceElement: SourceElement[AnyRef])(implicit checker: ArchitectureChecker): Seq[Ensemble] =
    {

        val ensembles = checker.getEnsembles.filter(
                (ensemble: Ensemble) => {
                val elements = checker.ensembleElements(ensemble);
                if (elements.contains(sourceElement)) {
                    true
                }
                else {
                    false
                }
            }
        )

        if (!ensembles.isEmpty) {
            ensembles.toList
        }
        else {
            Set(CloudEnsemble).toList
        }
    }
}