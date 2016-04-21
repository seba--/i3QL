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
package sae.bytecode.constants

/**
 *
 * @author Ralf Mitschke
 */
object OpCodes
{
    val NOP: Short = 0
    val ACONST_NULL: Short = 1
    val ICONST_M1: Short = 2
    val ICONST_0: Short = 3
    val ICONST_1: Short = 4
    val ICONST_2: Short = 5
    val ICONST_3: Short = 6
    val ICONST_4: Short = 7
    val ICONST_5: Short = 8
    val LCONST_0: Short = 9
    val LCONST_1: Short = 10
    val FCONST_0: Short = 11
    val FCONST_1: Short = 12
    val FCONST_2: Short = 13
    val DCONST_0: Short = 14
    val DCONST_1: Short = 15
    val BIPUSH: Short = 16
    val SIPUSH: Short = 17
    val LDC: Short = 18
    val LDC_W: Short = 19
    val LDC2_W: Short = 20
    val ILOAD: Short = 21
    val LLOAD: Short = 22
    val FLOAD: Short = 23
    val DLOAD: Short = 24
    val ALOAD: Short = 25
    val ILOAD_0: Short = 26
    val ILOAD_1: Short = 27
    val ILOAD_2: Short = 28
    val ILOAD_3: Short = 29
    val LLOAD_0: Short = 30
    val LLOAD_1: Short = 31
    val LLOAD_2: Short = 32
    val LLOAD_3: Short = 33
    val FLOAD_0: Short = 34
    val FLOAD_1: Short = 35
    val FLOAD_2: Short = 36
    val FLOAD_3: Short = 37
    val DLOAD_0: Short = 38
    val DLOAD_1: Short = 39
    val DLOAD_2: Short = 40
    val DLOAD_3: Short = 41
    val ALOAD_0: Short = 42
    val ALOAD_1: Short = 43
    val ALOAD_2: Short = 44
    val ALOAD_3: Short = 45
    val IALOAD: Short = 46
    val LALOAD: Short = 47
    val FALOAD: Short = 48
    val DALOAD: Short = 49
    val AALOAD: Short = 50
    val BALOAD: Short = 51
    val CALOAD: Short = 52
    val SALOAD: Short = 53
    val ISTORE: Short = 54
    val LSTORE: Short = 55
    val FSTORE: Short = 56
    val DSTORE: Short = 57
    val ASTORE: Short = 58
    val ISTORE_0: Short = 59
    val ISTORE_1: Short = 60
    val ISTORE_2: Short = 61
    val ISTORE_3: Short = 62
    val LSTORE_0: Short = 63
    val LSTORE_1: Short = 64
    val LSTORE_2: Short = 65
    val LSTORE_3: Short = 66
    val FSTORE_0: Short = 67
    val FSTORE_1: Short = 68
    val FSTORE_2: Short = 69
    val FSTORE_3: Short = 70
    val DSTORE_0: Short = 71
    val DSTORE_1: Short = 72
    val DSTORE_2: Short = 73
    val DSTORE_3: Short = 74
    val ASTORE_0: Short = 75
    val ASTORE_1: Short = 76
    val ASTORE_2: Short = 77
    val ASTORE_3: Short = 78
    val IASTORE: Short = 79
    val LASTORE: Short = 80
    val FASTORE: Short = 81
    val DASTORE: Short = 82
    val AASTORE: Short = 83
    val BASTORE: Short = 84
    val CASTORE: Short = 85
    val SASTORE: Short = 86
    val POP: Short = 87
    val POP2: Short = 88
    val DUP: Short = 89
    val DUP_X1: Short = 90
    val DUP_X2: Short = 91
    val DUP2: Short = 92
    val DUP2_X1: Short = 93
    val DUP2_X2: Short = 94
    val SWAP: Short = 95
    val IADD: Short = 96
    val LADD: Short = 97
    val FADD: Short = 98
    val DADD: Short = 99
    val ISUB: Short = 100
    val LSUB: Short = 101
    val FSUB: Short = 102
    val DSUB: Short = 103
    val IMUL: Short = 104
    val LMUL: Short = 105
    val FMUL: Short = 106
    val DMUL: Short = 107
    val IDIV: Short = 108
    val LDIV: Short = 109
    val FDIV: Short = 110
    val DDIV: Short = 111
    val IREM: Short = 112
    val LREM: Short = 113
    val FREM: Short = 114
    val DREM: Short = 115
    val INEG: Short = 116
    val LNEG: Short = 117
    val FNEG: Short = 118
    val DNEG: Short = 119
    val ISHL: Short = 120
    val LSHL: Short = 121
    val ISHR: Short = 122
    val LSHR: Short = 123
    val IUSHR: Short = 124
    val LUSHR: Short = 125
    val IAND: Short = 126
    val LAND: Short = 127
    val IOR: Short = 128
    val LOR: Short = 129
    val IXOR: Short = 130
    val LXOR: Short = 131
    val IINC: Short = 132
    val I2L: Short = 133
    val I2F: Short = 134
    val I2D: Short = 135
    val L2I: Short = 136
    val L2F: Short = 137
    val L2D: Short = 138
    val F2I: Short = 139
    val F2L: Short = 140
    val F2D: Short = 141
    val D2I: Short = 142
    val D2L: Short = 143
    val D2F: Short = 144
    val I2B: Short = 145
    val INT2BYTE: Short = 145
    val I2C: Short = 146
    val INT2CHAR: Short = 146
    val I2S: Short = 147
    val INT2SHORT: Short = 147
    val LCMP: Short = 148
    val FCMPL: Short = 149
    val FCMPG: Short = 150
    val DCMPL: Short = 151
    val DCMPG: Short = 152
    val IFEQ: Short = 153
    val IFNE: Short = 154
    val IFLT: Short = 155
    val IFGE: Short = 156
    val IFGT: Short = 157
    val IFLE: Short = 158
    val IF_ICMPEQ: Short = 159
    val IF_ICMPNE: Short = 160
    val IF_ICMPLT: Short = 161
    val IF_ICMPGE: Short = 162
    val IF_ICMPGT: Short = 163
    val IF_ICMPLE: Short = 164
    val IF_ACMPEQ: Short = 165
    val IF_ACMPNE: Short = 166
    val GOTO: Short = 167
    val JSR: Short = 168
    val RET: Short = 169
    val TABLESWITCH: Short = 170
    val LOOKUPSWITCH: Short = 171
    val IRETURN: Short = 172
    val LRETURN: Short = 173
    val FRETURN: Short = 174
    val DRETURN: Short = 175
    val ARETURN: Short = 176
    val RETURN: Short = 177
    val GETSTATIC: Short = 178
    val PUTSTATIC: Short = 179
    val GETFIELD: Short = 180
    val PUTFIELD: Short = 181
    val INVOKEVIRTUAL: Short = 182
    val INVOKESPECIAL: Short = 183
    val INVOKENONVIRTUAL: Short = 183
    val INVOKESTATIC: Short = 184
    val INVOKEINTERFACE: Short = 185
    val INVOKEDYNAMIC: Short = 186
    val NEW: Short = 187
    val NEWARRAY: Short = 188
    val ANEWARRAY: Short = 189
    val ARRAYLENGTH: Short = 190
    val ATHROW: Short = 191
    val CHECKCAST: Short = 192
    val INSTANCEOF: Short = 193
    val MONITORENTER: Short = 194
    val MONITOREXIT: Short = 195
    val WIDE: Short = 196
    val MULTIANEWARRAY: Short = 197
    val IFNULL: Short = 198
    val IFNONNULL: Short = 199
    val GOTO_W: Short = 200
    val JSR_W: Short = 201
}
