% Assumption: 
%	The 'Goal' we are searching for is unique
% 	All goals are not (semi-)deterministic

%successors(Goal,ClauseBody,T,F,GoalSuccessors) :- 



successors(Goal, Body ,T,F, GoalSuccessors) :- 
	( Body =  (L,R) ; Body = (L *-> R) ),
	(
		next_goal(R,RNextGoal), % the next goal is always unique...
		successors(Goal,L,RNextGoal,F,GoalSuccessors)	
	;
		last_goals(L,LLastGoals),
		successors(Goal,R,T,LLastGoals,GoalSuccessors)	
	),!.
successors(Goal, (If *-> Then ; Else) ,T,F,GoalSuccessors) :- 
	next_goal(Then,ThenNextGoal),
	next_goal(Else,ElseNextGoal),
	(
		successors(Goal,If,ThenNextGoal,[ElseNextGoal],GoalSuccessors)	
	;
		last_goals(If,IfLastGoals),
		successors(Goal,Then,T,IfLastGoals,GoalSuccessors)	% if "then" fails, else is not tried!
	;
		successors(Goal,Else,T,F,GoalSuccessors)	
	),!.
successors(Goal, (If -> Then ; Else) ,T,F,GoalSuccessors) :- 
	/*
	?- ((true -> fail) ; true) ; true.
	true.

	?- ((true -> fail) ; true).
	false.
	*/
	next_goal(Then,ThenNextGoal),
	next_goal(Else,ElseNextGoal),
	(	% the goal we are searching for is in the if branch...
		successors(Goal,If,ThenNextGoal,[ElseNextGoal],GoalSuccessors)	
	;	
		successors(Goal,Then,T,F,GoalSuccessors)	% if the "then" goal fails, else is not tried and the if condition is not evaluated again!
	;
		successors(Goal,Else,T,F,GoalSuccessors)	
	),!.
successors(Goal, (If -> Then) ,T,F,GoalSuccessors) :- 
	(	% the goal we are searching for is in the if branch...
		next_goal(Then,ThenNextGoal),
		successors(Goal,If,ThenNextGoal,F,GoalSuccessors)	
	;	
		successors(Goal,Then,T,F,GoalSuccessors)	% if the "then" goal fails, else is not tried and the if condition is not evaluated again!
	),!.	
successors(Goal, (L;R) ,T,F,GoalSuccessors) :- 
	(
		next_goal(R,RNextGoal),
		successors(Goal,L,T,[RNextGoal],GoalSuccessors)	
	;
		successors(Goal,R,T,F,GoalSuccessors)	
	),!.
successors(Goal, Goal ,T,F, succ(Goal,t(T),f(F))) :- !.




next_goal((L , _R),G) :- !,next_goal(L,G).
%[not required] next_goal((If -> _Then ; _Else),G) :- !,next_goal(If,G).
next_goal((If -> _Then),G) :- !,next_goal(If,G).
%[not required] next_goal((Condition *-> _Action ; _Else),G) :- !,next_goal(Condition,G).
next_goal((Condition *-> _Action),G) :- !,next_goal(Condition,G).
next_goal((L ; _R),G) :- !,next_goal(L,G).
next_goal(G,G).


% The following predicate does not take "cuts" into consideration.
last_goals(ComplexGoal,LGs) :- last_goals(ComplexGoal,LGs,[]).
/**
	The last goal that (potentially) was successfully called if the goal as a whole has succeeded
*/ 
last_goals((_L , R),S1,SZ) :- !,last_goals(R,S1,SZ).
last_goals((_If -> Then ; Else),S1,SZ) :- !,last_goals(Then,S1,S2),last_goals(Else,S2,SZ).
last_goals((_If -> Then),S1,SZ) :- !,last_goals(Then,S1,SZ).
last_goals((_Condition *-> Action ; Else),S1,SZ) :- !,last_goals(Action,S1,S2),last_goals(Else,S2,SZ).
last_goals((_Condition *-> Action),S1,SZ) :- !,last_goals(Action,S1,SZ). % the same as the conjunction (Condition,Action)
last_goals((L ; R),S1,SZ) :- !,last_goals(L,S1,S2),last_goals(R,S2,SZ).
last_goals(G,[G|SZ],SZ).




%did_cut(ComplexGoal,Behavior) :- ...
did_cut((L,R),Behavior) :- 
	did_cut(L,LBehavior),
	did_cut(R,RBehavior),	
	(
		(LBehavior = always; RBehavior =always), Behavior = always
	;
		(LBehavior = sometimes; RBehavior = sometimes), Behavior = sometimes
	;
		Behavior = never
	),!.
did_cut((L;R),Behavior) :- 
	did_cut(L,LBehavior),
	did_cut(R,RBehavior),	
	(
		(LBehavior = always), Behavior = always % DO GENERATE A DEAD CODE WARNING
	;
		(LBehavior = always ; RBehavior = always ; LBehavior = sometimes; RBehavior = sometimes), Behavior = sometimes
	;
		Behavior = never
	),!.
did_cut(!,always) :- !.
did_cut(_,never) :- !.
