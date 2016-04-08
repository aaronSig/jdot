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
import com.superpixel.jdot.JsonContentTransformer
import com.superpixel.jdot.Attachment
import com.superpixel.jdot.JsonArrayTransformAttachment
import com.superpixel.jdot.JsonContentAttacher

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
    
    
    
  }
  
  
  
  
  describe("Simplified ausF1 json") {
    import scala.language.implicitConversions

    //     TO                  FROM
    val jcTrans = JsonContentTransformer(
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
    
    val resultTrans = JsonContentTransformer(
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
    
    val attacher = JsonContentAttacher(Set(("results", "")))
    
    it("should be equals to ausF1Simple") {
      val transformed = jcTrans.transform(ausF1, List(JsonArrayTransformAttachment("Results", ausF1, resultTrans, attacher)));
      println(transformed)
      assert(parse(ausF1Simple) == parse(transformed));
    }
  }
  
}