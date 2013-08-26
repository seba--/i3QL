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


/**
 *
 * All Java Virtual Machine opcodes.
 *
 * @author Ralf Mitschke
 */
object BytecodeOpCodes
{
    final val NOP: Short = 0
    final val ACONST_NULL: Short = 1
    final val ICONST_M1: Short = 2
    final val ICONST_0: Short = 3
    final val ICONST_1: Short = 4
    final val ICONST_2: Short = 5
    final val ICONST_3: Short = 6
    final val ICONST_4: Short = 7
    final val ICONST_5: Short = 8
    final val LCONST_0: Short = 9
    final val LCONST_1: Short = 10
    final val FCONST_0: Short = 11
    final val FCONST_1: Short = 12
    final val FCONST_2: Short = 13
    final val DCONST_0: Short = 14
    final val DCONST_1: Short = 15
    final val BIPUSH: Short = 16
    final val SIPUSH: Short = 17
    final val LDC: Short = 18
    final val LDC_W: Short = 19
    final val LDC2_W: Short = 20
    final val ILOAD: Short = 21
    final val LLOAD: Short = 22
    final val FLOAD: Short = 23
    final val DLOAD: Short = 24
    final val ALOAD: Short = 25
    final val ILOAD_0: Short = 26
    final val ILOAD_1: Short = 27
    final val ILOAD_2: Short = 28
    final val ILOAD_3: Short = 29
    final val LLOAD_0: Short = 30
    final val LLOAD_1: Short = 31
    final val LLOAD_2: Short = 32
    final val LLOAD_3: Short = 33
    final val FLOAD_0: Short = 34
    final val FLOAD_1: Short = 35
    final val FLOAD_2: Short = 36
    final val FLOAD_3: Short = 37
    final val DLOAD_0: Short = 38
    final val DLOAD_1: Short = 39
    final val DLOAD_2: Short = 40
    final val DLOAD_3: Short = 41
    final val ALOAD_0: Short = 42
    final val ALOAD_1: Short = 43
    final val ALOAD_2: Short = 44
    final val ALOAD_3: Short = 45
    final val IALOAD: Short = 46
    final val LALOAD: Short = 47
    final val FALOAD: Short = 48
    final val DALOAD: Short = 49
    final val AALOAD: Short = 50
    final val BALOAD: Short = 51
    final val CALOAD: Short = 52
    final val SALOAD: Short = 53
    final val ISTORE: Short = 54
    final val LSTORE: Short = 55
    final val FSTORE: Short = 56
    final val DSTORE: Short = 57
    final val ASTORE: Short = 58
    final val ISTORE_0: Short = 59
    final val ISTORE_1: Short = 60
    final val ISTORE_2: Short = 61
    final val ISTORE_3: Short = 62
    final val LSTORE_0: Short = 63
    final val LSTORE_1: Short = 64
    final val LSTORE_2: Short = 65
    final val LSTORE_3: Short = 66
    final val FSTORE_0: Short = 67
    final val FSTORE_1: Short = 68
    final val FSTORE_2: Short = 69
    final val FSTORE_3: Short = 70
    final val DSTORE_0: Short = 71
    final val DSTORE_1: Short = 72
    final val DSTORE_2: Short = 73
    final val DSTORE_3: Short = 74
    final val ASTORE_0: Short = 75
    final val ASTORE_1: Short = 76
    final val ASTORE_2: Short = 77
    final val ASTORE_3: Short = 78
    final val IASTORE: Short = 79
    final val LASTORE: Short = 80
    final val FASTORE: Short = 81
    final val DASTORE: Short = 82
    final val AASTORE: Short = 83
    final val BASTORE: Short = 84
    final val CASTORE: Short = 85
    final val SASTORE: Short = 86
    final val POP: Short = 87
    final val POP2: Short = 88
    final val DUP: Short = 89
    final val DUP_X1: Short = 90
    final val DUP_X2: Short = 91
    final val DUP2: Short = 92
    final val DUP2_X1: Short = 93
    final val DUP2_X2: Short = 94
    final val SWAP: Short = 95
    final val IADD: Short = 96
    final val LADD: Short = 97
    final val FADD: Short = 98
    final val DADD: Short = 99
    final val ISUB: Short = 100
    final val LSUB: Short = 101
    final val FSUB: Short = 102
    final val DSUB: Short = 103
    final val IMUL: Short = 104
    final val LMUL: Short = 105
    final val FMUL: Short = 106
    final val DMUL: Short = 107
    final val IDIV: Short = 108
    final val LDIV: Short = 109
    final val FDIV: Short = 110
    final val DDIV: Short = 111
    final val IREM: Short = 112
    final val LREM: Short = 113
    final val FREM: Short = 114
    final val DREM: Short = 115
    final val INEG: Short = 116
    final val LNEG: Short = 117
    final val FNEG: Short = 118
    final val DNEG: Short = 119
    final val ISHL: Short = 120
    final val LSHL: Short = 121
    final val ISHR: Short = 122
    final val LSHR: Short = 123
    final val IUSHR: Short = 124
    final val LUSHR: Short = 125
    final val IAND: Short = 126
    final val LAND: Short = 127
    final val IOR: Short = 128
    final val LOR: Short = 129
    final val IXOR: Short = 130
    final val LXOR: Short = 131
    final val IINC: Short = 132
    final val I2L: Short = 133
    final val I2F: Short = 134
    final val I2D: Short = 135
    final val L2I: Short = 136
    final val L2F: Short = 137
    final val L2D: Short = 138
    final val F2I: Short = 139
    final val F2L: Short = 140
    final val F2D: Short = 141
    final val D2I: Short = 142
    final val D2L: Short = 143
    final val D2F: Short = 144
    final val I2B: Short = 145
    final val INT2BYTE: Short = 145
    final val I2C: Short = 146
    final val INT2CHAR: Short = 146
    final val I2S: Short = 147
    final val INT2SHORT: Short = 147
    final val LCMP: Short = 148
    final val FCMPL: Short = 149
    final val FCMPG: Short = 150
    final val DCMPL: Short = 151
    final val DCMPG: Short = 152
    final val IFEQ: Short = 153
    final val IFNE: Short = 154
    final val IFLT: Short = 155
    final val IFGE: Short = 156
    final val IFGT: Short = 157
    final val IFLE: Short = 158
    final val IF_ICMPEQ: Short = 159
    final val IF_ICMPNE: Short = 160
    final val IF_ICMPLT: Short = 161
    final val IF_ICMPGE: Short = 162
    final val IF_ICMPGT: Short = 163
    final val IF_ICMPLE: Short = 164
    final val IF_ACMPEQ: Short = 165
    final val IF_ACMPNE: Short = 166
    final val GOTO: Short = 167
    final val JSR: Short = 168
    final val RET: Short = 169
    final val TABLESWITCH: Short = 170
    final val LOOKUPSWITCH: Short = 171
    final val IRETURN: Short = 172
    final val LRETURN: Short = 173
    final val FRETURN: Short = 174
    final val DRETURN: Short = 175
    final val ARETURN: Short = 176
    final val RETURN: Short = 177
    final val GETSTATIC: Short = 178
    final val PUTSTATIC: Short = 179
    final val GETFIELD: Short = 180
    final val PUTFIELD: Short = 181
    final val INVOKEVIRTUAL: Short = 182
    final val INVOKESPECIAL: Short = 183
    final val INVOKENONVIRTUAL: Short = 183
    final val INVOKESTATIC: Short = 184
    final val INVOKEINTERFACE: Short = 185
    final val NEW: Short = 187
    final val NEWARRAY: Short = 188
    final val ANEWARRAY: Short = 189
    final val ARRAYLENGTH: Short = 190
    final val ATHROW: Short = 191
    final val CHECKCAST: Short = 192
    final val INSTANCEOF: Short = 193
    final val MONITORENTER: Short = 194
    final val MONITOREXIT: Short = 195
    final val WIDE: Short = 196
    final val MULTIANEWARRAY: Short = 197
    final val IFNULL: Short = 198
    final val IFNONNULL: Short = 199
    final val GOTO_W: Short = 200
    final val JSR_W: Short = 201

}
