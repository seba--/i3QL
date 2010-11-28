% Autor:
% Datum: 08.11.2010
/* Test the formatter. */

:- ensure_loaded('src/compiler/Lexer.pl').
:- ensure_loaded('src/compiler/Parser.pl').
:- ensure_loaded('src/compiler/Formatter.pl').
:- ensure_loaded('src/compiler/FormatterOld.pl').

do_formating(FileName,Solution,Options) :-
        atomic_list_concat(['test/compiler/formatter/data/',FileName],File),
        %atomic_list_concat(['test/compiler/formatter/data/',Solution],FileSolution),
        tokenize_file(File,Ts,Options),
        clauses(Ts,Cs),
        %load_solution(FileSolution,Lines),
        %Console output
        format_file(Cs,'',Out),nl,write(Out).
        %flatten(Out,FlatOut),nl,
        %create_output(Out).
        %create_output(Lines).
        %format_file(Lines).

load_solution(Solution,Lines) :-
   open(Solution,read,Stream),
   readLines(Stream,Lines),
   close(Stream).

create_output([]) :- !.
create_output([H|T]) :- write(H),create_output(T).

readLines(Stream,[]):-
   at_end_of_stream(Stream).

readLines(Stream,[X|T]):-
   \+ at_end_of_stream(Stream),
   read(Stream,X),
   readLines(Stream,T).

:- begin_tests(formatter).
 
 test(whitespaces1,[true(Solution = 'Whitespace1_formatted.pl')]) :- do_formating('Whitespace1.pl',Solution,[]).
 
test( whitespaces2,
       [true(
                  Solution ='Whitespace2_formatted.pl'
       )]
 ) :- do_formating('Whitespace2.pl',Solution,[]).
% 
% test( noEmptyLines,
%        [true(
%                   Lines = ['Whitespace1_formatted.pl']
%        )]
%  ) :- do_formating('NoEmptyLines.pl',Lines,[]).
% 
% test( emptyLinesBetweenSameClauses,
%        [true(
%                   Lines = ['Whitespace1_formatted.pl']
%        )]
%  ) :- do_formating('LineBetweenSameClauses.pl',Ts,[]).
%  
%  test( twoLinesBetweenDiffClauses,
%        [true(
%                   Lines = ['Whitespace1_formatted.pl']
%        )]
%  ) :- do_formating('TwoLineBetweenDiffClauses.pl',Ts,[]).
% 
%   test( lists,
%        [true(
%                   Lines = ['Whitespace1_formatted.pl']
%        )]
%  ) :- do_formating('Lists.pl',Ts,[]).
% 
%   test( operatorsWhitespace,
%        [true(
%                   Lines = ['Whitespace1_formatted.pl']
%        )]
%  ) :- do_formating('OperatorsWhitespace.pl',Ts,[]).
% 
%    test( quoting_atoms,
%        [true(
%                   Lines = ['Whitespace1_formatted.pl']
%        )]
%  ) :- do_formating('QuotingAtoms.pl',Ts,[]).
 
 
 
%test(rg_test_Example_pl) :- do_tokenization('Example.pl',_Ts,[]).


%test(rg_test_Test_pl) :- tokenize_file('test/compiler/formatter/Test.pl',_Ts).


 :- end_tests(formatter).