package unisson.model

import de.tud.cs.st.vespucci.interfaces.{IEnsemble, IConstraint}
import unisson.query.code_model.SourceElementFactory
import de.tud.cs.st.vespucci.interfaces.{IViolationSummary, IViolation, ICodeElement}

/**
 *
 * Author: Ralf Mitschke
 * Date: 18.01.12
 * Time: 16:51
 *
 */
object UnissonOrdering
{

    implicit def violationSummaryOrdering(implicit
                                   constraintOrdering: Ordering[IConstraint],
                                   ensembleOrdering: Ordering[IEnsemble],
                                   elementOrdering: Ordering[ICodeElement]
                                          ): Ordering[IViolationSummary] = {
        new Ordering[IViolationSummary]
        {
            def compare(x: IViolationSummary, y: IViolationSummary): Int = {
                val constraintOrder = constraintOrdering.compare(x.getConstraint, y.getConstraint)
                if (constraintOrder != 0) return constraintOrder

                val sourceEnsembleOrder = ensembleOrdering.compare(x.getSourceEnsemble, y.getSourceEnsemble)
                if (sourceEnsembleOrder != 0) return sourceEnsembleOrder

                val targetEnsembleOrder = ensembleOrdering.compare(x.getTargetEnsemble, y.getTargetEnsemble)
                if (targetEnsembleOrder != 0) return targetEnsembleOrder

                x.getDiagramFile.compareTo(y.getDiagramFile)
            }
        }
    }

    implicit def violationOrdering(implicit
                                   constraintOrdering: Ordering[IConstraint],
                                   ensembleOrdering: Ordering[IEnsemble],
                                   elementOrdering: Ordering[ICodeElement]
                                          ): Ordering[IViolation] = {
        new Ordering[IViolation]
        {
            def compare(x: IViolation, y: IViolation): Int = {
                val constraintOrder = constraintOrdering.compare(x.getConstraint, y.getConstraint)
                if (constraintOrder != 0) return constraintOrder
                
                val sourceEnsembleOrder = ensembleOrdering.compare(x.getSourceEnsemble, y.getSourceEnsemble)
                if (sourceEnsembleOrder != 0) return sourceEnsembleOrder

                val targetEnsembleOrder = ensembleOrdering.compare(x.getTargetEnsemble, y.getTargetEnsemble)
                if (targetEnsembleOrder != 0) return targetEnsembleOrder

                val sourceElementOrder = elementOrdering.compare(x.getSourceElement, y.getSourceElement)
                if (sourceElementOrder != 0) return sourceElementOrder

                val targetElementOrder = elementOrdering.compare(x.getTargetElement, y.getTargetElement)
                if (targetElementOrder != 0) return targetElementOrder

                val kindOrder = x.getViolatingKind.compare(y.getViolatingKind)
                if (kindOrder != 0) return kindOrder
                
                x.getDiagramFile.compareTo(y.getDiagramFile)
            }
        }
    }

    implicit def constraintOrdering(implicit ensembleOrdering: Ordering[IEnsemble]): Ordering[IConstraint] = {
        new Ordering[IConstraint]
        {
            def compare(x: IConstraint, y: IConstraint): Int = {
                val sourceOrder = ensembleOrdering.compare(x.getSource, y.getSource)
                if (sourceOrder != 0) return sourceOrder

                val targetOrder = ensembleOrdering.compare(x.getTarget, y.getTarget)
                if (targetOrder != 0) return targetOrder

                x.getDependencyKind.compare(y.getDependencyKind)
            }
        }
    }

    implicit def ensembleOrdering: Ordering[IEnsemble] = new Ordering[IEnsemble] {
        def compare(x: IEnsemble, y: IEnsemble): Int =
            x.getName.compare(y.getName)
    }

    implicit def elementOrdering: Ordering[ICodeElement] = new Ordering[ICodeElement] {
        def compare(x: ICodeElement, y: ICodeElement): Int = {
            val pnCompare = x.getPackageIdentifier.compareTo(y.getPackageIdentifier)
            if (pnCompare != 0) return pnCompare
            val snCompare = x.getSimpleClassName.compareTo(y.getSimpleClassName)
            if (snCompare != 0) return snCompare
            x.toString.compareTo(y.toString)
        }
    }

    implicit def sourceElementOrdering = SourceElementFactory.ordering
}