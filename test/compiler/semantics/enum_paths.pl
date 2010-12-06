% Assumption: 
%	The 'Goal' we are searching for is unique
% 	All goals are not (semi-)deterministic

%successors(Goal,ClauseBody,T,F,FC,GoalSuccessors) :- 



successors(Goal, Body ,T,F,FC, GoalSuccessors) :- 
	( Body =  (L,R) ; Body = (L *-> R) ),
	(
		next_goal(R,RNextGoal), % the next goal is always unique...
		successors(Goal,L,RNextGoal,F,FC,GoalSuccessors)	
	;
		last_goals(L,LLastGoals),
		successors(Goal,R,T,LLastGoals,FC,GoalSuccessors)	
	),!.
successors(Goal, (If *-> Then ; Else) ,T,F,FC,GoalSuccessors) :- 
	next_goal(Then,ThenNextGoal),
	next_goal(Else,ElseNextGoal),
	(
		successors(Goal,If,ThenNextGoal,[ElseNextGoal],FC,GoalSuccessors)	
	;
		last_goals(If,IfLastGoals),
		successors(Goal,Then,T,IfLastGoals,FC,GoalSuccessors)	% if "then" fails, else is not tried!
	;
		successors(Goal,Else,T,F,FC,GoalSuccessors)	
	),!.
successors(Goal, (If -> Then ; Else) ,T,F,FC,GoalSuccessors) :- 
	/*
	?- ((true -> fail) ; true) ; true.
	true.

	?- ((true -> fail) ; true).
	false.
	*/
	next_goal(Then,ThenNextGoal),
	next_goal(Else,ElseNextGoal),
	(	% the goal we are searching for is in the if branch...
		successors(Goal,If,ThenNextGoal,[ElseNextGoal],FC,GoalSuccessors)	
	;	
		successors(Goal,Then,T,F,FC,GoalSuccessors)	% if the "then" goal fails, else is not tried and the if condition is not evaluated again!
	;
		successors(Goal,Else,T,F,FC,GoalSuccessors)	
	),!.
successors(Goal, (If -> Then) ,T,F,FC,GoalSuccessors) :- 
	(	% the goal we are searching for is in the if branch...
		next_goal(Then,ThenNextGoal),
		successors(Goal,If,ThenNextGoal,F,FC,GoalSuccessors)	
	;	
		successors(Goal,Then,T,F,FC,GoalSuccessors)	% if the "then" goal fails, else is not tried and the if condition is not evaluated again!
	),!.	
successors(Goal, (L;R) ,T,F,FC,GoalSuccessors) :- 
	(
		next_goal(R,RNextGoal),
		successors(Goal,L,T,[RNextGoal],FC,GoalSuccessors)	
	;
		successors(Goal,R,T,F,FC,GoalSuccessors)	
	),!.
successors(Goal, Goal ,T,F,FC, succ(Goal,t(T),f(F),fc(FC))) :- !.




next_goal((L , _R),G) :- !,next_goal(L,G).
%[not required] next_goal((If -> _Then ; _Else),G) :- !,next_goal(If,G).
next_goal((If -> _Then),G) :- !,next_goal(If,G).
%[not required] next_goal((Condition *-> _Action ; _Else),G) :- !,next_goal(Condition,G).
next_goal((Condition *-> _Action),G) :- !,next_goal(Condition,G).
next_goal((L ; _R),G) :- !,next_goal(L,G).
next_goal(G,G).



last_goals(GSeq,LGs) :- last_goals(GSeq,LGs,[]).
/**
	The last goal that (potentially) was successfully called if the goal as a whole has succeeded
*/ 
last_goals((_L , R),S1,SZ) :- !,last_goals(R,S1,SZ).
last_goals((_If -> Then ; Else),S1,SZ) :- !,last_goals(Then,S1,S2),last_goals(Else,S2,SZ).
last_goals((_If -> Then),S1,SZ) :- !,last_goals(Then,S1,SZ).
last_goals((_Condition *-> Action ; Else),S1,SZ) :- !,last_goals(Action,S1,S2),last_goals(Else,S2,SZ).
last_goals((_Condition *-> Action),S1,SZ) :- !,last_goals(Action,S1,SZ). % the same as the conjunction (Condition,Action)
last_goals((L ; R),S1,SZ) :- !,last_goals(L,S1,S2),last_goals(R,S2,SZ).
last_goals(G,[G|T],T).



/*
last_goals(Goal,PrevGoals) :- findall(PrevGoal,last_goal(Goal,PrevGoal),PrevGoals).

/ **
	The last goal that (potentially) was successfully called if the goal as a whole has succeeded
* / 
last_goal((_L , R),G) :- !,last_goal(R,G).
last_goal((_If -> Then ; Else),G) :- !,(last_goal(Then,G);last_goal(Else,G)).
last_goal((_If -> Then),G) :- !,last_goal(Then,G).
last_goal((_Condition *-> Action ; Else),G) :- !,(last_goal(Action,G);last_goal(Else,G)).
last_goal((_Condition *-> Action),G) :- !,last_goal(Action,G). % the same as the conjunction (Condition,Action)
last_goal((L ; R),G) :- !,(last_goal(L,G);last_goal(R,G)).
last_goal(G,G).
*/