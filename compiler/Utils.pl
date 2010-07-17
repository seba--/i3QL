/*	Definition of common helper predicates.
	
	@version $Date$ $Rev$
	@author Michael Eichberg
*/
:- module('Utils',
	[	lookup/3,
		lookup_first/2,
		replace_first/4,
		max/3
	]).


max(V1,V2,R) :- V1 =< V2,!, R = V2.
max(V1,V2,R) :- V1 > V2, R = V1.




/*	lookup_first(DL,E) :- succeeds if E is an element (more precisely: can be
	unified with an element) of the difference list DL. The operator used by
	the difference list is the "-".
*/
/*lookup_first(L-[],E) :- 
	memberchk(E,L). */
lookup_first([]-[],_E) :- 
	!, % green cut
	fail.	
lookup_first([E|_]-_,E) :-
	!. % green cut
lookup_first([E|Rest]-Last,OtherE) :- 
	E \= OtherE,
	lookup_first(Rest-Last,OtherE).

	



/* replace_first(OldDL,OldE,NewE,NewDL) :- NewDL is a difference list where
	the first occurence of OldE in the difference list OldDL is replaced with
	NewE.
*/
replace_first([]-[],_OldE,_NewE,NewDL-NewDL) :- !.
replace_first([OldE|OldDLR]-OldDLZ,OldE,NewE,[NewE|OldDLR]-OldDLZ) :-
	!. % just the first element is replaced
replace_first([SomeE|OldDLR]-OldDLZ,OldE,NewE,[SomeE|NewDLR]-NewDLZ) :-
	SomeE \= OldE,
	replace_first(OldDLR-OldDLZ,OldE,NewE,NewDLR-NewDLZ).




/* lookup(Key,Dict,Value) :- looks up a Key's Value stored in
	the Dict(ionary). If Key and Value are instantiated, then this predicate 
	just succeeds or adds the key-value pair to the dictionary, if it is not 
	contained in the dictionary.<br/>
	Dictionary is an incomplete list where the elements
	are key value pairs.	
*/
lookup(Key,[(Key,Value)|_Dict],Value).
lookup(Key,[(Key1,_)|Dict],Value) :- Key \= Key1, lookup(Key,Dict,Value).




