/* LiceUnionBSD Style License):
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
package idb.algebra.ir

import idb.algebra.base.{RelationalAlgebraAggregationOperators, RelationalAlgebraBasicOperators}
import idb.query.QueryEnvironment
import idb.query.colors.{ClassColor, Color}

import scala.virtualization.lms.common.LiftAll
import idb.lms.extensions.{FunctionUtils, RemoteUtils, ScalaOpsExpOptExtensions}


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRAggregationOperators
    extends RelationalAlgebraIRBase with RelationalAlgebraAggregationOperators with RemoteUtils
{

    case class AggregationSelfMaintained[Domain: Manifest, Key: Manifest, RangeA, RangeB, Range : Manifest] (
		var relation : Rep[Query[Domain]],
		grouping : Rep[Domain => Key],
		start : RangeB,
		added : Rep[( (Domain, RangeB) ) => RangeB],
		removed : Rep[( (Domain, RangeB) ) => RangeB],
		updated: Rep[( (Domain, Domain, RangeB) ) => RangeB],
		convertKey : Rep[Key => RangeA],
		convert : Rep[((RangeA, RangeB)) => Range]
    ) extends Def[Query[Range]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = relation.isIncrementLocal

		def color = {
			val cGroup = colorsOfTFields(grouping, relation.color)
			val cConvertKey = colorsOfTFields(convertKey, Color.fromIdsInColors(cGroup))
			val rangeACol = Color.fromIdsInColors(cConvertKey)

			val cAdd = colorsOfTFields(added, Color.tupled(relation.color, Color.NO_COLOR))
			val cRemove = colorsOfTFields(removed, Color.tupled(relation.color, Color.NO_COLOR))
			val cUpdate = colorsOfTFields(updated, Color.tupled(relation.color, relation.color, Color.NO_COLOR))
			val rangeBCol = Color.fromIdsInColors(cAdd ++ cRemove ++ cUpdate)

			val cConvert = colorsOfTFields(convert, Color.tupled(rangeACol, rangeBCol))

			Color.fromIdsInColors(cConvert)
		}

		def host = relation.host
	}

	case class AggregationSelfMaintainedWithoutGrouping[Domain : Manifest, Result : Manifest](
		var relation : Rep[Query[Domain]],
		start : Result,
		added : Rep[((Domain, Result)) => Result],
		removed : Rep[((Domain, Result)) => Result],
		updated: Rep[( (Domain, Domain, Result) ) => Result]
	) extends Def[Query[Result]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = relation.isIncrementLocal

		def color = {
			val cAdd = colorsOfTFields(added, Color.tupled(relation.color, Color.NO_COLOR))
			val cRemove = colorsOfTFields(removed, Color.tupled(relation.color, Color.NO_COLOR))
			val cUpdate = colorsOfTFields(updated, Color.tupled(relation.color, relation.color, Color.NO_COLOR))
			Color.fromIdsInColors(cAdd ++ cRemove ++ cUpdate)
		}

		def host = relation.host
	}

	case class AggregationSelfMaintainedWithoutConvert[Domain : Manifest, Key : Manifest, Range : Manifest] (
		var relation: Rep[Query[Domain]],
		grouping: Rep[Domain => Key],
		start : Range,
		added : Rep[((Domain, Range)) => Range],
		removed : Rep[((Domain, Range)) => Range],
		updated: Rep[((Domain, Domain, Range)) => Range]
	) extends  Def[Query[Range]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = relation.isIncrementLocal

		def color = {
			val cGroup = colorsOfTFields(grouping, relation.color)

			val cAdd = colorsOfTFields(added, Color.tupled(relation.color, Color.NO_COLOR))
			val cRemove = colorsOfTFields(removed, Color.tupled(relation.color, Color.NO_COLOR))
			val cUpdate = colorsOfTFields(updated, Color.tupled(relation.color, relation.color, Color.NO_COLOR))

			Color.fromIdsInColors(cGroup ++ cAdd ++ cRemove ++ cUpdate)
		}

		def host = relation.host
	}

	case class AggregationNotSelfMaintained[Domain : Manifest, Key : Manifest, RangeA, RangeB, Range : Manifest](
		var relation : Rep[Query[Domain]],
		grouping : Rep[Domain => Key],
		start : RangeB,
		added : Rep[( (Domain, RangeB, Seq[Domain]) ) => RangeB],
		removed : Rep[( (Domain, RangeB, Seq[Domain]) ) => RangeB],
		updated: Rep[( (Domain, Domain, RangeB, Seq[Domain]) ) => RangeB],
		convertKey : Rep[Key => RangeA],
		convert : Rep[((RangeA, RangeB)) => Range]
	) extends Def[Query[Range]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = relation.isIncrementLocal

		def color = {
			val cGroup = colorsOfTFields(grouping, relation.color)
			val cConvertKey = colorsOfTFields(convertKey, Color.fromIdsInColors(cGroup))
			val rangeACol = Color.fromIdsInColors(cConvertKey)

			val cAdd = colorsOfTFields(added, Color.tupled(relation.color, Color.NO_COLOR, ClassColor(relation.color.ids)))
			val cRemove = colorsOfTFields(removed, Color.tupled(relation.color, Color.NO_COLOR, ClassColor(relation.color.ids)))
			val cUpdate = colorsOfTFields(updated, Color.tupled(relation.color, relation.color, Color.NO_COLOR, ClassColor(relation.color.ids)))
			val rangeBCol = Color.fromIdsInColors(cAdd ++ cRemove ++ cUpdate)

			val cConvert = colorsOfTFields(convert, Color.tupled(rangeACol, rangeBCol))

			Color.fromIdsInColors(cConvert)
		}

		def host = relation.host
	}

	case class AggregationNotSelfMaintainedWithoutGrouping[Domain : Manifest, Range : Manifest](
		var relation : Rep[Query[Domain]],
		start : Range,
		added : Rep[( (Domain, Range, Seq[Domain]) ) => Range],
		removed : Rep[( (Domain, Range, Seq[Domain]) ) => Range],
		updated: Rep[( (Domain, Domain, Range, Seq[Domain]) ) => Range]
	) extends Def[Query[Range]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = relation.isIncrementLocal

		def color = {
			val cAdd = colorsOfTFields(added, Color.tupled(relation.color, Color.NO_COLOR, ClassColor(relation.color.ids)))
			val cRemove = colorsOfTFields(removed, Color.tupled(relation.color, Color.NO_COLOR, ClassColor(relation.color.ids)))
			val cUpdate = colorsOfTFields(updated, Color.tupled(relation.color, relation.color, Color.NO_COLOR, ClassColor(relation.color.ids)))
			Color.fromIdsInColors(cAdd ++ cRemove ++ cUpdate)
		}

		def host = relation.host
	}

	case class AggregationNotSelfMaintainedWithoutConvert[Domain : Manifest, Key : Manifest, Range : Manifest] (
		var relation: Rep[Query[Domain]],
		grouping: Rep[Domain => Key],
		start : Range,
		added : Rep[( (Domain, Range, Seq[Domain]) ) => Range],
		removed : Rep[( (Domain, Range, Seq[Domain]) ) => Range],
		updated: Rep[( (Domain, Domain, Range, Seq[Domain]) ) => Range]
	) extends Def[Query[Range]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = relation.isIncrementLocal

		def color = {
			val cGroup = colorsOfTFields(grouping, relation.color)


			val cAdd = colorsOfTFields(added, Color.tupled(relation.color, Color.NO_COLOR, ClassColor(relation.color.ids)))
			val cRemove = colorsOfTFields(removed, Color.tupled(relation.color, Color.NO_COLOR, ClassColor(relation.color.ids)))
			val cUpdate = colorsOfTFields(updated, Color.tupled(relation.color, relation.color, Color.NO_COLOR, ClassColor(relation.color.ids)))
			Color.fromIdsInColors(cAdd ++ cRemove ++ cUpdate ++ cGroup)
		}

		def host = relation.host
	}

	case class Grouping[Domain : Manifest, Result : Manifest] (
		var relation : Rep[Query[Domain]],
		grouping : Rep[Domain => Result]
	) extends Def[Query[Result]] with QueryBaseOps {
		def isMaterialized: Boolean = !isIncrementLocal //Aggregation is materialized
		def isSet = false
		def isIncrementLocal = relation.isIncrementLocal

		def color = Color.fromIdsInColors(colorsOfTFields(grouping, relation.color))
		def host = relation.host
	}

	def aggregationSelfMaintained[Domain : Manifest, Key : Manifest, RangeA, RangeB, Range : Manifest](
		relation : Rep[Query[Domain]],
		grouping : Rep[Domain => Key],
		start : RangeB,
		added : Rep[( (Domain, RangeB) ) => RangeB],
		removed : Rep[( (Domain, RangeB) ) => RangeB],
		updated: Rep[( (Domain, Domain, RangeB) ) => RangeB],
		convertKey : Rep[Key => RangeA],
		convert : Rep[((RangeA, RangeB)) => Range]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] = {
		val r = AggregationSelfMaintained[Domain, Key, RangeA, RangeB, Range] (
			relation,
			grouping,
			start,
			added,
			removed,
			updated,
			convertKey,
			convert
		)

		r
	}


	def aggregationSelfMaintainedWithoutConvert[Domain : Manifest, Key : Manifest, Range : Manifest] (
		 relation: Rep[Query[Domain]],
		 grouping: Rep[Domain => Key],
		 start : Range,
		 added : Rep[((Domain, Range)) => Range],
		 removed : Rep[((Domain, Range)) => Range],
		 updated: Rep[((Domain, Domain, Range)) => Range]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] = {
		val r = AggregationSelfMaintainedWithoutConvert (
			relation,
			grouping,
			start,
			added,
			removed,
			updated
		)

		r
	}


	def aggregationSelfMaintainedWithoutGrouping[Domain : Manifest, Range : Manifest](
		relation : Rep[Query[Domain]],
		start : Range,
		added : Rep[((Domain, Range)) => Range],
		removed : Rep[((Domain, Range)) => Range],
		updated: Rep[( (Domain, Domain, Range) ) => Range]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] = {
		val r = AggregationSelfMaintainedWithoutGrouping (
			relation,
			start,
			added,
			removed,
			updated
		)

		r
	}


	def aggregationNotSelfMaintained[Domain : Manifest, Key : Manifest, RangeA, RangeB, Range : Manifest](
		relation : Rep[Query[Domain]],
		grouping : Rep[Domain => Key],
		start : RangeB,
		added : Rep[( (Domain, RangeB, Seq[Domain]) ) => RangeB],
		removed : Rep[( (Domain, RangeB, Seq[Domain]) ) => RangeB],
		updated: Rep[( (Domain, Domain, RangeB, Seq[Domain]) ) => RangeB],
		convertKey : Rep[Key => RangeA],
		convert : Rep[((RangeA, RangeB)) => Range]
  	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] =   {
		val r = AggregationNotSelfMaintained[Domain, Key, RangeA, RangeB, Range] (
			relation,
			grouping,
			start,
			added,
			removed,
			updated,
			convertKey,
			convert
		)

		r
	}


	def aggregationNotSelfMaintainedWithoutGrouping[Domain : Manifest, Range : Manifest](
		relation : Rep[Query[Domain]],
		start : Range,
		added : Rep[( (Domain, Range, Seq[Domain]) ) => Range],
		removed : Rep[( (Domain, Range, Seq[Domain]) ) => Range],
		updated: Rep[( (Domain, Domain, Range, Seq[Domain]) ) => Range]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] = {
		val r = AggregationNotSelfMaintainedWithoutGrouping (
			relation,
			start,
			added,
			removed,
			updated
		)

		r
	}


	def aggregationNotSelfMaintainedWithoutConvert[Domain : Manifest, Key : Manifest, Range : Manifest] (
		relation: Rep[Query[Domain]],
		grouping: Rep[Domain => Key],
		start : Range,
		added : Rep[( (Domain, Range, Seq[Domain]) ) => Range],
		removed : Rep[( (Domain, Range, Seq[Domain]) ) => Range],
		updated: Rep[( (Domain, Domain, Range, Seq[Domain]) ) => Range]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] = {
		val r = AggregationNotSelfMaintainedWithoutConvert (
			relation,
			grouping,
			start,
			added,
			removed,
			updated
		)

		r
	}


	def grouping[Domain : Manifest, Range : Manifest] (
		relation : Rep[Query[Domain]],
		grouping : Rep[Domain => Range]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] = {
		val r = Grouping (relation,grouping)

		r
	}

}


