%------
% Prolog based representation of the Vespucci architecture diagram: D:/workspace/opal/unisson/unisson-test/src/main/prolog/opal/test/simplegraph/v2/noedges/v2.noedges.expected_violation.sad
% Created by Vespucci, Technische Universiti�t Darmstadt, Department of Computer Science
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

% Date <07/03/2011 18:07:06>.
%------

%------
%ensemble(File, Name, Query, SubEnsembles) :- Definition of an ensemble.
%	File - The simple file name in which the ensemble is defined. (e.g., 'Flashcards.sad')
%	Name - Name of the ensemble
%	Query - Query that determines which source elements belong to the ensemble
%	SubEnsembles - List of all sub ensembles of this ensemble.
%------
ensemble('v2.noedges.expected_violation.sad', 'A', [], (class_with_members('opal.test.simplegraph.v2.noedges','A')), []).
ensemble('v2.noedges.expected_violation.sad', 'B', [], (class_with_members('opal.test.simplegraph.v2.noedges','B')), []).

%------
%DEPENDENCY(File, ID, SourceE, TargetE, Type) :- Definition of a dependency between two ensembles.
%	DEPENDENCY - The type of the dependency. Possible values: outgoing, incoming, expected, not_allowed
%	File - The simple file name in which the dependency is defined. (e.g., 'Flashcards.sad')
%	ID - An ID identifying the dependency
%	SourceE - The source ensemble
%	TargetE - The target ensemble
%	Relation classifier - Kinds of uses-relation between source and target ensemble (all, field_access, method_call,...)
%------
expected('v2.noedges.expected_violation.sad', 1, 'A', [], 'B', [], [all]).
expected('v2.noedges.expected_violation.sad', 2, 'B', [], 'A', [], [all]).
