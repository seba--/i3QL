% Autor:
% Datum: 08.11.2010
/* Test the formatter. */

:- ensure_loaded('src/compiler/Lexer.pl').
:- ensure_loaded('src/compiler/Parser.pl').
:- ensure_loaded('src/compiler/Formatter.pl').

do_formatting(FileName,Solution,Options) :-
        atomic_list_concat(['test/compiler/formatter/data/',FileName],File),
        atomic_list_concat(['test/compiler/formatter/data/',Solution],FileSolution),
        
        tokenize_file(File,Ts,Options), clauses(Ts,Cs), format_file(Cs,'',Out), nl,write('###FORMATTED###\n'),write(Out),
        
        load_solution(FileSolution,Lines),
        Out = Lines.

load_solution(Solution,Lines) :-
   open(Solution,read,Stream),
   readLines(Stream,Out),
   atomic_list_concat(Out,Lines),
   nl,write('###SOLUTION###\n'),write(Lines),
   close(Stream).

readLines(Stream,[]):-
   at_end_of_stream(Stream).

readLines(Stream,[X|T]):-
   \+ at_end_of_stream(Stream),
   get_char(Stream,X),
   readLines(Stream,T).

:- begin_tests(formatter).

 test(emptyFormatter) :- do_formatting('EmptyTest.pl','EmptyTest_formatted.pl',[]).

 test(whitespaces1) :- do_formatting('Whitespace1.pl','Whitespace1_formatted.pl',[]).

 test(whitespaces2) :- do_formatting('Whitespace2.pl','Whitespace2_formatted.pl',[]).

 :- end_tests(formatter).