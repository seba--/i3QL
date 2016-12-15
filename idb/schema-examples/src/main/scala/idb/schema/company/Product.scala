package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

case class Product(id : Int, name : String)
	extends Nameable with Benchmarkable

trait ProductSchema
	extends NameableSchema with BenchmarkableSchema
