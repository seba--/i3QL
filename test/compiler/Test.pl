% Lexer
:- ensure_loaded('test/compiler/lexer/Test.pl').

% Parser        
:- ensure_loaded('test/compiler/parser/Lists.pl').
:- ensure_loaded('test/compiler/parser/op-Directive.pl').
:- ensure_loaded('test/compiler/parser/CornerCases.pl').
:- ensure_loaded('test/compiler/parser/Errors.pl').
:- ensure_loaded('test/compiler/parser/Overall.pl').

% Formatter
:- ensure_loaded('test/compiler/formatter/Test.pl').

:- ensure_loaded('test/compiler/data_flow/intra_clause_variable_usage.pl').
:- ensure_loaded('test/compiler/data_flow/Messages.pl').
