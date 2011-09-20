package unisson

import ast._
import sae.bytecode.model._
import de.tud.cs.st.bat.{Type, ObjectType}

/**
 *
 * Author: Ralf Mitschke
 * Created: 05.09.11 09:59
 *
 */

object Utilities
{

    /**
     * Given a delimiter (e.g. ;) returns a string in the form:
     * Ensemble;EnsembleElementCount
     */
    def ensembleToString(ensemble: Ensemble)(implicit delimiter: String, checker: ArchitectureChecker) =
    {
        ensemble.name + delimiter + checker.ensembleElements(ensemble).size
    }

    def ensembleListToString(ensembles: Seq[Ensemble]): String =
    {
        ensembles.map(_.name).reduceLeft(_ + " | " + _)
    }

    /**
     * Given a delimiter (e.g. ;) returns a string in the form:
     * Type;Kind;Source Ensembles(s);Target Ensembles(s);Violation Count
     */
    def constraintToString(constraint: DependencyConstraint)(implicit delimiter: String, checker: ArchitectureChecker) =
        constraint match {
            case IncomingConstraint(
            sources,
            target,
            kind
            ) => "incoming" + delimiter +
                    kind.designator + delimiter +
                    ensembleListToString(sources) + delimiter +
                    target.name + delimiter +
                    checker.violations(constraint).size
            case OutgoingConstraint(
            source,
            targets,
            kind
            ) => "outgoing" + delimiter +
                    kind.designator + delimiter +
                    source.name + delimiter +
                    ensembleListToString(targets) + delimiter +
                    checker.violations(constraint).size
            case NotAllowedConstraint(
            source,
            target,
            kind
            ) => "not_allowed" + delimiter +
                    kind.designator + delimiter +
                    source.name + delimiter +
                    target.name + delimiter +
                    checker.violations(constraint).size
            case ExpectedConstraint(
            source,
            target,
            kind
            ) => "expected" + delimiter +
                    kind.designator + delimiter +
                    source.name + delimiter +
                    target.name + delimiter +
                    checker.violations(constraint).size
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
            case SourceElement(t: Type) => "type" + delimiter + t.toJava
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
                if (!ensemble.name.startsWith("@") && elements.contains(sourceElement)) {
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