package com.superpixel.jdot.example

import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import com.superpixel.jdot.pathing._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import scala.io.Source
import org.scalatest.FunSpec
import com.superpixel.jdot._

class AusGrandPrixExample extends FunSpec with Matchers {

  val ausF1: String = {
      val buffSource = Source.fromURL(getClass.getResource("/2016-aus-grandprix-result.json"))
      val jsonLines = buffSource.getLines
      compact(render(parse(jsonLines.mkString)))
  }
  
  val ausF1Simple: String = {
      val buffSource = Source.fromURL(getClass.getResource("/2016-aus-grandprix-result-simple.json"))
      val jsonLines = buffSource.getLines
      compact(render(parse(jsonLines.mkString)))
  }
  
  
  describe("Accessing json with simple dot and square bracket syntax") {
    
    //Set up accessor with our example json
    val accessor = JdotAccessor(ausF1Simple)
    
    it("can access immediate fields") {
      val racename = accessor.getString("raceName")
      racename.foreach { println(_) }
    }
    
    it("can access json objects") {
      val circuit = accessor.getJsonString("circuit")
      circuit.foreach { println(_) }
    }
  }
  
  
  
  
  describe("Simplified ausF1 json") {
    import scala.language.implicitConversions

    //     TO                  FROM
    val jcTrans = JdotTransformer(
      Set(
        ("season",             "season"),
        ("round",              "round"),
        ("raceName",           "raceName"),
        ("circuit.name",       "Circuit.circuitName"),
        ("circuit.city",       "Circuit.Location.locality"),
        ("circuit.country",    "Circuit.Location.country"),
        ("date",               "date"),
        ("time",               "time")
      )    
    )
    
    val resultTrans = JdotTransformer(
      Set(
        ("driver.forename",     "Driver.givenName"),
        ("driver.surname",      "Driver.familyName"),
        ("driver.nationality",  "Driver.nationality"),
        ("team",                "Constructor.name"),
        ("position",            "position"),
        ("carNumber",           "number"),
        ("gridPosition",        "grid"),
        ("points",              "points"),
        ("time",                "Time.time"),
        ("fastestLap",          "FastestLap.Time.time"),
        ("finishLap",           "~{status=(Finished)?(Lead Lap):{~status=(+1 Lap)?status:nothing}}"),
        ("dnfReason",           "~{status=(Finished)?nothing:{~status=(+1 Lap)?nothing:status}}"),
        ("status",              "~{status=(Finished)?(Finished):{~status=(+1 Lap)?(Finished):(DNF)}}")
      )    
    )
    
    val attacher = JdotAttacher(Set(("results", "")))
    
    it("should be equals to ausF1Simple") {
      val transformed = jcTrans.transform(ausF1, List(JsonArrayTransformAttachment("Results", ausF1, resultTrans, attacher)));
      println(transformed)
      assert(parse(ausF1Simple) == parse(transformed));
    }
  }
  
}