/*
	This phase transforms the AST to make the representation of SAE Prolog
	programs more regular.
	
	@author Michael Eichberg
*/
:- module('Compiler:Phase:Normalize',[pl_normalize/4]).

:- use_module('../Utils.pl').
:- use_module('../Debug.pl').




/*	Transforms the AST of an SAE Prolog program to make it more regular and 
	to facilitate subsequent analyses.
	
	@param Debug is the list of debug information that should be printed out.
		Possible values are: 'on_entry' and 'ast'.
	@param Program is the program's AST. 
		<pre>
		[	pred(
				a/1, 							% The identifier of the predicate
				[(C, [det, type=int])],	% A list of clauses defining the 
												% predicate. C is one of these clauses.
												% In case of built-in propeties, this list
												% is empty.
				[type=...]					% List of properties of the predicate
			),...
		]
		</pre>
	@param _OutputFolder
	@param NProgram is the normalized AST of the program. 
		<p>
		<b>Property</b><br/>
		In a normalized clause the header's arguments are all variables where no
		variable occurs more than once. <br/>
		<b>Process</b><br/>
		If a header's argument is not a free variable then the argument is 
		replaced with a new variable. Additionally, a statement that unifies 
		the new variable (representing the argument) with the original argument 
		is prepended to the body of the clause. All additional unifications are 
		prepended to the methods body in the order of the method arguments.	
		</p>
		<p>
		Additionally, a clause's body is transformed such, that – if a clause has a 
		unique last goal – then the last goal is the immediate right child of the
		body's top-level and (',') node.		
		</p>
		<code><pre>
		[	pred(
				a/1, 							% The predicate (Functor/Arity)
				[(C, [det, type=int])],	% A list of clauses defining the 
												% predicate. C is one of these clauses.
												% In case of built-in propeties, this list
												% is empty.<br/>
												% C is always of the form:
												% <code> term(A1,...AN) :- Body. </code>
												% where each Ai is a unique variable. 
				[type=...]					% List of properties of the predicate
			),...
		]
		</pre></code>
*/
pl_normalize(Debug,Program,_OutputFolder,NProgram) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Normalizing the AST__________________________________________'),

	normalize_predicates(Program,NProgram-[]),
	!, % TODO Still required?
	
	debug_message(Debug,ast,NProgram).
	
	

	
normalize_predicates([],X-X) :- !. % green cut
normalize_predicates(
		[pred(Predicate,Clauses,PredicateProperties)|Predicates],
		[ProcessedPredicate|RN]-RZ
	) :-
	normalize_clauses(Clauses,NewClauses),
	ProcessedPredicate = pred(Predicate,NewClauses,PredicateProperties),
	normalize_predicates(Predicates,RN-RZ).


	
	
/* normalize_clauses(Clauses,NewClauses) :-
	Normalizes the AST of all clauses.
*/		
normalize_clauses([],[]) :- !. % green cut
normalize_clauses([Clause|Clauses],[NClause|NClauses]) :-
	normalize_clause(Clause,NClause),
	normalize_clauses(Clauses,NClauses).
	
	
	
	
/* normalize_clause(OldClause,NewClause) :- normalizes the representation of a
 	clause. <br />
	In a normalized clause the header's arguments are all variables where no
	variable occurs more than once. 
	
	@param OldClause the old clause.
	@param NewClause is the normalized representation of OldClause.
*/
normalize_clause((:-(H,B),ClauseProperties),NewClause) :- 
	functor(H,Functor,Arity),
	functor(NewH,Functor,Arity),
	normalize_arguments(H,NewH,B,NewB),
	normalize_goal_sequence_order(NewB,NormalizedGoalOrderB),
	remove_trailing_true_calls(NormalizedGoalOrderB,MinimizedB),
	NewClause = (:-(NewH,MinimizedB),ClauseProperties).
	
	
	

normalize_arguments(H,NewH,B,NewB) :- 
	functor(H,_,AID),
	normalize_arguments(AID,H,NewH,B,NewB).

normalize_arguments(0,_,_,B,B) :- !.
normalize_arguments(AID,H,NewH,B,FinalB) :-
	AID > 0,	NewAID is AID - 1,
	arg(AID,H,Arg),
	(	(var(Arg),is_first_occurence_of_variable_as_term_argument(NewAID,H,Arg)) ->
		(
			arg(AID,NewH,Arg),
			NewB=B
		);(
			arg(AID,NewH,X),
			NewB = ((X = Arg),B)
		)
	),
	normalize_arguments(NewAID,H,NewH,NewB,FinalB).




/* is_first_occurence_of_variable_as_term_argument(+AID,+T,+Var) :- tests if no 
	argument of a given term T in the range [1..AID] is the variable Var.
	
 	@param AID is the upper bound of the argument index.
	@param T a term.
	@param Var an (unbound) variable.
*/
is_first_occurence_of_variable_as_term_argument(0,_,_) :- !.
is_first_occurence_of_variable_as_term_argument(AID,T,Var) :-
	arg(AID,T,Arg),
	Arg \== Var,
	NewAID is AID - 1,
	is_first_occurence_of_variable_as_term_argument(NewAID,T,Var).	




/* normalize_goal_sequence_order(Body,NewBody) :- performs a tree rotation to 
	make sure that	a clause's last goal – if the last goal is unique – is the
	immediate right child element of top-level "and" node.
	
	<p>
	<b>Example</b><br />
	E.g., the goal sequence (A,B,C,D,E) <=>
	(A,(B,(C,(D,E))) is transformed to ((((A, B), C), D), E). This representation
	is advantegeous because some analyses do need to analyze a goal sequence
	except of the "last goal". Hence, after this transformation the goal
	sequence G can be unified with G=(PreviousGoals,LastGoal) to get the sequence
	of all goals except of the last goal. 
	</p>
*/
normalize_goal_sequence_order(B,NewB) :- 
		var(B) -> NewB = B
	;
		(B = (L,R), nonvar(R), R=(RL,RR)) -> ( % we have to avoid accidental bindings
			normalize_goal_sequence_order((L,RL),NewL),
			IB = (NewL,RR),
			normalize_goal_sequence_order(IB,NewB)
		)
	;
		B = (L;R) -> (
			normalize_goal_sequence_order(L,NewLB),
			normalize_goal_sequence_order(R,NewRB),
			NewB = (NewLB;NewRB)
		)
	;
		NewB = B.

	


remove_trailing_true_calls(Term,NewTerm) :-
		var(Term) -> NewTerm = Term 
	;
		Term = (T,true) -> (ITerm = T, remove_trailing_true_calls(ITerm,NewTerm))
	;
		NewTerm = Term.
	

	


