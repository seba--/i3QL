package sae.test.helpFunctions
import sae.operators._
import sae.functions._


private class PseudoVarianzIntern[Domain <: AnyRef](val f : Domain => Double) extends AggregationFunktion[Domain, (Double,Double)] {
     val avg : AggregationFunktion[Domain, Double] = AVG(f).apply()
     val count = Count[Domain]().apply()
     var pi = 0.0
     var erwartunswet = 0.0
     var varianz = 0.0
     var value : Option[Domain] = None
        def add(d : Domain, data : Iterable[Domain]) = {
         	erwartunswet = avg.add(d, data)
         	var tmp : Double = count.add(d,data)
         	pi = 1.asInstanceOf[Double] / tmp
         	varianz = 0.0
         	data.foreach( x => {
         	  varianz += ( (f(x) - erwartunswet)*(f(x) - erwartunswet)*pi)
         	})
         	(erwartunswet,varianz)
        }
        def remove(d : Domain, data : Iterable[Domain]) = {
        	erwartunswet = avg.remove(d, data)
         	var tmp : Double = count.remove(d,data)
         	pi = 1.asInstanceOf[Double] / tmp
         	varianz = 0.0
         	data.foreach( x => {
         	  varianz += ( (f(x) - erwartunswet)*(f(x) - erwartunswet)*pi)
         	})
         	(erwartunswet,varianz)
        }

        def update(oldV : Domain, newV : Domain, data : Iterable[Domain]) = {
        	erwartunswet = avg.update(oldV,newV, data)
         	var tmp : Double = count.update(oldV,newV,data)
         	pi = 1.asInstanceOf[Double] / tmp
         	varianz = 0.0
         	data.foreach( x => {
         	  varianz += ( (f(x) - erwartunswet)*(f(x) - erwartunswet)*pi)
         	})
         	(erwartunswet,varianz)
        }
}

object PseudoVarianz {
    def apply[Domain <: AnyRef](f : (Domain => Double )) = {
        new AggregationFunktionFactory[Domain,(Double,Double)]{
           def apply() : AggregationFunktion[Domain,  (Double,Double)] = {
               new PseudoVarianzIntern[Domain](f)
           }
        }
    }
}