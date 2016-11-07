package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

/**
  * Created by mirko on 07.11.16.
  */
case class Component(id : Int, name : String)
	extends Nameable with Benchmarkable

trait ComponentSchema
	extends NameableSchema with BenchmarkableSchema
