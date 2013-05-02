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
package idb.iql.compiler.lms

import scala.virtualization.lms.common.BaseExp
import scala.reflect.ManifestFactory

/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRBase
    extends RelationalAlgebraBase with BaseExp
{

    type Relation[+Domain] = AbstractRelation[Domain]

    // TODO seems not good to tie this to the IR, binding it here means we can construct an IR only for one type of ConcreteRelation, e.g., only for incremental relations or for lists.
    // However, the data structures inside the incremental relations and the lists are also markedly different. Without further analyses it seem unlikely that we have the same query for both.
    // TODO make Domain covariant
    type CompiledRelation[Domain]

    abstract class AbstractRelation[+Domain: Manifest]

    def relationManifest[Domain: Manifest]: Manifest[AbstractRelation[Domain]] =
        ManifestFactory.classType (classOf[AbstractRelation[Domain]], manifest[Domain])


    case class BaseRelation[Domain](relImpl: CompiledRelation[Domain])
                                   (implicit mDom: Manifest[Domain], mRel: Manifest[CompiledRelation[Domain]])
        extends Exp[Relation[Domain]]

    def baseRelation[Domain](relImpl: CompiledRelation[Domain])
                            (implicit mDom: Manifest[Domain], mRel: Manifest[CompiledRelation[Domain]]) =
        BaseRelation (relImpl)

}
