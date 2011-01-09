/* License (BSD Style License):
   Copyright (c) 2010
   Department of Computer Science
   Technische Universität Darmstadt
   All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    - Neither the name of the Software Technology Group or Technische 
      Universität Darmstadt nor the names of its contributors may be used to 
      endorse or promote products derived from this software without specific 
      prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/


/**
	Analyzes each clause w.r.t. its usage of the cut.
	<p>
	After that, the overall behavior of the predicate is determined. I.e., if all
	clauses 1..n-1 (the last one doesn't have to) always perform a cut operation 
	then always at most one clause will succeed (deterministic clause selection).
	</p>
	
	@author Michael Eichberg
*/
:- module('SAEProlog:Compiler:Phase:PLCutAnalysis',[pl_cut_analysis/4]).

:- use_module('../AST.pl').
:- use_module('../Debug.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').



pl_cut_analysis(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(
		DebugConfig,
		on_entry,
		write('\n[Debug] Phase: Cut Analysis_________________________________________________\n')
	),
	foreach_user_predicate(Program,process_predicate(DebugConfig)).



process_predicate(DebugConfig,Predicate) :-
	% logging...
	predicate_identifier(Predicate,Functor/Arity),
	debug_message(DebugConfig,processing_predicate,write_atomic_list(['[Debug] Processing Predicate: ',Functor,'/',Arity,'\n'])),
	
	% implementation...
	predicate_clauses(Predicate,Clauses),
	foreach_clause(
		Clauses,
		analyze_cut,
		CutBehaviors),
	deterministic_clause_selection(CutBehaviors,DeterministicClauseSelection),
	add_to_predicate_meta(
		deterministic_clause_selection(DeterministicClauseSelection),
		Predicate),
		
	% logging...
	debug_message(DebugConfig,memberchk(results),write_atomic_list(['[Debug] Deterministic clause selection: ',Functor,'/',Arity,' - ',DeterministicClauseSelection,'\n'])).



analyze_cut(_ClauseId,Clause,_RelativeClausePosition,CutBehavior) :-
	clause_implementation(Clause,Implementation),
	rule_body(Implementation,BodyASTNode),
	cut_analysis(BodyASTNode,CutBehavior),
	add_to_clause_meta(cut(CutBehavior),Clause).



% IMPROVE It is imaginable to check that a set of clauses is exclusiv (given a specific calling pattern) to deduce that the clause selection is deterministic even if the developer uses no cuts.
deterministic_clause_selection(CutBehaviors,DeterministicClauseSelection) :-
	deterministic_clause_selection(
		CutBehaviors,
		yes, % initial value... if we have just one clause..
		DeterministicClauseSelection
	).



deterministic_clause_selection(
		[_CutBehavior], % We have reached the last clause
		DeterministicClauseSelection,
		DeterministicClauseSelection
	) :- !.
deterministic_clause_selection(
		[CutBehavior|CutBehaviors],
		yes,
		DeterministicClauseSelection
	) :-
	(	CutBehavior == always ->
		deterministic_clause_selection(CutBehaviors,yes,DeterministicClauseSelection)
	;
		DeterministicClauseSelection = no
	).
