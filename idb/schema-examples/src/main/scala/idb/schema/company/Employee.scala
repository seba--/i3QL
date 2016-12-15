package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

case class Employee(id : Int, name : String)
	extends Nameable with Benchmarkable

trait EmployeeSchema
	extends NameableSchema with BenchmarkableSchema
