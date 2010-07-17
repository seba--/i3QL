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

:- use_module('Utils.pl',[max/3]).


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

number_term_nodes(A,ID,NID,goal(ID,A)) :- var(A),!,NID is ID +1.
number_term_nodes((L,R),CID,NID,Goal) :- 
	!, 
	number_term_nodes(L,CID,IID,LGoal),
	number_term_nodes(R,IID,NID,RGoal),
	Goal = and(CID,IID,NID,(LGoal,RGoal)).
number_term_nodes((L;R),CID,NID,Goal) :- 
	!, 
	number_term_nodes(L,CID,IID,LGoal),
	number_term_nodes(R,IID,NID,RGoal),
	Goal = or(CID,IID,NID,(LGoal;RGoal)).
number_term_nodes(T,CID,NID,goal(CID,T)) :- NID is CID +1.


% to match the root node..
node_successors(Goal,NGoal) :- 
	Goal = goal(ID,G),!,
	Exit is ID + 1,
	is_cut(G,RelevantCutCall), % ... is not really required
	node_successors(Goal,Exit,Exit,Exit,RelevantCutCall,_Cuts,NGoal).
node_successors(Goal,NGoal) :- 
	Goal = and(_MinL,_MinR,Exit,(_LGoal,_RGoal)),!,
	node_successors(Goal,Exit,Exit,Exit,false,_Cuts,NGoal).	
node_successors(Goal,NGoal) :- 
	Goal = or(_MinL,_MinR,Exit,(_LGoal;_RGoal)),!,
	node_successors(Goal,Exit,Exit,Exit,false,_Cuts,NGoal).	
% to traverse the graph
node_successors(
		goal(ID,G),
		BranchSucceedsNID,
		BranchFailsNID,
		CutBranchFailsNID,
		RelevantCutCall,
		Cuts,
		NGoal) :- 
	!,
	(	is_cut(G) -> Cuts = true ; Cuts = RelevantCutCall ),
	NGoal = goal(ID,G,RelevantCutCall,BranchSucceedsNID,BranchFailsNID,CutBranchFailsNID).
node_successors(
		Goal,
		BranchSucceedsNID,
		BranchFailsNID,
		CutBranchFailsNID,
		RelevantCutCall, % in variable
		Cuts, % out variable
		NGoal) :-
	Goal = and(MinL,MinR,NID,(LGoal,RGoal)),!,
	node_successors(LGoal,MinR,BranchFailsNID,CutBranchFailsNID,RelevantCutCall,LCuts,NLGoal),
	% if there are no (open) choicepoints when this goal is reached, then it
	% is irrelevant if in the left branch a cut is encountered, as such a cut will never
	% cut of any choice points created (later) in the right branch
	( (BranchFailsNID =:= CutBranchFailsNID,!, RCC = false) ; RCC=LCuts ),
	node_successors(RGoal,BranchSucceedsNID,BranchFailsNID,CutBranchFailsNID,RCC,Cuts,NRGoal),
	NGoal = and(MinL,MinR,NID,(NLGoal,NRGoal)).
node_successors(
		Goal,
		BranchSucceedsNID,
		BranchFailsNID,
		CutBranchFailsNID,
		RelevantCutCall, % in variable
		Cuts, % out variable
		NGoal) :-
	Goal = or(MinL,MinR,NID,(LGoal;RGoal)),!,
	% when we reach this node (this "or" goal), a choice point is created and 
	% the right branch is the target if the 
	% evaluation of the left branch fails (and unless another cut is encountered...)
	% hence, "RelevantCutCall" is set to false because (at least) this choice point
	% is not cut
	node_successors(LGoal,BranchSucceedsNID,MinR,CutBranchFailsNID,false,LCuts,NLGoal),
	% the right branch is only evaluated if not cut is called when
	% the left branch is evaluated; however, previous choice points may be
	% cut and hence, when evaluating the right branch we have to the previous
	% information whether the cut was called or not into consideration
	node_successors(RGoal,BranchSucceedsNID,BranchFailsNID,CutBranchFailsNID,RelevantCutCall,RCuts,NRGoal),
	(
			(LCuts = true, RCuts = true,!,Cuts = true)
		;
			(LCuts = false, RCuts = false,!,Cuts = false)
		;
			Cuts = maybe
	),
	NGoal = or(MinL,MinR,NID,(NLGoal;NRGoal)).


is_cut(Term) :- nonvar(Term),Term = !.

is_cut(Term,Cut) :- is_cut(Term),!,Cut=true.
is_cut(_Term,false).



generate_dot_visualization(Term,OutputFile) :-
	visualize_term_structure(Term,DotFile),
	open(OutputFile,write,Stream),
	write(Stream,DotFile),
	close(Stream).


/*
	Examples:
	(G=(a,(b,(c,!,d,e;f);g),h)),normalize_goal_sequence_order(G,NG),visualize_term_structure(NG,VG),write(VG).

	Does not contain any relevant cut calls...
	generate_dot_visualization(call(a1,a2,a3),'/Users/Michael/Desktop/Forward.dot').
	generate_dot_visualization((((ap,!,aa);(bp,!,ba);(cp,!,ca)),f),'/Users/Michael/Desktop/Switch.dot').
	generate_dot_visualization((!,a,b,c,(d;e),f),'/Users/Michael/Desktop/Standard.dot').
	generate_dot_visualization((!),'/Users/Michael/Desktop/JustCut.dot').
	generate_dot_visualization((a,(b,!,c;d);f),'/Users/Michael/Desktop/TermWithCut.dot').
	generate_dot_visualization((((a;(c,!)),b),VG),'/Users/Michael/Desktop/NoRelevantCutCalls.dot').
*/
visualize_term_structure(Term,DotFile) :- 
	number_term_nodes(Term,NT),
	node_successors(NT,NTwithSucc),
	visualize_numbered_term(NTwithSucc,[],DF),
	atomic_list_concat(DF,T),
	term_to_atom(Term,A),
	numbered_term_exit_node_id(NTwithSucc,EID),
	atomic_list_concat([
		'digraph G {\n',
		'label="Term: ',A,'";\nnode [shape=none];\nedge [arrowhead=none];\n',
		T,
		EID,' [label="Exit",shape="Box"];\n',
		'}'],DotFile).




visualize_numbered_term(ThisGoal,CurrentDotFile,DotFile) :- 
	ThisGoal = and(_MinL,_MinR,_Max,(LGoal,RGoal)),!,
	visualize_numbered_term(LGoal,CurrentDotFile,IDF1),numbered_term_node_idatom(LGoal,LID),
	visualize_numbered_term(RGoal,IDF1,IDF2),numbered_term_node_idatom(RGoal,RID),	
	numbered_term_node_idatom(ThisGoal,TID),
	DotFile=[
		TID,' [label="⋀ (and)"];\n',
		TID,' -> ',LID,' [penwidth="2.25"];\n',
		TID,' -> ',RID,' [penwidth="2.25"];\n'
		|IDF2].
visualize_numbered_term(ThisGoal,CurrentDotFile,DotFile) :- 
	ThisGoal = or(_MinL,_MinR,_Max,(LGoal;RGoal)),!,
	visualize_numbered_term(LGoal,CurrentDotFile,IDF1),numbered_term_node_idatom(LGoal,LID),
	visualize_numbered_term(RGoal,IDF1,IDF2),numbered_term_node_idatom(RGoal,RID),	
	numbered_term_node_idatom(ThisGoal,TID),
	DotFile=[
		TID,' [label="⋁ (or)"];\n',
		TID,' -> ',LID,' [penwidth=2.25];\n',
		TID,' -> ',RID,' [penwidth=2.25];\n'	
		|IDF2].
visualize_numbered_term(goal(V,G,RelevantCutCall,SID,FID,CFID),CurrentDotFile,DotFile) :-
	term_to_atom(G,A),
	(
		( 
			RelevantCutCall = true,!,
			atomic_list_concat([
					V,' [label="',A,'",fontcolor=maroon];\n',
					V,' -> ',SID,' [color=green,constraint=false,arrowhead=normal];\n',
					V,' -> ',CFID,' [label="!",color=maroon,constraint=false,arrowhead=normal];\n'
				],ID)
		)
		;
		( 
			RelevantCutCall = false,!,
			atomic_list_concat([
					V,' [label="',A,'"];\n',
					V,' -> ',SID,' [color=green,constraint=false,arrowhead=normal];\n',
					V,' -> ',FID,' [color=red,constraint=false,arrowhead=normal];\n'
				],ID)
		)
		;
		(
			RelevantCutCall = maybe,!,
			atomic_list_concat([
					V,' [label="',A,'",fontcolor=darksalmon];\n',
					V,' -> ',SID,' [color=green,constraint=false,arrowhead=normal];\n',
					V,' -> ',FID,' [color=salmon,constraint=false,arrowhead=normal];\n',
					V,' -> ',CFID,' [label="!",color=pink,constraint=false,arrowhead=normal];\n'	
				],ID)		
		)
	),
	DotFile=[ID|CurrentDotFile].




numbered_term_node_idatom(and(MinL,MinR,Max,_),ID) :- atomic_list_concat([and,'_',MinL,'_',MinR,'_',Max],ID).
numbered_term_node_idatom(or(MinL,MinR,Max,_),ID) :- atomic_list_concat([or,'_',MinL,'_',MinR,'_',Max],ID).
numbered_term_node_idatom(goal(ID,_G,_RelevantCutCall,_SID,_FID,_CFID),ID).




numbered_term_exit_node_id(goal(_ID,_G,_RelevantCutCall,EID,EID,EID),EID) :- !.
numbered_term_exit_node_id(and(_MinL,_MinR,Max,_),Max) :- !.
numbered_term_exit_node_id(or(_Min,_MinR,Max,_),Max) :- !.

