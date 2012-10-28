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

import instructions._
import sae.{SetRelation, Relation}
import sae.syntax.sql._
import structure._
import de.tud.cs.st.bat.resolved.ExceptionHandler
import sae.operators.impl.NotExistsInSameDomainView
import sae.functions.Sort
import com.google.common.collect.SortedMultiset

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 11.09.12
 * Time: 16:24
 */

trait BytecodeCFG
{
    def instructions: SetRelation[InstructionInfo]

    def codeAttributes: SetRelation[CodeAttribute]

    private lazy val exceptionHandlers: Relation[ExceptionHandlerInfo] =
        SELECT ((code: CodeAttribute, handler: ExceptionHandler) => ExceptionHandlerInfo (code.declaringMethod, handler)) FROM (codeAttributes, ((_: CodeAttribute).exceptionHandlers) IN codeAttributes)

    private lazy val ifBranchInstructions: Relation[IfBranchInstructionInfo] =
        SELECT ((_: InstructionInfo).asInstanceOf[IfBranchInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[IfBranchInstructionInfo])

    private lazy val gotoBranchInstructions: Relation[GotoBranchInstructionInfo] =
        SELECT ((_: InstructionInfo).asInstanceOf[GotoBranchInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[GotoBranchInstructionInfo])

    private lazy val returnInstructions: Relation[ReturnInstructionInfo] =
        SELECT ((_: InstructionInfo).asInstanceOf[ReturnInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[ReturnInstructionInfo])

    private lazy val switchInstructions: Relation[SwitchInstructionInfo] =
        SELECT ((_: InstructionInfo).asInstanceOf[SwitchInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[SwitchInstructionInfo])

    private lazy val switchJumpTargets: Relation[(SwitchInstructionInfo, Int)] =
        SELECT ((switch: SwitchInstructionInfo, jumpOffset: Some[Int]) => (switch, switch.pc + jumpOffset.get)) FROM (switchInstructions, ((_: SwitchInstructionInfo).jumpOffsets.map (Some (_))) IN switchInstructions)


    /**
     *
     */
    lazy val basicBlockEndPcs: Relation[BasicBlockEndBorder] =
        SELECT (*) FROM (
            SELECT ((branch: IfBranchInstructionInfo) => BasicBlockEndBorder (branch.declaringMethod, branch.pc)) FROM ifBranchInstructions UNION_ALL (
                SELECT ((branch: IfBranchInstructionInfo) => BasicBlockEndBorder (branch.declaringMethod, branch.pc + branch.branchOffset - 1)) FROM ifBranchInstructions
                ) UNION_ALL (
                SELECT ((branch: GotoBranchInstructionInfo) => BasicBlockEndBorder (branch.declaringMethod, branch.pc)) FROM gotoBranchInstructions
                ) UNION_ALL (
                SELECT ((branch: GotoBranchInstructionInfo) => BasicBlockEndBorder (branch.declaringMethod, branch.pc + branch.branchOffset - 1)) FROM gotoBranchInstructions
                ) UNION_ALL (
                SELECT ((retrn: ReturnInstructionInfo) => BasicBlockEndBorder (retrn.declaringMethod, retrn.pc)) FROM returnInstructions
                ) UNION_ALL (
                SELECT ((switch: SwitchInstructionInfo) => BasicBlockEndBorder (switch.declaringMethod, switch.pc)) FROM switchInstructions
                ) UNION_ALL (
                SELECT ((switch: SwitchInstructionInfo) => BasicBlockEndBorder (switch.declaringMethod, switch.pc + switch.defaultOffset - 1)) FROM switchInstructions
                ) UNION_ALL (
                SELECT ((e: (SwitchInstructionInfo, Int)) => BasicBlockEndBorder (e._1.declaringMethod, e._2 - 1)) FROM switchJumpTargets
                ) UNION_ALL (
                SELECT ((code: CodeAttribute) => BasicBlockEndBorder (code.declaringMethod, code.codeLength)) FROM codeAttributes
                )
            ) WHERE (_.endPc >= 0)


    lazy val immediateBasicBlockSuccessorEdges: Relation[SuccessorEdge] =
        (SELECT ((branch: IfBranchInstructionInfo) => FallThroughSuccessorEdge (branch.declaringMethod, branch.pc).asInstanceOf[SuccessorEdge]) FROM ifBranchInstructions
            ) UNION_ALL (
            SELECT ((branch: IfBranchInstructionInfo) => JumpSuccessorEdge (branch.declaringMethod, branch.pc, branch.pc + branch.branchOffset).asInstanceOf[SuccessorEdge]) FROM ifBranchInstructions
            ) UNION_ALL (
            SELECT ((branch: GotoBranchInstructionInfo) => JumpSuccessorEdge (branch.declaringMethod, branch.pc, branch.pc + branch.branchOffset).asInstanceOf[SuccessorEdge]) FROM gotoBranchInstructions
            ) UNION_ALL (
            SELECT ((retrn: ReturnInstructionInfo) => JumpSuccessorEdge (retrn.declaringMethod, retrn.pc, Int.MaxValue).asInstanceOf[SuccessorEdge]) FROM returnInstructions
            ) UNION_ALL (
            SELECT ((switch: SwitchInstructionInfo) => JumpSuccessorEdge (switch.declaringMethod, switch.pc, switch.pc + switch.defaultOffset).asInstanceOf[SuccessorEdge]) FROM switchInstructions
            ) UNION_ALL (
            SELECT ((e: (SwitchInstructionInfo, Int)) => JumpSuccessorEdge (e._1.declaringMethod, e._1.pc, e._2).asInstanceOf[SuccessorEdge]) FROM switchJumpTargets
            )

    /**
     * In the end each basic block that does not have a successor yet has to receive a fall through case
     */
    lazy val fallThroughCaseSuccessors: Relation[SuccessorEdge] =
        new NotExistsInSameDomainView (
            compile (SELECT ((b: BasicBlockEndBorder) => FallThroughSuccessorEdge (b.declaringMethod, b.endPc).asInstanceOf[SuccessorEdge]) FROM basicBlockEndPcs).asMaterialized,
            compile (SELECT ((s: SuccessorEdge) => FallThroughSuccessorEdge (s.declaringMethod, s.fromEndPc).asInstanceOf[SuccessorEdge]) FROM immediateBasicBlockSuccessorEdges).asMaterialized
        )
    /*
        SELECT ((b: BasicBlockEndBorder) => FallThroughSuccessorEdge (b.declaringMethod, b.endPc).asInstanceOf[SuccessorEdge]) FROM basicBlockEndPcs WHERE NOT (
            EXISTS (
                SELECT (*) FROM immediateBasicBlockSuccessorEdges WHERE ((_: SuccessorEdge).declaringMethod) === ((_: BasicBlockEndBorder).declaringMethod) AND ((_: SuccessorEdge).fromEndPc) === ((_: BasicBlockEndBorder).endPc)
            )
        )
    */

    lazy val basicBlockSuccessorEdges: Relation[SuccessorEdge] = {
        import sae.syntax.RelationalAlgebraSyntax._
        immediateBasicBlockSuccessorEdges ⊎ fallThroughCaseSuccessors
    }


    private def endBorderMethod: BasicBlockEndBorder => MethodDeclaration = _.declaringMethod

    private def startBorderMethod: BasicBlockStartBorder => MethodDeclaration = _.declaringMethod

    lazy val basicBlockStartPcs: Relation[BasicBlockStartBorder] =
        SELECT ((c: CodeAttribute) => BasicBlockStartBorder (c.declaringMethod, 0)) FROM codeAttributes UNION_ALL (
            SELECT ((edge: SuccessorEdge) => BasicBlockStartBorder (edge.declaringMethod, edge.toStartPc)) FROM basicBlockSuccessorEdges
            )

    private lazy val bordersAll: Relation[(MethodDeclaration, Int, Int)] = SELECT ((start: BasicBlockStartBorder, end: BasicBlockEndBorder) => (start.declaringMethod, start.startPc, end.endPc)) FROM (basicBlockStartPcs, basicBlockEndPcs) WHERE (startBorderMethod === endBorderMethod)

    lazy val borders: Relation[(MethodDeclaration, Int, Int)] = SELECT (*) FROM (bordersAll) WHERE ((e: (MethodDeclaration, Int, Int)) => (e._2 < e._3))


    lazy val sortedBasicBlockEndPcsByMethod: Relation[MethodBasicBlockEndBorders] = {
        import sae.syntax.RelationalAlgebraSyntax._
        γ (δ (basicBlockEndPcs),
            (_: BasicBlockEndBorder).declaringMethod,
            Sort ((_: BasicBlockEndBorder).endPc),
            (key: MethodDeclaration, value: SortedMultiset[Int]) => MethodBasicBlockEndBorders (key, value)
        )
    }


    lazy val sortedBasicBlockStartPcsByMethod: Relation[MethodBasicBlockStartBorders] = {
        import sae.syntax.RelationalAlgebraSyntax._
        γ (δ (basicBlockStartPcs),
            (_: BasicBlockStartBorder).declaringMethod,
            Sort ((_: BasicBlockStartBorder).startPc),
            (key: MethodDeclaration, value: SortedMultiset[Int]) => MethodBasicBlockStartBorders (key, value)
        )
    }



    lazy val basicBlocksNew: Relation[MethodBasicBlocks] = {
        import sae.syntax.RelationalAlgebraSyntax._
        (
            (
                sortedBasicBlockStartPcsByMethod,
                (_: MethodBasicBlockStartBorders).declaringMethod
                ) ⋈ (
                (_: MethodBasicBlockEndBorders).declaringMethod,
                sortedBasicBlockEndPcsByMethod
                )
            )
        {
            (start: MethodBasicBlockStartBorders, end: MethodBasicBlockEndBorders) => {
                val startIt = start.startBorders.entrySet ().iterator ()
                val endIt = end.endBorders.entrySet ().iterator ()
                var entries: List[(Int, Int)] = Nil
                while (startIt.hasNext && endIt.hasNext) {
                    entries = (startIt.next ().getElement, endIt.next ().getElement) :: entries
                }
                MethodBasicBlocks(start.declaringMethod, entries)
            }
        }
    }


    lazy val basicBlocks: Relation[BasicBlock] = {
        import sae.syntax.RelationalAlgebraSyntax._
        /*
         SELECT (   (e: (MethodDeclaration, Int, Int)) => (e._1, e._3),
                    MAX[(MethodDeclaration, Int, Int)]((e: (MethodDeclaration, Int, Int)) => e._2) )
          ) FROM borders GROUP_BY (e: (MethodDeclaration, Int, Int)) => (e._1, e._3)
          */
        γ (borders,
            (e: (MethodDeclaration, Int, Int)) => (e._1, e._3),
            sae.functions.Max[(MethodDeclaration, Int, Int)]((e: (MethodDeclaration, Int, Int)) => e._2),
            (key: (MethodDeclaration, Int), value: Int) => BasicBlock (key._1, value, key._2)
        )
    }

    /*
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

    /**
     * Handlers mark three new ends of blocks:
     * 1. the pc before the try block starts
     * 2. the pc before the try block ends (the try block end denotes the first pc after the block)
     * 3. the pc before the handler
     * After the endPc there may be a finally block which should reach until the handler comes.
     */
    private lazy val exceptionHandlerBasicBlockEndList: Relation[BasicBlockEndBorder] =
        SELECT ((e: ExceptionHandlerInfo) => BasicBlockEndBorder (e.declaringMethod, e.startPc - 1)) FROM exceptionHandlers UNION_ALL (
            SELECT ((e: ExceptionHandlerInfo) => BasicBlockEndBorder (e.declaringMethod, e.endPc - 1)) FROM exceptionHandlers
            ) UNION_ALL (
            SELECT ((e: ExceptionHandlerInfo) => BasicBlockEndBorder (e.declaringMethod, e.handlerPc - 1)) FROM exceptionHandlers
            )

    /**
     * Every endPc that is in the range between handler.startPc and handler.endPc - 1 must receive an edge to the handler
     */
    private lazy val exceptionHandlerBasicBlockSuccessors =
        SELECT ((e: ExceptionHandlerInfo) => JumpSuccessorEdge (e.declaringMethod, e.endPc - 1, e.handlerPc)) FROM (exceptionHandlers) UNION_ALL (
            SELECT ((e: (BasicBlockEndBorder, ExceptionHandlerInfo)) => JumpSuccessorEdge (e._1.declaringMethod, e._1.endPc, e._2.handlerPc)) FROM (
                SELECT (*) FROM (basicBlockEndPcs, exceptionHandlers) WHERE (
                    ((_: BasicBlockEndBorder).declaringMethod) === ((_: ExceptionHandlerInfo).declaringMethod)
                    )
                ) WHERE ((e: (BasicBlockEndBorder, ExceptionHandlerInfo)) => e._1.endPc < e._2.endPc - 1 && e._1.endPc >= e._2.startPc)
            )


}

/*
/* The (obsolete) instructions jsr and ret are not supported. */
% TODO (uncomment?) basic_block_end_pcs(_, jsr(_), _) :- !,fail,
% TODO (uncomment?) basic_block_end_pcs(_, ret(_), _) :- !,fail.
cfg_instruction_rec(MID, Pc, MethodEndPC, CurrentInstr, NextInstr, BasicBlockBorderSet, BasicBlockSuccessorSet) :-
%     This is the 'main loop' of the cfg algorithm. each instruction is analyzed
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
*/


/*
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
*/


/*
% cfg_graph_list(StartPc, BasicBlockBorderList, CompleteBasicBlockBorders, BasicBlockSuccessorSet, cfg) :-
    %
%
cfg_graph_list(StartPc, [EndPc|RestPcList], CompleteBasicBlockBorders, BasicBlockSuccessorSet, [((StartPc, EndPc), BasicBlockSuccessors)|CFGListRest]) :-
    NextStartPc is EndPc + 1,
    treeset_to_list_within_bounds(BasicBlockSuccessorSet, PrecomputedSuccessorList-[], (EndPc,_), (NextStartPc,_)),
    cfg_graph_successors(PrecomputedSuccessorList, CompleteBasicBlockBorders, BasicBlockSuccessors),
    cfg_graph_list(NextStartPc, RestPcList, CompleteBasicBlockBorders, BasicBlockSuccessorSet, CFGListRest).

cfg_graph_list(_, [], _, _, []).
*/
