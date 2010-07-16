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
		
		visualize_term_structure/2]
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
	/*
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
	).*/





/* Terms used internally by the SAE that could conflict with terms of SAE prolog
	programs always start with the '$SAE' "namespace". 
*/
built_in_term('$SAE':_).



/*
We need to know the id of the goal (S) that is executed after a given goal (G)
when the goal (G) fails or succeeds.

goal(G,SID,FID)
*/

number_term_nodes(Term,NumberedTerm) :-
	number_term_nodes(Term,1,_,NumberedTerm).

number_term_nodes(A,CID,NID,goal(CID,A)) :- var(A),!,NID is CID +1.
number_term_nodes((L,R),CID,NID,Goal) :- 
	!, 
	number_term_nodes(L,CID,IID,LGoal),
	number_term_nodes(R,IID,NID,RGoal),
	Goal = and(CID,NID,(LGoal,RGoal)).
number_term_nodes((L;R),CID,NID,Goal) :- 
	!, 
	number_term_nodes(L,CID,IID,LGoal),
	number_term_nodes(R,IID,NID,RGoal),
	Goal = or(CID,NID,(LGoal;RGoal)).
number_term_nodes(T,CID,NID,goal(CID,T)) :- NID is CID +1.


inc(ID,NID) :- NID is ID + 1.
inc_if_less_than_else(LT,E,ID,NID) :- ID < LT -> NID is ID +1 ; NID = E.
constant(NID,_ID,NID).

% to match the root node..
node_successors(Goal,NGoal) :- 
	Goal = goal(_ID,_G),!,
	node_successors(Goal,inc,inc,NGoal).
node_successors(Goal,NGoal) :- 
	Goal = and(_Min,Max,(_LGoal,_RGoal)),!,
	node_successors(Goal,inc,constant(Max),NGoal).	
node_successors(Goal,NGoal) :- 
	Goal = or(_Min,Max,(_LGoal;_RGoal)),!,
	node_successors(Goal,inc,constant(Max),NGoal).	
% to traverse the graph
node_successors(goal(ID,G),SuccIDFunc,FailIDFunc,NGoal) :- !,
	call(SuccIDFunc,ID,SID),
	call(FailIDFunc,ID,FID),
	NGoal = goal(ID,G,SID,FID).
node_successors(Goal,SuccIDFunc,FailIDFunc,NGoal) :-
	Goal = and(Min,Max,(LGoal,RGoal)),!,
	node_successors(LGoal,SuccIDFunc,FailIDFunc,NLGoal),
	node_successors(RGoal,SuccIDFunc,FailIDFunc,NRGoal),
	NGoal = and(Min,Max,(NLGoal,NRGoal)).
node_successors(Goal,SuccIDFunc,FailIDFunc,NGoal) :-
	Goal = or(Min,Max,(LGoal;RGoal)),!,
	(RGoal = goal(RBLID,_),! ; RGoal = and(RBLID,_,_),! ; RGoal = or(RBLID,_,_),!),
	Boundary is RBLID - 1,
	node_successors(LGoal,inc_if_less_than_else(Boundary,Max),constant(RBLID),NLGoal),
	node_successors(RGoal,SuccIDFunc,FailIDFunc,NRGoal),
	NGoal = or(Min,Max,(NLGoal;NRGoal)).
	
	




visualize_term_structure(Term,DotFile) :- 
	number_term_nodes(Term,NT),
	node_successors(NT,NTwithSucc),
	visualize_numbered_term(NTwithSucc,[],DF),
	atomic_list_concat(DF,T),
	term_to_atom(Term,A),
	numbered_term_exit_node_id(NTwithSucc,EID),
	atomic_list_concat([
		'digraph G {\n',
		'label="',A,'";\nnode [shape=none];\nedge [arrowhead=none];\n',
		T,
		EID,' [label="Exit",shape="Box"];\n',
		'}'],DotFile).
	
/*	
visualize_numbered_term(ThisGoal,DotFile) :- 
	visualize_numbered_term(ThisGoal,[],NT),
	atomic_list_concat(NT,T),
	term_to_atom(ThisGoal,A),
	DF=['digraph G {\nlabel="',A,'";\nnode [shape=none];\n',T,'}'],
	atomic_list_concat(DF,DotFile).
*/
	 
visualize_numbered_term(ThisGoal,CurrentDotFile,DotFile) :- 
	ThisGoal = and(_Min,_Max,(LGoal,RGoal)),!,
	visualize_numbered_term(LGoal,CurrentDotFile,IDF1),numbered_term_node_id(LGoal,LID),
	visualize_numbered_term(RGoal,IDF1,IDF2),numbered_term_node_id(RGoal,RID),	
	numbered_term_node_id(ThisGoal,TID),
	DotFile=[
		TID,' [label="⋀ (and)"];\n',
		TID,' -> ',LID,' [penwidth="2.25"];\n',
		TID,' -> ',RID,' [penwidth="2.25"];\n'
		|IDF2].
visualize_numbered_term(ThisGoal,CurrentDotFile,DotFile) :- 
	ThisGoal = or(_Min,_Max,(LGoal;RGoal)),!,
	visualize_numbered_term(LGoal,CurrentDotFile,IDF1),numbered_term_node_id(LGoal,LID),
	visualize_numbered_term(RGoal,IDF1,IDF2),numbered_term_node_id(RGoal,RID),	
	numbered_term_node_id(ThisGoal,TID),
	DotFile=[
		TID,' [label="⋁ (or)"];\n',
		TID,' -> ',LID,' [penwidth=2.25];\n',
		TID,' -> ',RID,' [penwidth=2.25];\n'	
		|IDF2].
visualize_numbered_term(goal(V,G,SID,FID),CurrentDotFile,DotFile) :-
	term_to_atom(G,A),
	atomic_list_concat([
			V,' [label="',A,'"];\n',
			V,' -> ',SID,' [color=green,constraint=false,arrowhead=normal];\n',
			V,' -> ',FID,' [color=red,constraint=false,arrowhead=normal];\n'	
		],ID),
	DotFile=[ID|CurrentDotFile].
	

numbered_term_node_id(and(Min,Max,_),ID) :- atomic_list_concat([and,'_',Min,'_',Max],ID).
numbered_term_node_id(or(Min,Max,_),ID) :- atomic_list_concat([or,'_',Min,'_',Max],ID).
numbered_term_node_id(goal(ID,_G,_SID,_FID),ID).

numbered_term_exit_node_id(goal(_ID,_G,EID,EID),EID) :- !.
numbered_term_exit_node_id(and(_Min,Max,_),Max) :- !.
numbered_term_exit_node_id(or(_Min,Max,_),Max) :- !.




/*
normalize_goal_sequence_order((A=t2,X=1,(A = t1,!,B=t2 ; C = u2, B=u1),Z=e),G),goals(G,1,M,AA).

goals(A,A) :- var(A),!.
goals((L,R),Goal) :- !, (goals(L,Goal);goals(R,Goal)).
goals((L;R),Goal) :- !, (goals(L,Goal);goals(R,Goal)).	
goals(T,T).
*/
	
