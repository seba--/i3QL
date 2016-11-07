package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

/**
  * Created by mirko on 07.11.16.
  */
case class Supplier(id : Int, name : String, city : String)
	extends Nameable with Benchmarkable

trait SupplierSchema
	extends NameableSchema with BenchmarkableSchema