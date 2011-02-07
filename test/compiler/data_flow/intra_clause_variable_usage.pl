/* License (BSD Style License):
   Copyright (c) 2011
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
	Tests the intra_clause_variable_usage predicate.

   @author Michael Eichberg (mail@michael-eichberg.de)
*/
:- ensure_loaded('src/prolog/compiler/Lexer.pl').
:- ensure_loaded('src/prolog/compiler/Parser.pl').
:- ensure_loaded('src/prolog/compiler/AST.pl').
:- ensure_loaded('src/prolog/compiler/Utils.pl').
:- ensure_loaded('src/prolog/compiler/phase/PLAnalyzeVariables.pl').


:- begin_tests(intra_clause_variable_usage).


assert_data_flow_analysis_results(
		PrologClause, % e.g., "do(A,B)."
		pre(PreUsedVariables,PrePotentiallyUsedVariables,PerVariablesUsedOnlyOnce),
		post(PostUsedVariables,PostPotentiallyUsedVariables,PostVariablesUsedOnlyOnce)
	) :-
	tokenize_string(PrologClause,Ts),
	clauses(Ts,[Clause]),
	named_variables_of_term(Clause,ClauseVariablesNodes,[]),	
	sae_analyze_variables:map_names_of_body_variables(
			0,ClauseVariablesNodes,
			_ClauseLocalVariablesCount,_VariableNamesToIdsMap),
	sae_analyze_variables:intra_clause_variable_usage(
		Clause,
		PreUsedVariables,PrePotentiallyUsedVariables,PerVariablesUsedOnlyOnce,
		UsedVariables,PotentiallyUsedVariables,VariablesUsedOnlyOnce	
	),
	(	set_is_equal(UsedVariables,PostUsedVariables), 
		set_is_equal(PostPotentiallyUsedVariables,PostPotentiallyUsedVariables),
		set_is_equal(VariablesUsedOnlyOnce,PostVariablesUsedOnlyOnce)
	->
		true
	;
		write('Expected:\n'),
		write(PostUsedVariables),write(PostPotentiallyUsedVariables),write(PostVariablesUsedOnlyOnce),nl,
		write('Result:\n'),
		write(UsedVariables),write(PotentiallyUsedVariables),write(VariablesUsedOnlyOnce),nl,nl,
		fail
	).



test(all_variables_are_new) :-
	assert_data_flow_analysis_results(
		"do(A,X).",
		pre([],[],[]),
		post([clv(0),clv(1)],[],[clv(0),clv(1)])
	).

test(using_a_variable_multiple_times) :-
	assert_data_flow_analysis_results(
		"do(A,A).",
		pre([],[],[]),
		post([clv(0)],[],[])
	).

test(one_variable_has_been_used_previously) :-
	assert_data_flow_analysis_results(
		"do(A,B).",
		pre([clv(0)],[],[clv(0)]),
		post([clv(0),clv(1)],[],[clv(1)])
	).

test(one_variable_has_been_used_previously_only_once) :-
	assert_data_flow_analysis_results(
		"do(A,B).",
		pre([clv(0)],[clv(1)],[clv(1)]),
		post([clv(0),clv(1)],[],[])
	).
	
test(chain_use) :-
	assert_data_flow_analysis_results(
		"do(D),do(A,B),do(A),do(C),do(B,C).",
		pre([],[],[]),
		post([clv(0),clv(1),clv(2),clv(3)],[],[clv(0)])
	).	
	
test(or_all_variables_are_new) :-
	assert_data_flow_analysis_results(
		"do(A);do(B).",
		pre([],[],[]),
		post([],[clv(0),clv(1)],[clv(0),clv(1)])
	).	

test(or_one_new_variable) :-
	assert_data_flow_analysis_results(
		"do(A),(do(A);do(B)).",
		pre([],[],[]),
		post([clv(0)],[clv(1)],[clv(1)])
	).

test(or_all_variables_are_used_on_all_paths_once) :-
	assert_data_flow_analysis_results(
		"do(A),(foo(B);bar(B)).",
		pre([],[],[]),
		post([clv(0),clv(1)],[],[clv(0),clv(1)])
	).

test(or_one_variable_used_on_all_paths_two_used_on_only_one_path) :-
	assert_data_flow_analysis_results(
		"do(A),do(B);do(A),do(C).",
		pre([],[],[]),
		post([clv(0)],[clv(1),clv(2)],[clv(0),clv(1),clv(2)])
	).


:- end_tests(intra_clause_variable_usage).