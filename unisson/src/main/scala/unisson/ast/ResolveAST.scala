package unisson.ast

import unisson.model.kinds.{ResolveKinds, DependencyKind}

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 15:25
 * Resolves entities in the ast to build a coherent object graph.
 * I.e. ensembles know their dependencies and vice versa.
 * The resolves state is mutable!
 */

object ResolveAST
{


    def apply(elements: Seq[UnissonDefinition]): Seq[UnissonDefinition] =
    {
        val ensembles = resolveEnsembleHierarchy(
            elements.collect {
                case e: UnresolvedEnsemble => e
            }
        )

        val constraints = resolveConstraintEdges(
            elements.collect {
                case e: DependencyConstraintEdge => e
            },
            ensembles
        )

        ensembles ++ constraints
    }

    /**
     *
     */
    private def resolveEnsembleHierarchy(implicit unresolved: Seq[UnresolvedEnsemble]): Seq[Ensemble] =
    {
        val allChildren = (for {
            e <- unresolved
            sub <- e.subEnsembleNames
        } yield sub).distinct

        val topLevel = for {
            e <- unresolved
            if (!allChildren.contains(e.name))
        } yield resolveChildreRec(e)

        topLevel ++ collectAllChildren(topLevel)
    }


    private def collectAllChildren(ensembles: Seq[Ensemble]): Seq[Ensemble] =
    {
        ensembles.flatMap((e: Ensemble) => e.children ++ collectAllChildren(e.children))
    }


    private def resolveChildreRec(unresolved: UnresolvedEnsemble)(implicit allUnresolved: Seq[UnresolvedEnsemble]): Ensemble =
    {
        val children: Seq[Ensemble] = allUnresolved.filter(
                (e: UnresolvedEnsemble) =>
                unresolved.subEnsembleNames.contains(e.name)
        ).map(resolveChildreRec)
        Ensemble(
            unresolved.name,
            if (unresolved.query == DerivedQuery()) {
                children.foldLeft[UnissonQuery](EmptyQuery())((q: UnissonQuery, e: Ensemble) => OrQuery(q, e.query))
            }
            else {
                unresolved.query
            },
            Nil,
            children
        )
    }


    private def resolveConstraintEdges(implicit edges: Seq[DependencyConstraintEdge], ensembles: Seq[Ensemble]): Seq[DependencyConstraint] =
    {
        val outgoing = for {ensemble <- ensembles
                            constraint <- resolveConstraints(
                            {
                                // my own outgoing constraints
                                case OutgoingConstraintEdge(_, ensemble.name, _, _, _, _) => true
                                // my parents outgoing constraints
                                case OutgoingConstraintEdge(_, someName, _, _, _, _)
                                    if (ensemble.allAncestors.map(_.name).contains(someName)) => true
                            },
                                (kinds: Seq[String], constraints: Seq[DependencyConstraintEdge]) =>
                                for (kind <- removeSubsumptions(resolveKinds(kinds)))
                                yield
                                {
                                    val c = OutgoingConstraint(
                                        ensemble,
                                        for {
                                            target <- ensembles
                                            out <- constraints
                                            if (target.name == out.targetName)
                                            if (out.kinds.contains(kind.designator))
                                        } yield target
                                        ,
                                        kind
                                    )
                                    c.origins = constraints
                                    c
                                }
                            )
        } yield constraint

        val incoming = for {ensemble <- ensembles
                            constraint <- resolveConstraints(
                            {
                                case IncomingConstraintEdge(_, _, _, ensemble.name, _, _) => true
                            },
                                (kinds: Seq[String], constraints: Seq[DependencyConstraintEdge]) =>
                                for (kind <- normalizeKinds(resolveKinds(kinds)))
                                yield
                                {
                                    val c = IncomingConstraint(
                                        for {
                                            source <- ensembles
                                            in <- constraints
                                            if (source.name == in.sourceName)
                                            if (in.kinds.exists((s: String) => kind.isCompatibleKind(ResolveKinds(s))))
                                        } yield source
                                        , ensemble,
                                        kind
                                    )
                                    c.origins = constraints
                                    c
                                }
                            )
        } yield constraint

        val not_allowed = for {source <- ensembles
                               target <- ensembles
                               constraint <- resolveConstraints(
                               {
                                   case NotAllowedConstraintEdge(_, source.name, _, target.name, _, _) => true
                               },
                                   (kinds: Seq[String], constraints: Seq[DependencyConstraintEdge]) =>
                                   for (kind <- resolveKinds(kinds))
                                   yield
                                {
                                    val c = NotAllowedConstraint(
                                           source,
                                           target,
                                           kind
                                    )
                                    c.origins = constraints
                                    c
                                }
                               )
        } yield constraint

        val expected = for {source <- ensembles
                            target <- ensembles
                            constraint <- resolveConstraints(
                            {
                                case ExpectedConstraintEdge(_, source.name, _, target.name, _, _) => true
                            },
                                (kinds: Seq[String], constraints: Seq[DependencyConstraintEdge]) =>
                                for (kind <- resolveKinds(kinds))
                                yield
                                {
                                    val c = ExpectedConstraint(
                                        source,
                                        target,
                                        kind
                                    )
                                    c.origins = constraints
                                    c
                                }
                            )
        } yield constraint

        outgoing ++ incoming ++ not_allowed ++ expected
    }

    /**
     * provide:
     * - a function that tells us how to identify the relevant edges
     */
    private def resolveConstraints(
                                      isRelevant: PartialFunction[DependencyConstraintEdge, Boolean],
                                      createDependencies: (Seq[String], Seq[DependencyConstraintEdge]) => Seq[DependencyConstraint]
                                  )
                                  (
                                      implicit allEdges: Seq[DependencyConstraintEdge]
                                  ): Seq[DependencyConstraint] =
    {
        val relevant = allEdges.filter(
            isRelevant.orElse {
                case _ => false
            }
        )
        createDependencies(relevant.flatMap(_.kinds).distinct, relevant)
    }


    /**
     * resolve the kinds by removing subsumptions and returning a list of kinds
     * that have pairwise non-overlapping regions for all pairs.
     * I.e. for all pairs (A,B) the following holds: A ∪ B != A and A ∪ B != B
     */
    private def removeSubsumptions(resolved: Seq[DependencyKind]): Seq[DependencyKind] =
    {
        val subsumptions = for (k <- resolved;
                                y <- resolved;
                                if (k.isSubKindOf(y))
        ) yield (k, y)
        val result = resolved.filterNot(
                (k: DependencyKind) => subsumptions.exists {
                case (x, _) if x == k => true
            }
        )
        result
    }

    /**
     * TODO
     * Currently it is not performant to normalize every "all" relation.
     * This will only be possible once queries are optimized.
     * Thus we check first whether normalization is required.
     */
    def normalizeKinds(resolved: Seq[DependencyKind]): Seq[DependencyKind] =
    {
        var needsNormalize = false
        for (kind <- resolved; if (resolved.exists((_: DependencyKind).isSubKindOf(kind)))) {
            needsNormalize = true
        }
        if (needsNormalize) {
            val result = ((
                    for (kind <- resolved; node <- kind.descendents; if (node.children == Nil)) yield node
                    ) ++
                    (
                            for (kind <- resolved; if (kind.children == Nil)) yield kind
                            )).distinct
            result
        }
        else {
            resolved
        }
    }

    /**
     * resolve the kinds from strings to a sequence of kind types
     */
    private def resolveKinds(kinds: Seq[String]): Seq[DependencyKind] =
    {
        for (k <- kinds) yield ResolveKinds(k)
    }
}