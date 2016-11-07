package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

/**
  * Created by mirko on 07.11.16.
  */
case class Employee(id : Int, name : String)
	extends Nameable with Benchmarkable

trait EmployeeSchema
	extends NameableSchema with BenchmarkableSchema
