package unisson.queries

import sae.bytecode.BytecodeDatabase
import unisson.ast._
import unisson.{Violation, ArchitectureChecker, Queries, SourceElement}
import sae.bytecode.model.dependencies._
import sae.collections.{BagResult, QueryResult}
import sae.{Observer, LazyView}
import sae.functions.Count
import sae.operators.{Aggregation, NotSelfMaintainalbeAggregateFunction}
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model._
import java.lang.IllegalArgumentException
import unisson.model.kinds.DependencyKind

/**
 * 
 * Author: Ralf Mitschke
 * Created: 31.08.11 09:17
 *
 */

class QueryCompiler(val checker : ArchitectureChecker)
{

    val queries = new Queries(checker.db)

    import queries._

    def addAll(definitions : Seq[UnissonDefinition])
    {
        definitions.map(add(_))
    }

    def add(definition : UnissonDefinition)
    {
        definition match {
            case e @ Ensemble(_,_,_,_) => existingQuery(e).getOrElse(createEnsembleQuery(e))
            case inc @ IncomingConstraint(_,_,_) => existingQuery(inc).getOrElse(createIncomingQuery(inc))
            case out @ OutgoingConstraint(_,_,_) => existingQuery(out).getOrElse(createOutgoingQuery(out))
            case not @ NotAllowedConstraint(_,_,_) => existingQuery(not).getOrElse(createNotAllowedQuery(not))
            case exp @ ExpectedConstraint(_,_,_) => existingQuery(exp).getOrElse(createExpectedQuery(exp))
        }
    }

    /**
     * call this method to indicate that no more ensembles are forthcoming
     * This is required to close the Outgoing constraints
     */
    def finishOutgoing() {

        import sae.syntax.RelationalAlgebraSyntax._
        // all outgoing constraints that did not yet have this ensemble included need to append this ensemble


        val outgoingConstraints = checker.getConstraints.collect{case out : OutgoingConstraint => out}

        for( out <- outgoingConstraints )
        {
            val dependencyRelation = kindAsDependency(out.kind)
            val otherEnsembles =
                for{ ensemble <- checker.getEnsembles
                     if(out.source != ensemble)
                     if(!out.targets.contains(ensemble))
                     if(!out.source.allDescendents.contains(ensemble))
                }
                    yield existingQuery(ensemble).get

            val restQuery = otherEnsembles.foldLeft[QueryResult[SourceElement[AnyRef]]](new ArchitectureChecker.EmptyResult[SourceElement[AnyRef]])(_ ∪ _)

            val inAnyEnsemble = ((dependencyRelation, Queries.target(_)) ⋉ (identity(_:SourceElement[AnyRef]), restQuery))
            val oldResult = checker.violations(out).asInstanceOf[BagResult[Violation]]
            oldResult.relation.removeObserver(oldResult)
            val newQuery = oldResult.relation match
            {
                case p @ Π(func,oldQuery:LazyView[Dependency[AnyRef, AnyRef]]) =>
                    {
                        p.relation.removeObserver(p.asInstanceOf[Observer[Dependency[AnyRef, AnyRef]]])
                        Π(func)(oldQuery ∩ inAnyEnsemble)
                    }
            }

            checker.updateConstraint(out, newQuery)
        }


        val allElements = Π(new SourceElement[AnyRef](_:ObjectType))(db.classfiles) ∪ Π(new SourceElement[AnyRef](_:Method))(db.classfile_methods) ∪ Π(new SourceElement[AnyRef](_:Field))(db.classfile_fields)

        val allEnsembles =
                for(ensemble <- checker.getEnsembles)
                    yield checker.ensembleElements(ensemble)




        val restQuery : QueryResult[SourceElement[AnyRef]] = allEnsembles.foldLeft[LazyView[SourceElement[AnyRef]]](new ArchitectureChecker.EmptyResult[SourceElement[AnyRef]])(_ ∪ _)
        val notInAnyEnsemble = (allElements, identity(_:SourceElement[AnyRef])) ⊳ (identity(_:SourceElement[AnyRef]), restQuery)

        checker.addEnsemble(CloudEnsemble, allElements)
        checker.addEnsemble(RestEnsemble, notInAnyEnsemble)



    }

    def createEnsembleQuery(ensemble:Ensemble): LazyView[SourceElement[AnyRef]] =
    {
        val query = compileUnissonQuery(ensemble.query)
        checker.addEnsemble(ensemble, query)
        query
    }

    def compileUnissonQuery(query : UnissonQuery) : LazyView[SourceElement[AnyRef]] =
    {
        query match
        {
            case ClassSelectionQuery(pn, sn) => `class`(pn, sn)
            case ClassQuery(classQuery) => `class`(compileUnissonQuery(classQuery))
            case ClassWithMembersQuery(ClassSelectionQuery(pn, sn)) => class_with_members(pn, sn)
            case ClassWithMembersQuery(classQuery) => class_with_members(compileUnissonQuery(classQuery))
            case PackageQuery(pn) => `package`(pn)
            case OrQuery(left, right) => compileUnissonQuery(left) or compileUnissonQuery(right)
            case WithoutQuery(left, right) => compileUnissonQuery(left) without compileUnissonQuery(right)
            case TransitiveQuery(SuperTypeQuery(innerQuery)) => transitive_supertype(compileUnissonQuery(innerQuery))
            case SuperTypeQuery(innerQuery) => supertype(compileUnissonQuery(innerQuery))
            case EmptyQuery() => new ArchitectureChecker.EmptyResult[SourceElement[AnyRef]]()
            case _ => throw new IllegalArgumentException("Unknown query type: " + query)
        }
    }

    def createIncomingQuery(constraint:IncomingConstraint): LazyView[Violation] =
    {
        import sae.syntax.RelationalAlgebraSyntax._
        val dependencyRelation = kindAsDependency(constraint.kind)
        val targetQuery =  existingQuery(constraint.target).getOrElse(createEnsembleQuery(constraint.target))

        var query = ( (dependencyRelation, Queries.target(_)) ⋉ (identity(_:SourceElement[AnyRef]), targetQuery) ) ∩
        ( (dependencyRelation, Queries.source(_)) ⊳ (identity(_:SourceElement[AnyRef]), targetQuery) )

        constraint.sources.foreach(
            (source : Ensemble) =>
            {
                val sourceQuery = existingQuery(source).getOrElse(createEnsembleQuery(source))
                query = query ∩
                ((dependencyRelation, Queries.source(_)) ⊳ (identity(_:SourceElement[AnyRef]), sourceQuery))
            }
        )

        // TODO currently we do not resolve sources as ensembles for the constraint.
        // potentially the element can belong to more than one ensemble
        val violations = Π( (d:Dependency[AnyRef, AnyRef]) => Violation( None, Queries.source(d), Some(constraint.target), Queries.target(d), constraint, dependencyAsKind(d)) )(query)

        checker.addConstraint(constraint, violations)
        violations
    }

    def createOutgoingQuery(constraint:OutgoingConstraint): LazyView[Violation] =
    {
        import sae.syntax.RelationalAlgebraSyntax._
        val dependencyRelation = kindAsDependency(constraint.kind)
        val sourceQuery =  existingQuery(constraint.source).getOrElse(createEnsembleQuery(constraint.source))

        var query = ( (dependencyRelation, Queries.source(_)) ⋉ (identity(_:SourceElement[AnyRef]), sourceQuery) ) ∩
        ( (dependencyRelation, Queries.target(_)) ⊳ (identity(_:SourceElement[AnyRef]), sourceQuery) )

        constraint.targets.foreach(
            (target : Ensemble) =>
            {
                val targetQuery = existingQuery(target).getOrElse(createEnsembleQuery(target))
                query = query ∩
                ((dependencyRelation, Queries.target(_)) ⊳ (identity(_:SourceElement[AnyRef]), targetQuery))
            }
        )


        // TODO currently we do not resolve targets as ensembles for the constraint.
        // potentially the element can belong to more than one ensemble
        val violations = Π( (d:Dependency[AnyRef, AnyRef]) => Violation( Some(constraint.source), Queries.source(d), None, Queries.target(d), constraint, dependencyAsKind(d)) )(query)

        checker.addConstraint(constraint, violations)
        violations
    }

    def createNotAllowedQuery(constraint:NotAllowedConstraint)
    {

        import sae.syntax.RelationalAlgebraSyntax._
        val sourceQuery =  existingQuery(constraint.source).getOrElse(createEnsembleQuery(constraint.source))

        val targetQuery =  existingQuery(constraint.target).getOrElse(createEnsembleQuery(constraint.target))

        val dependencyRelation = kindAsDependency(constraint.kind)

        val query = ( (dependencyRelation, Queries.source(_)) ⋉ (identity(_:SourceElement[AnyRef]), sourceQuery) ) ∩
        ( (dependencyRelation, Queries.target(_)) ⋉ (identity(_:SourceElement[AnyRef]), targetQuery) )

        val violations = Π( (d:Dependency[AnyRef, AnyRef]) => Violation( Some(constraint.source), Queries.source(d), Some(constraint.target), Queries.target(d), constraint, dependencyAsKind(d)) )(query)

        checker.addConstraint(constraint, violations)
    }

    def createExpectedQuery(constraint:ExpectedConstraint)
    {
        import sae.syntax.RelationalAlgebraSyntax._
        val sourceQuery =  existingQuery(constraint.source).getOrElse(createEnsembleQuery(constraint.source))

        val targetQuery =  existingQuery(constraint.target).getOrElse(createEnsembleQuery(constraint.target))


        val dependencyRelation = kindAsDependency(constraint.kind)

        val sourceToTargetDep = ( (dependencyRelation, Queries.source(_)) ⋉ (identity(_:SourceElement[AnyRef]), sourceQuery) ) ∩
        ( (dependencyRelation, Queries.target(_)) ⋉ (identity(_:SourceElement[AnyRef]), targetQuery) )

        // TODO this is not so nice, I can not have a LazyView of any thus I can not select the count(*)
        val aggregation = Aggregation[Dependency[AnyRef, AnyRef], None.type, Int](sourceToTargetDep,
                (newD : Dependency[AnyRef, AnyRef]) => None,
                Count[Dependency[AnyRef, AnyRef]]()
        )

        val countQuery = σ( (_:(None.type, Int))._2 == 0 )(aggregation)


        val violations = Π( (i:(None.type, Int)) => Violation( Some(constraint.source), null, Some(constraint.target), null, constraint, "none") )(countQuery)

        checker.addConstraint(constraint, violations)

    }

/**
 * The following dependency kinds are supported
 *  extends(Class1, Class2)
 *  implements(Class1, Class2)
 *  field_type(Field, Class)
 *  parameter(Method, Class)
 *  return_type(Method, Class)
 *  write_field(Method, Field)
 *  read_field(Method, Field)
 *  class_cast(Method, Class)
 *  instanceof(Method, Class)
 *  create(Method, Class)
 *  create_class_array(Method, Class)
 *  throw(Method, Class)
 *
 *  // TODO implement the following
 *  get_class(Method, Class)
 *  annotation(Class|Field|Method, Class)
 *  parameter_annotation(Method, Class)
 *  calls(Method1, Method2) =
 *  signature
 */
    private def kindAsDependency(kind : DependencyKind) : LazyView[Dependency[AnyRef, AnyRef]] =
    {
        import sae.syntax.RelationalAlgebraSyntax._
        // TODO we can do the match by kind tpye now, not by string
        kind.designator match
        {

            case "all" => checker.db.dependency.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "calls" => checker.db.calls.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "subtype" => checker.db.`extends`.∪[Dependency[AnyRef, AnyRef], implements] (checker.db.implements)
            case "signature" => checker.db.parameter.∪[Dependency[AnyRef, AnyRef], return_type] (checker.db.return_type)
            case "extends" => checker.db.`extends`.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "implements" => checker.db.implements.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "field_type" => checker.db.field_type.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "parameter" => checker.db.parameter.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "return_type" => checker.db.return_type.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "write_field" => checker.db.write_field.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "read_field" => checker.db.read_field.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "invoke_interface" => checker.db.invoke_interface.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "invoke_special" => checker.db.invoke_special.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "invoke_static" => checker.db.invoke_static.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "invoke_virtual" => checker.db.invoke_virtual.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "instanceof" => checker.db.instanceof.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "create" => checker.db.create.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "create_class_array" => checker.db.create_class_array.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "class_cast" => checker.db.class_cast.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "throws" => checker.db.thrown_exceptions.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            //case "exception" => checker.db.handled_exceptions.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
        }
    }


    private def dependencyAsKind(d:Dependency[AnyRef, AnyRef]) : String =
    {
        d match
        {
            case `extends`(_,_) => "extends"
            case implements(_,_) => "implements"
            case field_type(_,_) => "field_type"
            case parameter(_,_) => "parameter"
            case return_type(_,_) => "return_type"
            case write_field(_,_,_) => "write_field"
            case read_field(_,_,_) => "read_field"
            case invoke_interface(_,_) => "invoke_interface"
            case invoke_special(_,_) => "invoke_special"
            case invoke_static(_,_) => "invoke_static"
            case invoke_virtual(_,_) => "invoke_virtual"
            case instanceof(_,_) => "instanceof"
            case create(_,_) => "create"
            case create_class_array(_,_) => "create_class_array"
            case class_cast(_,_) => "class_cast"
            case throws(_,_) => "throws"
            case inner_class(_,_,_,_) => "inner_class"
            case handled_exception(_,_) => "handled_exception"
            case _ => throw new IllegalArgumentException("Unknown dependency kind + " + d)
        }
    }

    private def existingQuery(c : DependencyConstraint) : Option[LazyView[Violation]] =
    {

        if( checker.hasConstraint(c) )
        {
            Some(checker.violations(c))
        }
        else{
            None
        }
    }

    private def existingQuery(e : Ensemble) : Option[LazyView[SourceElement[AnyRef]]] =
    {

        if( checker.hasEnsemble(e) )
        {
            Some(checker.ensembleElements(e))
        }
        else{
            None
        }
    }
}

