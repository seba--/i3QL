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
	Analyzes a clause to check if the clause performs a cut.
	
	@author Michael Eichberg
*/
:- module('Compiler:Phase:PLCutAnalysis',[pl_cut_analysis/4]).

:- use_module('../AST.pl').
:- use_module('../Debug.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').



pl_cut_analysis(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(DebugConfig,on_entry,write('\n[Debug] Phase: Cut Analysis_________________________________________________\n')),
	foreach_user_predicate(Program,process_predicate).



process_predicate(Predicate) :-
	predicate_identifier(Predicate,PredicateIdentifier),
	term_to_atom(PredicateIdentifier,PredicateIdentifierAtom),
	debug_message(DebugConfig,processing_predicate,write_atomic_list(['[Debug] Processing Predicate: ',PredicateIdentifierAtom,'\n'])),
	% implementation...
	predicate_clauses(Predicate,Clauses),
	foreach_clause(Clauses,analyze_cut,[CutBehavior|CutBehaviors]),
	disjunction_of_list_of_cut_behaviors(CutBehaviors,CutBehavior,OverallCutBehavior),
	predicate_meta(Predicate,Meta),
	add_to_meta(cut(OverallCutBehavior),Meta).



analyze_cut(_ClauseId,Clause,_RelativeClausePosition,CutBehavior) :-
	clause_implementation(Clause,Implementation),
	rule_body(Implementation,BodyASTNode),
	cut_analysis(BodyASTNode,CutBehavior),
	clause_meta(Clause,Meta),	
	add_to_meta(cut(CutBehavior),Meta).



disjunction_of_list_of_cut_behaviors([],CutBehavior,CutBehavior).
disjunction_of_list_of_cut_behaviors(
		[CutBehavior|CutBehaviors],
		CurrentCutBhavior,
		OverallCutBehavior
	) :-
	disjunction_of_cut_behaviors(CutBehavior,CurrentCutBhavior,IntermediateCutBehavior),
	disjunction_of_list_of_cut_behaviors(CutBehaviors,IntermediateCutBehavior,OverallCutBehavior).
