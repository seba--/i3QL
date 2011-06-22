package sae.profiler.util

/**
 *
 * Author: Ralf Mitschke
 * Created: 17.06.11 11:17
 *
 * The basic query profile determines how many views of which kind are inside the query
 */
class BasicQueryProfile
{
    var materializedViews = 0

    var indexedViews = 0

    var baseRelations = 0

    var projections = 0

    var selections = 0

    var duplicateEliminations = 0

    var equiJoins = 0

    var unions = 0

    var intersections = 0

    var differences = 0

    def asConsoleOutput = "" +
            "materializedViews:     " + materializedViews + "\n" +
            "indexedViews:          " + indexedViews + "\n" +
            "baseRelations:         " + baseRelations + "\n" +
            "projections:           " + projections + "\n" +
            "selections:            " + selections + "\n" +
            "duplicateEliminations: " + duplicateEliminations + "\n" +
            "equiJoins:             " + equiJoins + ""
}

