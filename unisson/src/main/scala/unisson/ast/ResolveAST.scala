package unisson.ast

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
                            res <- resolveConstraints(
                            {
                                case OutgoingConstraintEdge(_, ensemble.name, _, _, _, _) => true
                                //case OutgoingConstraintEdge(_, someName, _, _, _, _) if(ensemble.allAncestors.map(_.name).contains(someName) ) => true
                            },
                                (kind: String, constraints: Seq[DependencyConstraintEdge]) =>
                                OutgoingConstraint(
                                    ensemble,
                                    for {
                                        target <- ensembles
                                        out <- constraints
                                        if (target.name == out.targetName)
                                        if (out.kinds.contains(kind))
                                    } yield target
                                    ,
                                    kind
                                )
                            )
        } yield res

        val incoming = for {ensemble <- ensembles
                            res <- resolveConstraints(
                            {
                                case IncomingConstraintEdge(_, _, _, ensemble.name, _, _) => true
                            },
                                (kind: String, constraints: Seq[DependencyConstraintEdge]) =>
                                IncomingConstraint(
                                    for {
                                        source <- ensembles
                                        in <- constraints
                                        if (source.name == in.sourceName)
                                        if (in.kinds.contains(kind))
                                    } yield source
                                    , ensemble,
                                    kind
                                )
                            )
        } yield res

        val not_allowed = for {source <- ensembles
                               target <- ensembles
                               res <- resolveConstraints(
                               {
                                   case NotAllowedConstraintEdge(_, source.name, _, target.name, _, _) => true
                               },
                                   (kind: String, constraints: Seq[DependencyConstraintEdge]) =>
                                   NotAllowedConstraint(
                                       source,
                                       target,
                                       kind
                                   )
                               )
        } yield res

        val expected = for {source <- ensembles
                            target <- ensembles
                            res <- resolveConstraints(
                            {
                                case ExpectedConstraintEdge(_, source.name, _, target.name, _, _) => true
                            },
                                (kind: String, constraints: Seq[DependencyConstraintEdge]) =>
                                ExpectedConstraint(
                                    source,
                                    target,
                                    kind
                                )
                            )
        } yield res

        outgoing ++ incoming ++ not_allowed ++ expected
    }

    /**
     * provide:
     * - a function that tells us how to identify the relevant edges
     */
    private def resolveConstraints(
                                      isRelevant: PartialFunction[DependencyConstraintEdge, Boolean],
                                      createDependency: (String, Seq[DependencyConstraintEdge]) => DependencyConstraint
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
        for (kind <- relevant.flatMap(_.kinds).distinct
        ) yield createDependency(kind, relevant)
    }

}