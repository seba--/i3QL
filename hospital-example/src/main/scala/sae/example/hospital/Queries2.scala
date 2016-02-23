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
		import sae.example.hospital.data.HospitalDatabase._

		//Setup query
		val join1 = EquiJoinView[FinancialData, Person](
			financeDatabase,
			personDatabase,
			Seq(f => f.personId),
			Seq(p => p.personId),
			isSet = true
		)

		val join2 = EquiJoinView[(FinancialData, Person), Patient](
			join1,
			patientDatabase,
			Seq(fp => fp._2.personId),
			Seq(p => p.personId),
			isSet = true
		)

		val selection1 = SelectionView[TreatmentData](
			treatmentDatabase,
		    t => t.diagnosis == Diagnosis.allergy,
			isSet = true
		)

		val join3 = EquiJoinView[((FinancialData, Person), Patient), TreatmentData](
			join2,
			treatmentDatabase,
			Seq(fpp => fpp._2.treatmentId),
			Seq(t => t.treatmentId),
			isSet = true
		)

		val aggregate1 = Aggregation(join3, Sum, isSet = true)

		val query = aggregate1.asMaterialized

		//Add to tables
		println("Start adding items...")
		//No allergy patients
		financeDatabase += financeJohnDoe
		personDatabase += johnDoe
		patientDatabase += patientJohnDoe1
		treatmentDatabase += treatmentLungCancerChemo

		//Allergy patient
		treatmentDatabase += treatmentAllergyMedicine
		treatmentDatabase += treatmentAllergyInhaler

		financeDatabase += financeJaneDoe
		personDatabase += janeDoe
		patientDatabase += patientJaneDoe2

		financeDatabase += financeBobRoss
		personDatabase += bobRoss
		patientDatabase += patientBobRoss1

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





