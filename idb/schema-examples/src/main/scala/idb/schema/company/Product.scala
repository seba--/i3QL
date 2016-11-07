package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

/**
  * Created by mirko on 07.11.16.
  */
case class Product(id : Int, name : String)
	extends Nameable with Benchmarkable

trait ProductSchema
	extends NameableSchema with BenchmarkableSchema
