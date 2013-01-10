package unisson.model

import de.tud.cs.st.vespucci.interfaces.{IConstraint, IEnsemble, IArchitectureModel}

/**
 *
 * Author: Ralf Mitschke
 * Date: 04.07.12
 * Time: 15:36
 *
 */
trait IUnissonArchitectureModelDatabase extends IUnissonDatabase
{


    /**
     * Add all ensembles and constraints in the <code>model</code> to a slice identified to the models name.
     */
    def addSlice(model: IArchitectureModel) {
        import scala.collection.JavaConversions._
        addEnsemblesToSlice(model.getEnsembles)(model.getName)
        addConstraintsToSlice(model.getConstraints)(model.getName)
    }

    /**
     * Remove all ensembles and constraints in the <code>model</code> from the slice identified to the models name.
     */
    def removeSlice(model: IArchitectureModel) {
        import scala.collection.JavaConversions._
        removeEnsemblesFromSlice(model.getEnsembles)(model.getName)
        removeConstraintsFromSlice(model.getConstraints)(model.getName)
    }

    /**
     * Update all ensembles and constraints in the <code>oldModel</code> to the values in the <code>newModel</code>.
     */
    def updateSlice(oldModel: IArchitectureModel, newModel: IArchitectureModel) {
        import scala.collection.JavaConversions._

        val oldEnsembles = oldModel.getEnsembles
        // remove old Ensembles
        removeEnsemblesFromSlice(
            oldEnsembles.filterNot(
                (e: IEnsemble) => newModel.getEnsembles.exists(_.getName == e.getName)
            )
        )(oldModel.getName)

        // remove old constraints
        removeConstraintsFromSlice(oldModel.getConstraints.filterNot(
            (c: IConstraint) => newModel.getConstraints.exists(_ == c)
        ))(oldModel.getName)



        // update existing Ensembles
        for (oldE <- oldEnsembles;
             newE <- newModel.getEnsembles)
            if (oldE.getName == newE.getName
            ) {
                updateEnsembleInSlice(oldE, newE)(oldModel.getName)
            }
        // we currently do not update any constraints.
        // This would make sense only for constraint changes w.r.t. kinds,
        // but then removing the old constraint is probably as effective.

        // add new Ensembles
        addEnsemblesToSlice(newModel.getEnsembles.filterNot(
            (e: IEnsemble) => oldModel.getEnsembles.exists(_.getName == e.getName)
        ))(newModel.getName)


        // add new Constraints
        addConstraintsToSlice(newModel.getConstraints.filterNot(
            (c: IConstraint) => oldModel.getConstraints.exists(_ == c)
        ))(newModel.getName)

    }

    /**
     * Add all ensembles in the <code>model</code> and their children to the global list of defined ensembles.
     */
    def setRepository(model: IArchitectureModel) {
        import scala.collection.JavaConversions._
        for (ensemble <- model.getEnsembles) {
            addEnsemble(ensemble)
        }
    }

    /**
     * Remove all ensembles in the <code>model</code> and their children to the global list of defined ensembles.
     */
    def unsetRepository(model: IArchitectureModel) {
        import scala.collection.JavaConversions._

        for (ensemble <- model.getEnsembles) {
            removeEnsemble(ensemble)
        }
    }

    /**
     * Update all ensembles in the <code>model</code> and their children to the global list of defined ensembles.
     */
    def updateRepository(oldModel: IArchitectureModel, newModel: IArchitectureModel) {
        import scala.collection.JavaConversions._

        // remove old Ensembles
        removeEnsembles(
            oldModel.getEnsembles.filterNot(
                (e: IEnsemble) => newModel.getEnsembles.exists(_.getName == e.getName)
            )
        )
        // add new Ensembles
        addEnsembles(
            newModel.getEnsembles.filterNot(
                (e: IEnsemble) => oldModel.getEnsembles.exists(_.getName == e.getName)
            )
        )

        // update existing Ensembles
        for (oldE <- oldModel.getEnsembles;
             newE <- newModel.getEnsembles)
            if (oldE.getName == newE.getName
            ) {
                updateEnsemble(oldE, newE)
            }
    }
}