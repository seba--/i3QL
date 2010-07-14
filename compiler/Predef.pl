 /*
	Definition of all operators and predicates that are either directly
	understood by SAE Prolog - i.e., that are taken care of during compilation -
	or which are pre-imlemented as part of SAE Prolog's library.
	
	The redefinition of built-in predicates (operators) is not supported. The ISO
	standard semantics of these predicates is implemented by the compiler.

	@version $Date$ $Rev$
	@author Michael Eichberg
*/
:- module(
	'Compiler:Predef',
	[	predefined_predicates/2,
		predefined_operator/1,
		predefined_term_operator/1,
		predefined_arithmetic_operator/1,
		
		built_in_term/1,
		
		called_predicate/2]
).


/* @signature predefined_predicates(UserDefinedProgram,CompleteProgram)

	@param UserDefinedProgram is the AST of the loaded SAE Prolog Program.
	@param CompleteProgram is the AST of the loaded Program extended with
		the built-in predicates.
*/
predefined_predicates(UserDefinedProgram,CompleteProgram) :-
	CompleteProgram = [
	
		% Primary Predicates - we need to analyze the sub
		pred(';'/2,[],[deterministic(no)]), % or
		%(not yet supported:) ('->'/2,[],[deterministic(dependent)]), % If->Then(;Else)
		%(not yet supported:) ('*->'/2,[],[deterministic(dependent)]), % Soft Cut
		pred(','/2,[],[deterministic(dependent)]), % and
	
		% Extra-logical Predicates
		pred(nonvar/1,[],[deterministic(yes)]),
		pred(var/1,[],[deterministic(yes)]),
		pred(integer/1,[],[deterministic(yes)]),
		pred(atom/1,[],[deterministic(yes)]),
		pred(compound/1,[],[deterministic(yes)]),
		pred(functor/3,[],[deterministic(yes)]),

		% "Other" Predicates
		pred('='/2,[],[deterministic(yes)]), % unify
		pred('\\='/2,[],[deterministic(yes)]), % does not unify
		pred('/'('\\+',1),[],[deterministic(yes)]), % not
		pred('=='/2,[],[deterministic(yes)]), % term equality
		pred('\\=='/2,[],[deterministic(yes)]), % term inequality

		pred('is'/2,[],[deterministic(yes),mode(-,+)]), % "arithmetic" assignment (comparison)

		pred(true/0,[],[deterministic(yes)]), % always succeeds
		pred(false/0,[],[deterministic(yes)]), % always fails
		pred(fail/0,[],[deterministic(yes)]), % always fails
		pred('!'/0,[],[deterministic(yes)]), % cut

		pred('<'/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=:='/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=<'/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=\\='/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('>'/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('>='/2,[],[deterministic(yes),mode(+,+)]) % arithmetic comparison; requires both terms to be instantiated

		% Recall, that the "other" arithmetic operators 
		% (e.g., +, -, *,...) are not top-level
		% predicates!)
		| UserDefinedProgram].




predefined_operator(Operator) :-
	predefined_term_operator(Operator);
	predefined_arithmetic_operator(Operator).




predefined_term_operator('=').
predefined_term_operator('\\=').
predefined_term_operator('==').
predefined_term_operator('\\==').




predefined_arithmetic_operator('<').
predefined_arithmetic_operator('=:=').
predefined_arithmetic_operator('=<').
predefined_arithmetic_operator('=\\=').
predefined_arithmetic_operator('>').
predefined_arithmetic_operator('>=').




/* called_predicate(Term,Predicate) :- the list of all Predicates called by the
	given Term (in depth-first order). 
	
	<p>
	This predicate takes care of supported meta-calls (e.g., call/1, findall/3...). 
	</p>
*/	
called_predicate(Term,Predicate):- 
	functor(Term,Functor,Arity),
	(
		Predicate = Functor/Arity
	;
		(
			Functor = ',';
			Functor = ';'
		),
		Arity =:= 2,
		(arg(1,Term,Arg);arg(2,Term,Arg)),
		called_predicate(Arg,Predicate)
	).





/* Terms used internally by the SAE that could conflict with terms of SAE prolog
	programs always start with the 'SAE' 
	"namespace". 
*/
built_in_term('SAE':_).



/*
We need to know the id of the goal (S) that is executed after a given goal (G)
when the goal (G) fails or succeeds.

goal(G,SID,FID)
*/
/* goals(Term,CurrentID,ID,goal(G),success(ID,goal(GS)),failure(ID,goal(GF))) :- ...*/

%%%% goals(CurrT,PrevT,CurrID,NextID,Goals) :- 
/*
goals_f(V,PrevTs,CurrID,PrevGoals,Goals) :- 
	var(V),!,
	Gs = [goal(CurrID,V)|PrevGoals],
	(	PrevTs = [] ->
		Goals=Gs
	;	
		
	).
	
	PrevTs = [PT|RPTs] 
	goals_b(PT,RTs,)
	*/
	
number_goals(A,CID,NID,goal(CID,A)) :- var(A),!,NID is CID +1.
number_goals((L,R),CID,NID,Goal) :- 
	!, 
	number_goals(L,CID,IID,LGoal),
	number_goals(R,IID,NID,RGoal),
	Goal = and(CID,NID,(LGoal,RGoal)).
number_goals((L;R),CID,NID,Goal) :- 
	!, 
	number_goals(L,CID,IID,LGoal),
	number_goals(R,IID,NID,RGoal),
	Goal = or(CID,NID,(LGoal;RGoal)).
number_goals(T,CID,NID,goal(CID,T)) :- NID is CID +1.
	

/*
goals(A,A) :- var(A),!.
goals((L,R),Goal) :- !, (goals(L,Goal);goals(R,Goal)).
goals((L;R),Goal) :- !, (goals(L,Goal);goals(R,Goal)).	
goals(T,T).
*/
	
