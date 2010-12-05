%successors(Term,TS) :- 


% Assumption: 
%	The 'Goal' we are searching for is unique
% 	All goals are not (semi-)deterministic
%successors(Goal,ClauseBody,T,F,FC,GoalSuccessors) :- 
	
successors(Goal, Goal ,T,F,FC, succ(Goal,t(T),f(F),fc(FC))) :- !.
successors(Goal, (L,R) ,T,F,FC, GoalSuccessors) :- 
	(
		next_goal(R,RNextGoal), % the next goal is always unique...
		successors(Goal,L,RNextGoal,F,FC,GoalSuccessors)	
	;
		last_goals(L,LLastGoals),
		successors(Goal,R,T,LLastGoals,FC,GoalSuccessors)	
	),!.
successors(Goal, (L;R) ,T,F,FC,GoalSuccessors) :- 
	(
		next_goal(R,RNextGoal),
		successors(Goal,L,T,[RNextGoal],FC,GoalSuccessors)	
	;
		successors(Goal,R,T,F,FC,GoalSuccessors)	
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

successors(Goal, (If *-> Then ; Else) ,T,F,FC,GoalSuccessors) :- 
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
		last_goals(If,IfLastGoals),
		successors(Goal,Then,T,IfLastGoals,FC,GoalSuccessors)	% if "then" fails, else is not tried!
	;
		successors(Goal,Else,T,F,FC,GoalSuccessors)	
	),!.


	
next_goal((L , _R),G) :- !,next_goal(L,G).
next_goal((If -> _Then ; _Else),G) :- !,next_goal(If,G).
next_goal((If -> _Then),G) :- !,next_goal(If,G).
next_goal((L ; _R),G) :- !,next_goal(L,G).
%next_goal((A *-> _B),G) :- !,next_goal(A,G).
next_goal(G,G).

last_goals(Goal,PrevGoals) :- findall(PrevGoal,last_goal(Goal,PrevGoal),PrevGoals).

/**
	The last goal that (potentially) was successfully called before the goal as a whole was evaluated to true
*/ 
last_goal((_L , R),G) :- !,last_goal(R,G).
last_goal((_If -> Then ; Else),G) :- !,(last_goal(Then,G);last_goal(Else,G)).
last_goal((_If -> Then),G) :- !,last_goal(Then,G).
last_goal((L ; R),G) :- !,(last_goal(L,G);last_goal(R,G)).
%last_goal((A *-> _B),G) :- !,last_goal(A,G).
last_goal(G,G).