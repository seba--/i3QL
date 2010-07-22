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
	This library provides predicates to visualize a clause's goal execution order
	using Graphviz' DOT tool (www.graphviz.org).<br />
	The two main predicates are: {@link generate_dot_visualization/2} and
	{@link visualize_term_structure/2}.

	<p>
	<b>Examples</b>
	To generate the dot file in one step:
	<pre>
		generate_dot_visualization(
			((b;a,!),c;f),
			'WeiredControlFlow.dot'
		).
		generate_dot_visualization(
			call(a1,a2,a3),
			'DemoA.dot'
		).
		generate_dot_visualization(
			(((ap,!,aa);(bp,!,ba);(cp,!,ca)),f),
			'Switch.dot'
		).
		generate_dot_visualization(
			(!,a,b,c,(d;e),f),
			'Standard.dot'
		).
		generate_dot_visualization(
			(a,(b,!,c;d);f),
			'IfThenElse.dot'
		).
		generate_dot_visualization(
			(((a;(c,!)),b),VG),
			'IrrelevantCutCall.dot'
		).
	</pre>	
	To print out the dot file to the terminal:
	<pre>
		visualize_term_structure((a,b;c),D),write(D).
	</pre>
	</p>
	
	<p>
	<b>Version History</b>
	<i>0.1 - July 21th, 2010</i><br />
	First release. It can be used to visualize the goal execution order of 
	clauses using "and (,)", "or (;)" and "cut (!)". Further predicates are
	currently not supported.	
	</p>
		
	@author Michael Eichberg
*/
:- module(
	'Compiler:Utils:VisualizeGoalExecutionOrder',
	[	generate_dot_visualization/2,
		visualize_term_structure/2]
).

:- use_module('../Utils.pl').



generate_dot_visualization(Term,OutputFile) :-
	visualize_term_structure(Term,DotFile),
	open(OutputFile,write,Stream),
	write(Stream,DotFile),
	close(Stream).



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
	% If there are no (open) choicepoints when this goal is reached, then it
	% is irrelevant if in the left branch has a cut, as that cut will never
	% cut of any choice points created (later) in the right branch.
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
	% When we reach this node (this "or" goal), a choice point is created and 
	% the right branch is the target if the evaluation of the left branch fails
	% (and unless another cut is encountered...) hence, "RelevantCutCall" is set
	% to false because (at least) this choice point is not cut.
	node_successors(LGoal,BranchSucceedsNID,MinR,CutBranchFailsNID,false,LCuts,NLGoal),
	% The right branch is only evaluated if cut is not called during the 
	% evaluation of the left branch; however, previous choice points may be
	% cut and, hence, when evaluating the right branch we have to the previous
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



numbered_term_node_idatom(and(MinL,MinR,Max,_),ID) :- atomic_list_concat([and,'_',MinL,'_',MinR,'_',Max],ID).
numbered_term_node_idatom(or(MinL,MinR,Max,_),ID) :- atomic_list_concat([or,'_',MinL,'_',MinR,'_',Max],ID).
numbered_term_node_idatom(goal(ID,_G,_RelevantCutCall,_SID,_FID,_CFID),ID).



numbered_term_exit_node_id(goal(_ID,_G,_RelevantCutCall,EID,EID,EID),EID) :- !.
numbered_term_exit_node_id(and(_MinL,_MinR,Max,_),Max) :- !.
numbered_term_exit_node_id(or(_Min,_MinR,Max,_),Max) :- !.



is_cut(Term) :- nonvar(Term),Term = !.



is_cut(Term,Cut) :- is_cut(Term),!,Cut=true.
is_cut(_Term,false).



visualize_term_structure(Clause,DotFile) :- 
	Clause = ':-'(H,B),
	!,
	name_variables(H,'H',1,_AID),
	name_variables(B,'B',1,_VID),
	visualize_term_structure(B,GraphDescription,EID),
	term_to_atom(Clause,A),
	sanitize_atom(A,SA),
	atomic_list_concat([
		'digraph G {\n',
		'label="Term: ',SA,'";\nnode [shape=none];\nedge [arrowhead=none];\n',
		GraphDescription,
		EID,' [label="Exit",shape="Box"];\n',
		'}'],DotFile).
visualize_term_structure(Term,DotFile) :- 
	name_variables(Term,'V',1,_VID),
	visualize_term_structure(Term,GraphDescription,EID),
	term_to_atom(Term,A),
	sanitize_atom(A,SA),
	atomic_list_concat([
		'digraph G {\n',
		'label="Term: ',SA,'";\nnode [shape=none];\nedge [arrowhead=none];\n',
		GraphDescription,
		EID,' [label="Exit",shape="Box"];\n',
		'}'],DotFile).


visualize_term_structure(Term,GraphDescription,ExitID) :- 
	number_term_nodes(Term,NT),
	node_successors(NT,NTwithSucc),
	visualize_numbered_term(NTwithSucc,[],DF),
	atomic_list_concat(DF,GraphDescription),
	numbered_term_exit_node_id(NTwithSucc,ExitID).



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
	sanitize_atom(A,SA),
	(
		( 
			RelevantCutCall = true,!,
			atomic_list_concat([
					V,' [label="',SA,'",fontcolor=maroon];\n',
					V,' -> ',SID,' [color=green,constraint=false,arrowhead=normal];\n',
					V,' -> ',CFID,' [label="!",color=maroon,constraint=false,arrowhead=normal];\n'
				],ID)
		)
		;
		( 
			RelevantCutCall = false,!,
			atomic_list_concat([
					V,' [label="',SA,'"];\n',
					V,' -> ',SID,' [color=green,constraint=false,arrowhead=normal];\n',
					V,' -> ',FID,' [color=red,constraint=false,arrowhead=normal];\n'
				],ID)
		)
		;
		(
			RelevantCutCall = maybe,!,
			atomic_list_concat([
					V,' [label="',SA,'",fontcolor=darksalmon];\n',
					V,' -> ',SID,' [color=green,constraint=false,arrowhead=normal];\n',
					V,' -> ',FID,' [color=salmon,constraint=false,arrowhead=normal];\n',
					V,' -> ',CFID,' [label="!",color=pink,constraint=false,arrowhead=normal];\n'	
				],ID)		
		)
	),
	DotFile=[ID|CurrentDotFile].



sanitize_atom(A,NA) :-
	string_to_atom(S,A),
	string_to_list(S,SL),
	sanitize_string(SL,SS),
	string_to_atom(SS,NA).



sanitize_string(S,NewS) :-
	replace_char_with_string(S,"\\","\\\\",S1),
	replace_char_with_string(S1,"\"","\\\"",S2),
	replace_char_with_string(S2,"\n","\\\n",S3),
	replace_char_with_string(S3,"\t","\\\t",NewS).



name_variables(Term,Type,ID,NewID) :-
	var(Term),
	!,
	atomic_list_concat([Type,ID],Term),
	NewID is ID + 1.
name_variables([Term|Terms],Prefix,ID,NewID) :-
	% This special case is required to not run in 
	% endless recursion (cf. next clause).
	name_variables(Term,Prefix,ID,NID),
	name_variables(Terms,Prefix,NID,NewID),
	!.	
name_variables(Term,Type,ID,NewID) :-
	compound(Term),!,
	Term =.. [_|Args],
	name_variables(Args,Type,ID,NewID).
% The base case (i.e., _Term is an atom):
name_variables(_Term,_Type,ID,ID) :- 
	true.