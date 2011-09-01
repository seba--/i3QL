package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 15:25
 * Resolves entities in the ast to build a coherent object graph.
 * I.e. ensembles know their dependencies and vice versa.
 * The resolves state is mutable!
 */

object ResolveAST {


    def apply(elements: Seq[UnissonDefinition]) : Seq[UnissonDefinition] =
    {
        elements.foreach(resolve(_, elements))
        val filtered = elements.filter( _ match
            {
                case i:DependencyConstraintEdge => false
                case _=> true
            }
        )

        filtered.flatMap( _ match
            {
                case e: Ensemble => resolveEdges(e) :+ e
                case elem => List(elem)
            }
        )
    }


    private def resolveEdges(ensemble :Ensemble) : Seq[UnissonDefinition] =
    {
        val outgoing = ensemble.outgoingConnections.collect{ case i:OutgoingConstraintEdge => i }

        val incoming = ensemble.incomingConnections.collect{ case i:IncomingConstraintEdge => i }

        val not_allowed = ensemble.outgoingConnections.collect{ case i:NotAllowedConstraintEdge => i }

        val expected = ensemble.outgoingConnections.collect{ case i:ExpectedConstraintEdge => i }

        ensemble.outgoingConnections = ensemble.outgoingConnections.filter( _ match
            {
                // all edges must be removed from both sides
                case i:DependencyConstraintEdge => false
                case _=> true
            }
        )

        ensemble.incomingConnections = ensemble.incomingConnections.filter( _ match
            {
                // all edges must be removed from both sides
                case i:DependencyConstraintEdge => false
                case _=> true
            }
        )

        val resolvedOutgoingDependencies =
            for( kind <- outgoing.flatMap( _.kinds ).distinct )
                yield
                    OutgoingConstraint(ensemble,
                            for{
                                out <- outgoing
                                if( out.kinds.contains(kind) )
                                target <- out.target
                            } yield target
                            ,
                            kind
                    )

        val resolvedNotAllowedDependencies =
            for( constraint <- not_allowed; kind <- constraint.kinds)
                yield
                    NotAllowedConstraint(constraint.source.get,
                                    constraint.target.get,
                            kind
                    )

        val resolvedExpectedDependencies =
            for( constraint <- expected; kind <- constraint.kinds)
                yield
                    ExpectedConstraint(constraint.source.get,
                                    constraint.target.get,
                            kind
                    )

        ensemble.outgoingConnections = ensemble.outgoingConnections ++ resolvedOutgoingDependencies ++ resolvedNotAllowedDependencies ++ resolvedExpectedDependencies

        for{ con <- resolvedOutgoingDependencies
               target <- con.targets
        }{
            target.incomingConnections = target.incomingConnections :+ con
        }

        for{ con <- resolvedNotAllowedDependencies
        }{
            con.target.incomingConnections = con.target.incomingConnections :+ con
        }

        for{ con <- resolvedExpectedDependencies
        }{
            con.target.incomingConnections = con.target.incomingConnections :+ con
        }

        val resolvedIncoming = for( kind <- incoming.flatMap( _.kinds ).distinct )
            yield IncomingConstraint(
                            for{
                                in <- incoming
                                if( in.kinds.contains(kind) )
                                source <- in.source
                            } yield source
                            , ensemble,
                            kind
                    )

        ensemble.incomingConnections = ensemble.incomingConnections ++ resolvedIncoming

        for{ con <- resolvedIncoming
               source <- con.sources
        }{
            source.outgoingConnections = source.outgoingConnections :+ con
        }

        resolvedIncoming ++ resolvedOutgoingDependencies ++ resolvedNotAllowedDependencies ++ resolvedExpectedDependencies
    }


    private def resolve(elem : UnissonDefinition, rest: Seq[UnissonDefinition])
    {
        elem match {
            case e: Ensemble => resolveEnsemble(e, rest)
            case d: DependencyConstraintEdge => resolveConstraint(d, rest)

        }
    }

    private def resolveEnsemble(ensemble : Ensemble, rest: Seq[UnissonDefinition])
    {

        for( elem <- rest )
        {
            if( elem.isInstanceOf[DependencyConstraintEdge])
            {
                connect(ensemble, elem.asInstanceOf[DependencyConstraintEdge])
            }
            if( elem.isInstanceOf[Ensemble])
            {
                connect(ensemble, elem.asInstanceOf[Ensemble])
            }
        }

    }

    private def resolveConstraint(constraint : DependencyConstraintEdge, rest: Seq[UnissonDefinition])
    {
        for( e @ Ensemble(_,_,_) <- rest)
        {
            connect(e, constraint)
        }
    }


    def connect(ensemble: Ensemble, constraint:DependencyConstraintEdge)
    {
        if( constraint.sourceName == ensemble.name )
        {
            constraint.source = Some(ensemble)

            if( !ensemble.outgoingConnections.contains(constraint) )
            {
                ensemble.outgoingConnections = ensemble.outgoingConnections :+ constraint
            }

        }
        else if( constraint.targetName == ensemble.name )
        {
            constraint.target = Some(ensemble)

            if( !ensemble.incomingConnections.contains(constraint) )
            {
                ensemble.incomingConnections = ensemble.incomingConnections :+ constraint
            }
        }
    }

    def connect(ensembleA: Ensemble, ensembleB: Ensemble)
    {
        if( ensembleA.subEnsembleNames.contains(ensembleB.name) )
        {
            if( !ensembleA.childEnsembles.contains(ensembleB) )
            {
                ensembleA.childEnsembles = ensembleA.childEnsembles :+ ensembleB
            }
            if( ensembleB.parentEnsemble != Some(ensembleA) )
            {
                ensembleB.parentEnsemble = Some(ensembleA)
            }
        }
        else
        if( ensembleB.subEnsembleNames.contains(ensembleA.name) )
        {
            if( !ensembleB.childEnsembles.contains(ensembleA) )
            {
                ensembleB.childEnsembles = ensembleB.childEnsembles :+ ensembleA
            }
            if( ensembleA.parentEnsemble != Some(ensembleB) )
            {
                ensembleA.parentEnsemble = Some(ensembleB)
            }
        }
    }
}