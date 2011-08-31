%------
% Prolog based representation of the Vespucci architecture diagram: v2.noedges.not_allowed_correct.sad% Created by Vespucci, Technische Universität Darmstadt, Department of Computer Science
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

% Date <22/11/2010 10:35:31>.
%------

%------
%ensemble(File, Name, Query, SubEnsembles) :- Definition of an ensemble.
%	File - The simple file name in which the ensemble is defined. (e.g., 'Flashcards.sad')
%	Name - Name of the ensemble
%	Query - Query that determines which source elements belong to the ensemble
%	SubEnsembles - List of all sub ensembles of this ensemble.
%------
ensemble('v2.noedges.not_allowed_correct.sad', 'A', [], (class_with_members('opal.test.simplegraph.v2.directed','A')), []).
ensemble('v2.noedges.not_allowed_correct.sad', 'B', [], (class_with_members('opal.test.simplegraph.v2.directed','B')), []).

%------
%DEPENDENCY(File, ID, SourceE, TargetE, Type) :- Definition of a dependency between two ensembles.
%	DEPENDENCY - The type of the dependency. Possible values: outgoing, incoming, expected, not_allowed
%	File - The simple file name in which the dependency is defined. (e.g., 'Flashcards.sad')
%	ID - An ID identifying the dependency
%	SourceE - The source ensemble
%	TargetE - The target ensemble
%	Relation classifier - Kinds of uses-relation between source and target ensemble (all, field_access, method_call,...)
%------
not_allowed('v2.noedges.not_allowed_correct.sad', 1, 'A', [], 'B', [], [all]).
not_allowed('v2.noedges.not_allowed_correct.sad', 2, 'B', [], 'A', [], [all]).
