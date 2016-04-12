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
      assert(racename == Some("Australian Grand Prix"))
    }
    
    it("can access json objects") {
      val circuit = accessor.getJsonString("circuit")
      assert(circuit == Some("""{"name":"Albert Park Grand Prix Circuit","country":"Australia","city":"Melbourne"}"""))
    }
    
    it("can access json arrays") {
      val results = accessor.getJsonString("results")
      results.foreach { println(_) }
    }
    
    it ("can access nested objects using javascript dot syntax") {
      val raceCity = accessor.getString("circuit.city")
      assert(raceCity == Some("Melbourne"))
    }
    
    it ("can access array elements using javascript square bracket syntax") {
      val winnerName = accessor.getString("results[0].driver.surname")
      assert(winnerName == Some("Rosberg"))
    }
  }
  
  describe("Accessing json with embedded, data lead default values") {
    
    //Set up accessor with our example json
    val accessor = JdotAccessor(ausF1Simple)
    
    it("can access values with a fallback default using round brackets") {
      val racename = accessor.getString("raceName(Unknown Race)")
      assert(racename == Some("Australian Grand Prix"))
      
      val racenameTypo = accessor.getString("raxeName(Unknown Race)")
      assert(racenameTypo == Some("Unknown Race"))
    }
    
    it("can be used to manage differing fields in an array of objects") {
      //Winner has field "finishLap"
      val winnerFinishLap = accessor.getString("results[0].finishLap(DNF)")
      assert(winnerFinishLap == Some("Lead Lap"))
      
      //Seventeenth element of array does not have field "finishLap"
      val seventeenthFinishLap = accessor.getString("results[16].finishLap(DNF)")
      assert(seventeenthFinishLap == Some("DNF"))
    }
    
    it("can be used multiple times in a path to intercept missing elements") {
      //Winner has field "finishLap"
      val winnerFinishLap = accessor.getString("results[0](No entry).finishLap(DNF)")
      assert(winnerFinishLap == Some("Lead Lap"))
      
      //Seventeenth element of "results" array does not have field "finishLap"
      val seventeenthFinishLap = accessor.getString("results[16](No entry).finishLap(DNF)")
      assert(seventeenthFinishLap == Some("DNF"))
      
      //There is no 30th element of the "results" array
      val thirtiethFinishLap = accessor.getString("results[29](No entry).finishLap(DNF)")
      assert(thirtiethFinishLap == Some("No entry"))
    }
  }
  
  describe("Accessing just values (no json interaction) with round brackets") {
    
    //Set up accessor with our example json
    val accessor = JdotAccessor(ausF1Simple)
    
    it("can be used to access a value defined purely in the path") {
      val justValue = accessor.getString("(hello world)")
      assert(justValue == Some("hello world"))
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