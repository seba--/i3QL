package sae.example.hospital

import idb.SetTable
import idb.operators.{Aggregation, AggregateFunction, SelfMaintainableAggregateFunction, SelfMaintainableAggregateFunctionFactory}
import idb.operators.impl.{SelectionView, EquiJoinView}
import sae.example.hospital.data._


/**
 * @author Mirko Köhler
 */
object Queries2 extends HospitalTestData {

	def main(args : Array[String]): Unit = {
		import sae.example.hospital.data.Hospital.LocalSetup._

		//Setup query
		val join1 = EquiJoinView[FinancialData, Person](
			finance,
			person,
			Seq(f => f.personId),
			Seq(p => p.personId),
			isSet = true
		)

		val join2 = EquiJoinView[(FinancialData, Person), Patient](
			join1,
			patient,
			Seq(fp => fp._2.personId),
			Seq(p => p.personId),
			isSet = true
		)

		val selection1 = SelectionView[TreatmentData](
			treatment,
		    t => t.diagnosis == Diagnosis.allergy,
			isSet = true
		)

		val join3 = EquiJoinView[((FinancialData, Person), Patient), TreatmentData](
			join2,
			treatment,
			Seq(fpp => fpp._2.treatmentId),
			Seq(t => t.treatmentId),
			isSet = true
		)

		val aggregate1 = Aggregation(join3, Sum, isSet = true)

		val query = aggregate1.asMaterialized

		//Add to tables
		println("Start adding items...")
		//No allergy patients
		finance += financeJohnDoe
		person += johnDoe
		patient += patientJohnDoe1
		treatment += treatmentLungCancerChemo

		//Allergy patient
		treatment += treatmentAllergyMedicine
		treatment += treatmentAllergyInhaler

		finance += financeJaneDoe
		person += janeDoe
		patient += patientJaneDoe2

		finance += financeBobRoss
		person += bobRoss
		patient += patientBobRoss1

		//print result
		println("Results:")
		query.foreach(println)


	}

}

object Sum extends SelfMaintainableAggregateFunctionFactory[(((FinancialData, Person), Patient), TreatmentData), Double] {

	class SumFunction extends SelfMaintainableAggregateFunction[(((FinancialData, Person), Patient), TreatmentData), Double] {
		var sum : Double = 0

		override def add(newD: (((FinancialData, Person), Patient), TreatmentData)): Double = {
			val v = newD._1._1._1.payment
			sum = sum + v
			sum
		}


		override def update(oldD: (((FinancialData, Person), Patient), TreatmentData), newD: (((FinancialData, Person), Patient), TreatmentData)): Double = {
			val v1 = newD._1._1._1.payment
			val v2 = oldD._1._1._1.payment
			sum = sum - v2 + v1
			sum
		}

		override def remove(newD: (((FinancialData, Person), Patient), TreatmentData)): Double = {
			val v = newD._1._1._1.payment
			sum = sum - v
			sum
		}

		override def get: Double = sum
	}

	override def apply(): SelfMaintainableAggregateFunction[(((FinancialData, Person), Patient), TreatmentData), Double] = new SumFunction
}





