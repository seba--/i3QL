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

    def apply(elem : UnissonDefinition, rest: Seq[UnissonDefinition])
    {
        elem match {
            case e: Ensemble => resolveEnsemble(e, rest)
            case d: DependencyConstraint => resolveConstraint(d, rest)

        }
    }

    private def resolveEnsemble(ensemble : Ensemble, rest: Seq[UnissonDefinition])
    {

        for( elem <- rest )
        {
            if( elem.isInstanceOf[DependencyConstraint])
            {
                connect(ensemble, elem.asInstanceOf[DependencyConstraint])
            }
            if( elem.isInstanceOf[Ensemble])
            {
                connect(ensemble, elem.asInstanceOf[Ensemble])
            }
        }

    }

    private def resolveConstraint(constraint : DependencyConstraint, rest: Seq[UnissonDefinition])
    {
        for( e @ Ensemble(_,_,_) <- rest)
        {
            connect(e, constraint)
        }
    }


    def connect(ensemble: Ensemble, constraint:DependencyConstraint)
    {
        if( constraint.sourceName == ensemble.name )
        {
            constraint.source = Some(ensemble)

            if( !ensemble.outgoingConstraints.contains(constraint) )
            {
                ensemble.outgoingConstraints = ensemble.outgoingConstraints :+ constraint
            }

        }
        else if( constraint.targetName == ensemble.name )
        {
            constraint.target = Some(ensemble)

            if( !ensemble.incomingConstraints.contains(constraint) )
            {
                ensemble.incomingConstraints = ensemble.incomingConstraints :+ constraint
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