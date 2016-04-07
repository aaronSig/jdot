package com.superpixel.advokit.json.example

import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import com.superpixel.advokit.json.pathing._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import scala.io.Source
import org.scalatest.FunSpec
import com.superpixel.advokit.mapper.JsonContentTransformer

class AusGrandPrixExample extends FunSpec with Matchers {

  val ausF1: String = {
      val buffSource = Source.fromURL(getClass.getResource("/pl-league-week-14.json"))
      val jsonLines = buffSource.getLines
      compact(render(parse(jsonLines.mkString)))
  }
  
  describe("Simplify ausf1 json") {
    //     TO          FROM
    val jcTrans = JsonContentTransformer(
      Set(
        ("season",     "season"),
        ("round",      "round^n"),
        ("raceName",   "raceName"),
        ("circuit",    "Circuit.circuitName"),
        ("city",       "Circuit.Location.locality"),
        ("country",    "Circuit.Location.country")  
      )    
    )
    
  }
  
}