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


successors(( (A,B) , S1,SZ  ) :- !, 
	first(B,FB),
	last_if_true(A,LTA,[]),
	last_if_false(B,LFB), 
	findall(Succ,(member(AnA,LTA),Succ = succ(AnA,FB)),S1,S2),
	S2 = [fails(LFB,LTA)|S3],
	successors(A,S3,S4),
	successors(B,S4,SZ).
successors( (A;B) , S1,SZ  ) :- !, 
	first(B,FB),
	first(A,FA),
	S1 = [fails(FA,[FB])|S2],
	successors(A,S2,S3),
	successors(B,S3,SZ).	
successors( _ , SZ,SZ).	



% there may be multiple goals... we use a difference list
last_if_true( (_A,B), H,T ) :- !, last_if_true(B,H,T).
last_if_true( (A;B), H,NewT ) :- !, last_if_true(A,H,IT),last_if_true(B,IT,NewT).
last_if_true( X , [X|T],T ).



last_if_false( (A,_B), X ) :- !, last_if_false(A,X).
last_if_false( (_A;B), X ) :- !, last_if_false(B,X).
last_if_false( X , X ).


first( (A ,_B), X ) :- !, first(A,X).
first( (A ;_B), X ) :- !, first(A,X).
% first( (A -> _B), X ) :- !, first(A,X).
% first( (A *-> _B), X ) :- !, first(A,X).
first( X , X ).


/*
goal_list( (C -> B ) ,S1,SZ ) :- !,
	goal_list( C, S1, S2 ),
	goal_list( B, S2, SZ ).
goal_list( (C *-> B ) ,S1,SZ ) :- !,
	goal_list( C, S1, S2 ),
	goal_list( B, S2, SZ ).
*/
goal_list( (A,B) , S1 , SZ  ) :- !,
	goal_list( A, S1, S2 ),
	goal_list( B, S2, SZ ).
goal_list( (A;B) , S1 , SZ  ) :- !,
	goal_list( A, S1, S2 ),
	goal_list( B, S2, SZ ).
goal_list( G , [G|SZ],SZ).	


