/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.bytecode

import instructions.{SwitchInstructionInfo, ReturnInstructionInfo, BranchInstructionInfo, InstructionInfo}
import sae.LazyView
import sae.syntax.sql._
import structure.MethodDeclaration

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 11.09.12
 * Time: 16:24
 */

trait BytecodeCFG
{
    /*
    % cfg(MID, CFG) :- Given a valid MID, that CFG is returned as a list structure.
    %       The CFG is a list of tuples for each basic block (BB):
    %       CFG = [ BasicBlockTuple_1, ..., BasicBlockTuple_N].
    %       Each tuple contains the boundaries of the basic block in the first parameter
        %       and holds a list of successor basic blocks (also encoded as boundaries)
    %       as second parameter:
        %       BasicBlockTuple = ( (BBStart, BBEnd), [(Succ_1_BBStart, Succ_1_BBEnd), ...])
    cfg(MID, CFG) :-
        instr(MID, 0, FirstInstr),
    cfg_instruction_rec(MID, 0, MethodEndPC, FirstInstr, _, BasicBlockBorderSet, BasicBlockSuccessorSet),
    treeset_to_list_within_bounds(BasicBlockBorderSet, BasicBlockBordersWithoutFallThrough-[], 0, MethodEndPC),
    cfg_add_fall_through_cases(BasicBlockBordersWithoutFallThrough, BasicBlockSuccessorSet),
    (
        method_exceptions_table(MID,Handlers) ->
            cfg_add_exception_handler(Handlers, BasicBlockBorderSet, BasicBlockSuccessorSet)
    ;
    true
    ),
    treeset_to_list_within_bounds(BasicBlockBorderSet, BasicBlockBorders-[], 0, MethodEndPC),
    cfg_graph_list(0, BasicBlockBorders, BasicBlockBorders, BasicBlockSuccessorSet, CFG).
    */

    def instructions: LazyView[InstructionInfo]

    def branchInstructions: LazyView[BranchInstructionInfo] = SELECT ((_: InstructionInfo).asInstanceOf[BranchInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[BranchInstructionInfo])

    def returnInstructions: LazyView[ReturnInstructionInfo] = SELECT ((_: InstructionInfo).asInstanceOf[ReturnInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[ReturnInstructionInfo])

    def switchInstructions: LazyView[SwitchInstructionInfo] = SELECT ((_: InstructionInfo).asInstanceOf[SwitchInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[SwitchInstructionInfo])

    def basicBlockEndPcs: LazyView[(MethodDeclaration, Int)] =
        SELECT ((branch: BranchInstructionInfo) => (branch.declaringMethod, branch.pc)) FROM branchInstructions UNION_ALL (
            SELECT ((branch: BranchInstructionInfo) => (branch.declaringMethod, branch.branchOffset - 1)) FROM branchInstructions WHERE (_.branchOffset >= 1)
            ) UNION_ALL (
            SELECT ((retrn: ReturnInstructionInfo) => (retrn.declaringMethod, retrn.pc)) FROM returnInstructions
            ) UNION_ALL (
            SELECT ((switch: SwitchInstructionInfo) => (switch.declaringMethod, switch.pc)) FROM switchInstructions
            ) UNION_ALL (
            SELECT (*) FROM (
                    SELECT ((switch: SwitchInstructionInfo) => (switch.declaringMethod, switch.defaultOffset - 1)) FROM switchInstructions
                ) WHERE (_._2 >= 0)
            ) UNION_ALL (
            SELECT ((switch: SwitchInstructionInfo, jumpOffset: Some[Int]) => (switch.declaringMethod, jumpOffset.get - 1)) FROM (switchInstructions, ((_: SwitchInstructionInfo).jumpOffsets.map (Some (_))) IN switchInstructions)
            )
}

/*
/* The (obsolete) instructions jsr and ret are not supported. */
% TODO (uncomment?) basic_block_end_pcs(_, jsr(_), _) :- !,fail,
% TODO (uncomment?) basic_block_end_pcs(_, ret(_), _) :- !,fail.

basic_block_end_pcs(Pc, if_cmp_const(_, Target), [Pc|PrevList]) :- !, % if_cmp_const marks the end of a basic block
        (Target >= 1) ->
        (       % the jump target ist not the very first instruction
        EndPc is Target - 1,  % the instruction before Target is also the end of a basic block
        PrevList = [EndPc]
                )
                ;       % the jump target is the very first instruction of the method
                PrevList = [].

basic_block_end_pcs(Pc, if_cmp(_, _, Target), [Pc|PrevList]) :- !, % if_cmp marks the end of a basic block
                       (Target >= 1) ->
                       (
                           EndPc is Target - 1, % the instruction before Target is also the end of a basic block
                           PrevList = [EndPc]
                       );
                       PrevList = [].

basic_block_end_pcs(Pc, goto(Target), [Pc|PrevList]) :- !, % goto marks the end of a basic block.
                       (Target >= 1) ->
                       (
                           EndPc is Target - 1, % the instruction before Target is also the end of a basic block
                           PrevList = [EndPc]
                       );
                       PrevList = [].


basic_block_end_pcs(Pc, return(_), [Pc]) :- !. % a return statement always marks the end of a basic block.

basic_block_end_pcs(Pc, tableswitch(DefaultTarget,_,_,JumpTargets), List) :- !,
                       List = [Pc|DefaulTargetEndPcList],
                       tableswitch_target_end_pc_list(JumpTargets, JumpTargetEndPcList),
                       DefaultTargetEndPc is DefaultTarget - 1,
                       (
                            (DefaultTargetEndPc >= 0, DefaulTargetEndPcList = [DefaultTargetEndPc|JumpTargetEndPcList])
                                                                        ;
                            (DefaultTargetEndPc  < 0, DefaulTargetEndPcList = JumpTargetEndPcList)
                       ).

basic_block_end_pcs(Pc, lookupswitch(DefaultTarget,_,JumpTargets), List) :- !,
                       List = [Pc|DefaulTargetEndPcList],
                       lookupswitch_target_end_pc_list(JumpTargets, JumpTargetEndPcList),
                       DefaultTargetEndPc is DefaultTarget - 1,
                       (
                            (DefaultTargetEndPc >= 0, DefaulTargetEndPcList = [DefaultTargetEndPc|JumpTargetEndPcList]);
                            (DefaultTargetEndPc  < 0, DefaulTargetEndPcList = JumpTargetEndPcList)
                       ).


basic_block_end_pcs(Pc, athrow, [Pc]) :- !. % a throw statement always marks the end of a basic block.


% tableswitch_target_end_pc(JumpTargetList, EndPcList) :-
%                      Determines all EndPc's in EndPcList that stem from the given jump targets
tableswitch_target_end_pc_list([Target|RestTargets], List) :- !,
                        EndPc is Target - 1,
                        (
                             (
                                  EndPc >= 0,!,
                                  List = [EndPc| RestEndPcs]
                             );
                             (
                                  EndPc  < 0,
                                  List = RestEndPcs
                             )
                        ),
                        tableswitch_target_end_pc_list(RestTargets, RestEndPcs).
tableswitch_target_end_pc_list([], []).

% lookupswitch_target_end_pc(JumpTargetList, EndPcList) :-
%                      Determines all EndPc's in EndPcList that stem from the given jump targets
lookupswitch_target_end_pc_list([kv(_,Target)|RestTargets], List) :- !,
                        EndPc is Target - 1,
                        (
                             (
                                  EndPc >= 0,!,
                                  List = [EndPc| RestEndPcs]
                             );
                             (
                                  EndPc  < 0,
                                  List = RestEndPcs
                             )
                        ),
                        lookupswitch_target_end_pc_list(RestTargets, RestEndPcs).
lookupswitch_target_end_pc_list([], []).



% basic_block_edge_list(Pc, Instr, EdgeList) :-
%
basic_block_edge_list(Pc, if_cmp_const(_, Target), [(Pc, NextPc), (Pc, Target)]) :- !,
                     (NextPc is Pc + 1).                     % Fall-through case for if: we rule out pc's that are greater than the bounds later


basic_block_edge_list(Pc, if_cmp(_, _, Target), [(Pc, NextPc), (Pc, Target)]) :- !,
                     (NextPc is Pc + 1).                     % Fall-through case for if: we rule out pc's that are greater than the bounds later

basic_block_edge_list(Pc, goto(Target), [(Pc, Target)]) :- !.

basic_block_edge_list(Pc, return(_), [(Pc, end)]) :- !.

basic_block_edge_list(Pc, athrow, [(Pc, end)]) :- !.

basic_block_edge_list(Pc, tableswitch(DefaultTarget,_,_,JumpTargets), [(Pc, DefaultTarget) | JumpTargetsList]) :- !,
                     tableswitch_target_edge_list(JumpTargets, Pc, JumpTargetsList).

basic_block_edge_list(Pc, lookupswitch(DefaultTarget,_,JumpTargets), [(Pc, DefaultTarget) | JumpTargetsList]) :- !,
                     lookupswitch_target_edge_list(JumpTargets, Pc, JumpTargetsList).


% tableswitch_target_edge_list(JumpTargetList, Pc, EdgeList) :-
%
tableswitch_target_edge_list([Target|RestTargets], Pc, [(Pc, Target)|RestEdges])  :- !,
                       tableswitch_target_edge_list(RestTargets, Pc, RestEdges).
tableswitch_target_edge_list([Target], Pc, [(Pc, Target)]).

% lookupswitch_target_edge_list(JumpTargetList, Pc, EdgeList) :-
%
lookupswitch_target_edge_list([kv(_,Target)|RestTargets], Pc, [(Pc, Target)|RestEdges]) :- !,
                       lookupswitch_target_edge_list(RestTargets, Pc, RestEdges).
lookupswitch_target_edge_list([kv(_,Target)], Pc, [(Pc, Target)]).







% cfg_graph_list(StartPc, BasicBlockBorderList, CompleteBasicBlockBorders, BasicBlockSuccessorSet, CFG) :-
%
%
cfg_graph_list(StartPc, [EndPc|RestPcList], CompleteBasicBlockBorders, BasicBlockSuccessorSet, [((StartPc, EndPc), BasicBlockSuccessors)|CFGListRest]) :-
      NextStartPc is EndPc + 1,
      treeset_to_list_within_bounds(BasicBlockSuccessorSet, PrecomputedSuccessorList-[], (EndPc,_), (NextStartPc,_)),
      cfg_graph_successors(PrecomputedSuccessorList, CompleteBasicBlockBorders, BasicBlockSuccessors),
      cfg_graph_list(NextStartPc, RestPcList, CompleteBasicBlockBorders, BasicBlockSuccessorSet, CFGListRest).

cfg_graph_list(_, [], _, _, []).


% cfg_graph_successors(BasicBlockSuccessorList, CompleteBasicBlockBorders, BasicBlockSuccessors) :-
%
%
cfg_graph_successors([(_, BBStartPc)|EdgeListRest], [BBEndPc|BasicBlockBordersRest], Successors) :-
      % traverse the list of basic block ends until we have reached one that is greater than the start pc yielded by the edge
      BBStartPc \= end,
      BBEndPc < BBStartPc, !,
      cfg_graph_successors([(_, BBStartPc)|EdgeListRest], BasicBlockBordersRest, Successors).

% TODO isn't it advantegeous to put this clause at the very beginning?
cfg_graph_successors([(_, BBStartPc)|EdgeListRest], [BBEndPc|BasicBlockBordersRest], [(BBStartPc, BBEndPc)|SuccessorRest]) :-
      BBStartPc \= end,
      BBEndPc >= BBStartPc, !,
      cfg_graph_successors(EdgeListRest, [BBEndPc|BasicBlockBordersRest], SuccessorRest).

cfg_graph_successors([(_, end)|EdgeListRest], BasicBlockBorders, Successors) :- !,
      % traverse the list of basic block ends until we have reached one that is greater than the start pc yielded by the edge
      cfg_graph_successors(EdgeListRest, BasicBlockBorders, Successors).

cfg_graph_successors([], _, []) :- !. % we are finished when the list of edges is empty. Then we yield the empty list for suceesors


% cfg_instruction_rec(MID, Pc, MethodEndPC, CurrentInstr, NextInstr, BasicBlockBorderSet, BasicBlockSuccessorSet) :-
%     This is the 'main loop' of the cfg algorithm. each instrcution is analyzed
%     once and respective modifications to basic block information is stored inside
%     BasicBlockBorderSet and BasicBlockSuccessorSet.
cfg_instruction_rec(MID, Pc, MethodEndPC, CurrentInstr, NextInstr, BasicBlockBorderSet, BasicBlockSuccessorSet) :-
      CurrentInstr \= 'n/a',
      NextPc is Pc + 1,

      (basic_block_end_pcs(Pc, CurrentInstr, EndPcList) -> list_to_treeset(EndPcList, BasicBlockBorderSet); EndPcList = []), % implication saves ~ 1-3 inferences per instruction
      (basic_block_edge_list(Pc, CurrentInstr, EdgeList) -> list_to_treeset(EdgeList, BasicBlockSuccessorSet); EdgeList = []), % implication saves ~ 1-3 inferences per instruction
      % If there are no successors check if the next instruction is a jump target, if yes construct a fall through case
      % Note that return and athrow instructions have a (Pc, exit) edge and thus do not apply here, which is the correct semantics
      % ((EdgeList = [], is_value_in_treeset(NextPc, BasicBlockSuccessorSet)) -> treeset_lookup((Pc, NextPc), BasicBlockSuccessorSet); true ),
      % add_fall_through_for_previous_instr(Pc, EdgeList, BasicBlockSuccessorSet),
      (instr(MID, NextPc, NextInstr) ; \+ instr(MID, NextPc, _), NextInstr = 'n/a'),
      !,
      cfg_instruction_rec(MID, NextPc, MethodEndPC, NextInstr, _, BasicBlockBorderSet, BasicBlockSuccessorSet).

cfg_instruction_rec(_, Pc, MethodEndPC, 'n/a', _, _, _) :- MethodEndPC is Pc - 1. % recursion anchor evaluated only once at the end.


cfg_add_fall_through_cases([EndPc|RestEndPcs], BasicBlockSuccessorSet) :-
      NextPc is EndPc + 1,
      (
         treeset_to_list_within_bounds(BasicBlockSuccessorSet, []-[], (EndPc, _), (NextPc, _)) ->    % we have an empty list and thus no edge yet
           treeset_lookup((EndPc, NextPc), BasicBlockSuccessorSet)
           ;
           true
      ),
      cfg_add_fall_through_cases(RestEndPcs, BasicBlockSuccessorSet).
cfg_add_fall_through_cases([], _) :- !. % green cut



% cfg_add_exception_handler(Handlers, BasicBlockBorderSet, BasicBlockSuccessorSet) :-
%
cfg_add_exception_handler([], _, _).
cfg_add_exception_handler([handler(TryBlockStartPC,EndPC,HandlerPC,_)| HandlersRest], BasicBlockBorderSet, BasicBlockSuccessorSet) :-
      PCBeforeTryBlock is TryBlockStartPC - 1,
      PCBeforeHandlerPCEnd is HandlerPC - 1,
      TryBlockEndPC is EndPC - 1,
      treeset_lookup(PCBeforeTryBlock, BasicBlockBorderSet),
      treeset_lookup(PCBeforeHandlerPCEnd, BasicBlockBorderSet),
      treeset_lookup(TryBlockEndPC, BasicBlockBorderSet),
      % if there is no edge yet from the TryBlockEndPC, then it is not a jump and we construct a fall through case.
      % EndPC is TryBlockEndPC + 1 => thus we can reuse the variable here to denote the jump target
      (treeset_to_list_within_bounds(BasicBlockSuccessorSet, []-[], (PCBeforeTryBlock, _), (TryBlockStartPC, _)) -> treeset_lookup((PCBeforeTryBlock, TryBlockStartPC), BasicBlockSuccessorSet); true),
      (treeset_to_list_within_bounds(BasicBlockSuccessorSet, []-[], (PCBeforeHandlerPCEnd, _), (HandlerPC, _)) -> treeset_lookup((PCBeforeHandlerPCEnd, HandlerPC), BasicBlockSuccessorSet); true),
      (treeset_to_list_within_bounds(BasicBlockSuccessorSet, []-[], (TryBlockEndPC, _), (EndPC, _)) -> treeset_lookup((TryBlockEndPC, EndPC), BasicBlockSuccessorSet); true),
      cfg_add_exception_handler(HandlersRest, BasicBlockBorderSet, BasicBlockSuccessorSet),
      % addition of edges can only be performed after all borders have been set thus we perform this step on the upward recursive step.
      % TODO try with tail recursion and two predicates
      (
          % we know that TryBlockEndPC is a trivial solution to this call, but we need to know all solutions anyway
          treeset_to_list_within_bounds(BasicBlockBorderSet, BBInTryBlockEndPCList-[], TryBlockStartPC, TryBlockEndPC),
          %treeset_lookup((BBInTryBlockEndPC, HandlerPC), BasicBlockSuccessorSet)
          add_catch_block_edge_list(BBInTryBlockEndPCList, HandlerPC, BasicBlockSuccessorSet)
      ).

% add_catch_block_edge_list(BBInTryBlockEndPCList, HandlerPC, BasicBlockSuccessorSet) :-
%
%
add_catch_block_edge_list([EndPC|RestEndPcs], HandlerPC, BasicBlockSuccessorSet) :-
      treeset_lookup((EndPC, HandlerPC), BasicBlockSuccessorSet),
      add_catch_block_edge_list(RestEndPcs, HandlerPC, BasicBlockSuccessorSet).

add_catch_block_edge_list([], _, _).
*/