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
	Predicates to operate on fixed-sized lists; i.e., in general the tail
	argument of the last element of the list that is processed is expected to be 
	bound to the empty list atom ('[]').<br/>
	If the tail argument of the last list element is not bound to the empty list, 
	the precise behavior is defined by the predicate. If the behavior is not 
	specified no assumption is allowed to be made, the behavior of the 
	implementation may change without notice.

	@see open_list
	@author Michael Eichberg (mail@michael-eichberg.de)
*/
:- module(
		list,
		[	% tests
			is_not_empty/1,
			is_list/1,
			
			% actions
			replace_all/4,
			
			% other
			write_elements/2
		]
	).



/**
	Tests if the specified argument is a proper list. I.e., it is a fixed size
	list where the tail of the last list element (<code>'.'(Head,Tail)</code>) is
	the empty list atom.<br />
	Does never cause instantiation of the given argument.
	
	@signature is_list(?List:term) is semidet
*/
is_list(List) :- var(List),!,fail.
is_list(List) :- /*nonvar(L),*/ List == [],!.
is_list(/*List=*/[_|T]) :- /*nonvar(List),*/ is_list(T).



/**
	Tests if the given argument is a non-empty list. <br/>
	Does never cause instantiation of the given argument.
	<p>
	<b>Examples</b><br/>
	<code><pre>
	?- is_not_empty(_).
	false.
	
	?- is_not_empty([]).
	false.
	
	?- is_not_empty([a]).
	true.
	
	?- is_not_empty([a,b|_]). 
	true.	
	
	?- is_not_empty([_]). 
	true.	
	</pre></code>
	</p>
	
	@signature is_not_empty(?Argument:term) is semidet
	@arg Argument An arbirtray term. 
*/
is_not_empty(X) :- nonvar(X), X = [_|_].



/**
 	Replaces all elements of the given list OldList that unify with OldElement with 
	the (same instance of the) element NewElement and unifies the result with 
	NewList. <br/>
	Does never cause instantiation of OldElement.
	<p>
	<b>Examples</b><br/>
	<code><pre>
	?- list:replace_all([a,b,c],Z,x,R).
	R = [x, x, x].

	?- list:replace_all([a,b,c],a,x,R).
	R = [x, b, c].

	?- list:replace_all([a,b,c],a,X,R).
	R = [X, b, c].

	?- list:replace_all([a,b,c,a,b,c],a,X,R).
	R = [X, b, c, X, b, c].

	?- list:replace_all([a,b,c,a,b,c|T],a,X,R).
	T = [],
	R = [X, b, c, X, b, c].

	?- list:replace_all([],a,_,R).
	R = [].

	?- list:replace_all([a],a,a,R).
	R = [a].
	
	?- list:replace_all([a(1),b,c,a(2)],a(_),a(X),R).
	R = [a(X), b, c, a(X)].
	</pre></code>
	</p>	
	
	@signature replace_all(OldList,OldElement,NewElement,NewList) is det
	@arg OldList 
*/
replace_all([],_,_,[]) :- !. 
replace_all([H|Tail],OldElement,NewElement,[NewElement|NewTail]) :- 
	\+ (H \= OldElement), % we don't want OldElement to become instantiated
   !, 
	replace_all(Tail,OldElement,NewElement,NewTail).
replace_all([H|Tail],OldElement,NewElement,[H|NewTail]) :- % nothing to do
	% H \= OldElement,
   replace_all(Tail,OldElement,NewElement,NewTail).



/**
	Calls <code>write(Stream,E)</code> for all elements (E) of the given list 
	(List = [E|Es]).<br />

	@signature write_elements(+Stream,+List) is det
*/
write_elements(_Stream,List) :- 
	var(List),
	!,
	throw(error( % ISO compliant
			instantiation_error,
			context(write_elements/2,'the list (2nd argument) has to be nonvar'))).
write_elements(Stream,List) :- 
	write_elements_loop(Stream,List).

write_elements_loop(_Stream,[]).
write_elements_loop(Stream,[E|Es]) :-
	write(Stream,E),
	write_elements_loop(Stream,Es).
