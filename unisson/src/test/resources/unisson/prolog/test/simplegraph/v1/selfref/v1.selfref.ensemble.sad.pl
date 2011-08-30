%------
% Prolog based representation of the Vespucci architecture diagram: D:/workspace/sae/unisson/src/test/resources/unisson/prolog/test/simplegraph/v1/selfref/v1.selfref.ensemble.sad
% Created by Vespucci, Technische Universität Darmstadt, Department of Computer Science
% www.opal-project.de

:- multifile ensemble/5.
:- multifile abstract_ensemble/5.
:- multifile outgoing/7.
:- multifile incoming/7.
:- multifile not_allowed/7.
:- multifile expected/7.
:- discontiguous ensemble/5.
:- discontiguous abstract_ensemble/5.
:- discontiguous outgoing/7.
:- discontiguous incoming/7.
:- discontiguous not_allowed/7.
:- discontiguous expected/7.

% Date <30/08/2011 14:43:41>.
%------

%------
%ensemble(File, Name, Ensemble Parameter, Query, SubEnsembles) :- Definition of an ensemble
%	File - The simple file name in which the ensemble is defined (e.g., 'Flashcards.sad')
%	Name - Name of the ensemble
%	Ensemble Parameter - Parameter of the ensemble
%	Query - Query that determines which source elements belong to the ensemble
%	SubEnsembles - List of all sub ensembles of this ensemble
%------
ensemble('v1.selfref.ensemble.sad', 'A', [], (class_with_members('opal.test.simplegraph.v1.selfref','A')), []).

%------
%DEPENDENCY(File, ID, SourceE, SourceE Parameter, TargetE, TargetE Parameter, Type) :- Definition of a dependency between two ensembles.
%	DEPENDENCY - The type of the dependency. Possible values: outgoing, incoming, expected, not_allowed
%	File - The simple file name in which the dependency is defined (e.g., 'Flashcards.sad')
%	ID - An ID identifying the dependency
%	SourceE - The source ensemble
%	SourceE Parameter - Parameter of the source ensemble
%	TargetE - The target ensemble
%	TargetE Parameter - Parameter of the target ensemble
%	Relation classifier - Kinds of uses-relation between source and target ensemble (all, field_access, method_call,...)
%------
