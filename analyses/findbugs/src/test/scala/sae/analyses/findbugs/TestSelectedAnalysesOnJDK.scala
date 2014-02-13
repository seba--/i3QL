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
package sae.analyses.findbugs

import org.junit.{Ignore, Test}
import sae.analyses.findbugs.selected._


/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 *
 */

class TestSelectedAnalysesOnJDK extends AbstractTestAnalysesOnJDK
{

    @Test
    def test_CI_CONFUSED_INHERITANCE () {
		executeAnalysis(CI_CONFUSED_INHERITANCE, expectedMatches = 123)
    }

  // TODO Findbugs finds less entries
    @Test
    def test_CN_IDIOM () {
		executeAnalysis(CN_IDIOM, expectedMatches = 25)
    }


    @Test
    def test_CN_IDIOM_NO_SUPER_CALL () {
		executeAnalysis(CN_IDIOM_NO_SUPER_CALL, expectedMatches = 136)

    }

  // TODO Findbugs finds more entries
    @Test
    def test_CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE () {
	  	executeAnalysis(CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE, expectedMatches = 34)
        // Findbugs says 38
    }


    @Test
    def test_CO_ABSTRACT_SELF () {
		executeAnalysis(CO_ABSTRACT_SELF, expectedMatches = 16)
    }


    @Test
    def test_CO_SELF_NO_OBJECT () {
		executeAnalysis(CO_SELF_NO_OBJECT, expectedMatches = 55)
    }


    @Test
    def test_DM_GC () {
		executeAnalysis(DM_GC, expectedMatches = 3)
    }


    @Test
    def test_DM_RUN_FINALIZERS_ON_EXIT () {
		executeAnalysis(DM_RUN_FINALIZERS_ON_EXIT, expectedMatches = 1)
    }


    @Test
    def test_EQ_ABSTRACT_SELF () {
		executeAnalysis(EQ_ABSTRACT_SELF, expectedMatches = 4)
    }


    @Test
    def test_FI_PUBLIC_SHOULD_BE_PROTECTED () {
		executeAnalysis(FI_PUBLIC_SHOULD_BE_PROTECTED, expectedMatches = 20)
    }


    @Test
    def test_IMSE_DONT_CATCH_IMSE () {
		executeAnalysis(IMSE_DONT_CATCH_IMSE, expectedMatches = 0)
    }


    // TODO Option.get is called on None, due to too optimistic pushing of operators
   // @Ignore
    @Test
    def test_SE_NO_SUITABLE_CONSTRUCTOR () {
		executeAnalysis(SE_NO_SUITABLE_CONSTRUCTOR, expectedMatches = 46)
    }


    // TODO Findbugs finds less entries
    @Test
    def test_SS_SHOULD_BE_STATIC () {
		executeAnalysis(SS_SHOULD_BE_STATIC, expectedMatches = 102)

      // Findbugs says 92, but it is not clear why the last 10 entries are filtered
        // the respective entries are:
        /*
        FieldDeclaration(ClassDeclaration(51,32,Lcom/sun/imageio/plugins/jpeg/JFIFMarkerSegment;,
        Some(Lcom/sun/imageio/plugins/jpeg/MarkerSegment;),WrappedArray()),18,MAX_THUMB_HEIGHT,I,Some(255),
        Some(Ljava/lang/Integer;))
        FieldDeclaration(ClassDeclaration(51,32,Lcom/sun/imageio/plugins/jpeg/JFIFMarkerSegment;,
        Some(Lcom/sun/imageio/plugins/jpeg/MarkerSegment;),WrappedArray()),18,MAX_THUMB_WIDTH,I,Some(255),
        Some(Ljava/lang/Integer;))
        FieldDeclaration(ClassDeclaration(51,32,Lcom/sun/imageio/plugins/jpeg/JFIFMarkerSegment;,
        Some(Lcom/sun/imageio/plugins/jpeg/MarkerSegment;),WrappedArray()),18,debug,Z,Some(0),Some(Ljava/lang/Integer;))
        FieldDeclaration(ClassDeclaration(51,33,
        Lcom/sun/org/apache/xml/internal/dtm/ref/dom2dtm/DOM2DTMdefaultNamespaceDeclarationNode;,Some(Ljava/lang/Object;),
        WrappedArray(Lorg/w3c/dom/Attr;, Lorg/w3c/dom/TypeInfo;)),16,NOT_SUPPORTED_ERR,Ljava/lang/String;,
        Some(Unsupported operation on pseudonode),Some(Ljava/lang/String;))
        FieldDeclaration(ClassDeclaration(51,33,Lcom/sun/org/apache/xml/internal/dtm/ref/sax2dtm/SAX2DTM2$PrecedingIterator;,
        Some(Lcom/sun/org/apache/xml/internal/dtm/ref/DTMDefaultBaseIterators$InternalAxisIteratorBase;),WrappedArray()),18,
        _maxAncestors,I,Some(8),Some(Ljava/lang/Integer;))
        FieldDeclaration(ClassDeclaration(51,48,Lcom/sun/xml/internal/ws/message/source/SourceUtils;,
        Some(Ljava/lang/Object;),WrappedArray()),18,domSource,I,Some(1),Some(Ljava/lang/Integer;))
        FieldDeclaration(ClassDeclaration(51,48,Lcom/sun/xml/internal/ws/message/source/SourceUtils;,
        Some(Ljava/lang/Object;),WrappedArray()),18,saxSource,I,Some(4),Some(Ljava/lang/Integer;))
        FieldDeclaration(ClassDeclaration(51,48,Lcom/sun/xml/internal/ws/message/source/SourceUtils;,
        Some(Ljava/lang/Object;),WrappedArray()),18,streamSource,I,Some(2),Some(Ljava/lang/Integer;))
        FieldDeclaration(ClassDeclaration(51,48,Lsun/nio/ch/WindowsSelectorImpl;,Some(Lsun/nio/ch/SelectorImpl;),
        WrappedArray()),18,INIT_CAP,I,Some(8),Some(Ljava/lang/Integer;))
        FieldDeclaration(ClassDeclaration(51,32,Lsun/tracing/dtrace/DTraceProvider;,Some(Lsun/tracing/ProviderSkeleton;),
        WrappedArray()),18,proxyClassNamePrefix,Ljava/lang/String;,Some($DTraceTracingProxy),Some(Ljava/lang/String;))
        */
    }

  // TODO Findbugs finds less entries
    @Test
    def test_UUF_UNUSED_FIELD () {
	  	executeAnalysis(UUF_UNUSED_FIELD, expectedMatches = 117)
        // Findbugs says 53
    }
}
