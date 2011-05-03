package sae.test

import sae.core._
import sae.core.impl._
import sae.core.RelationalAlgebraSyntax._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

case class Item
(
	val Name : String,
	val Type : String
)

case class Supplier
(
	val CompanyName : String,
	val Department : String,
	val ItemName : String,
	val Volume : Int
)


/**
 * Test the basic functions of the framework with an example database of companies and items supplied
 */

@RunWith(classOf[JUnitRunner]) 
class ItemSupplyCompanyFunSuite
	extends FunSuite  
{
	// item(choclate, food).
	// item(apples, food).
 	// item(tires, car).
 	// item(tires, electronics).


	val choclate = Item("choclate", "food")
 	val apples = Item("apples", "food")
 	val tires = Item("tires", "car")
 	val cpu = Item("tires", "electronics")

	val nestle_choclate = Supplier("nestle", "dep. for sweets", "choclate", 3500)
	val nestle_apples = Supplier("nestle", "dep. for fruit", "apples", 10000) 

	val ferrari_tires = Supplier("ferrari", "racing cars", "tires", 30)
	val ibm_cpu = Supplier("ibm", "intelligent solutions", "cpu", 1100)

	val amd_cpu = Supplier("amd", "fast computation", "cpu", 1000) 

	val tx_cpu = Supplier("texas_instruments", "micro electronics", "cpu", 300)
	
	val agrotrade_apples = Supplier("agrotrade", "fruits", "apples", 30000)
	
	// supplier(CompanyName,Department,ItemName,Volume)
	def suppliers : MaterializedRelation[Supplier] = 
	{
		val suppliers = new MultisetRelation[Supplier]{ def materialize() : Unit = { /* nothing to do, the set itself is the data */ } } 
		suppliers += nestle_choclate
		suppliers += nestle_apples
		suppliers += agrotrade_apples
		suppliers += ferrari_tires
		suppliers += ibm_cpu
		suppliers += amd_cpu
		suppliers += tx_cpu
	}

	// item(CourseId, Name)
	def items : MaterializedRelation[Item] = 
	{
		val items = new MultisetRelation[Item]{ def materialize() : Unit = { /* nothing to do, the set itself is the data */ } }  
		items += choclate
		items += apples
		items += tires
		items += cpu 
	}

	test("aggregate group") {
		
	}
}