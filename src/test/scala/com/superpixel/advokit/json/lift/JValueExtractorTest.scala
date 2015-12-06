package com.superpixel.advokit.json.lift

import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import scala.io.Source
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.pathing._

class JValueExtractorTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  val jsonVal = {
		  val buffSource = Source.fromURL(getClass.getResource("/ExampleContent.json"))
		  val jsonLines = buffSource.getLines
      val temp = parse(jsonLines.mkString)
      buffSource.close()
      temp
  }
  
  val linkLamb = 
      (s: String) => s match {
        case "6t4HKjytPi0mYgs240wkG" => {
          val jLinked: JValue = 
            ("file" ->
              ("url" -> "example-url") ~
              ("fileName" -> "example") ~
              ("size" -> 5432) ~
              ("creators" -> List("fred", "daphne", "velma"))
            )
          Some(jLinked)
        }
        case _ => None
      }

    
  
  "JValueExtractor getValue" should "extract from simple path" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JString("Little Yeah")) {
      val jPath = JPath(JObjectPath("symbolSingle"))
      extractor.getValue(jPath)
    }
    assertResult(JBool(true)) {
      val jPath = JPath(JObjectPath("boolean"))
      extractor.getValue(jPath)
    }
  }
  
  it should "extract from object path" in {
    val extractor = JValueExtractor(jsonVal)
    
    assertResult(JBool(false)) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("gender"), JObjectPath("female"))
      extractor.getValue(jPath)
    }
    assertResult(JInt(2)) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("small"))
      extractor.getValue(jPath)
    }
  }
  
  it should "extract from array path" in {
    val extractor = JValueExtractor(jsonVal)

    assertResult(JString("old")) {
      val jPath = JPath(JObjectPath("symbolList"), JArrayPath(3))
      extractor.getValue(jPath)
    }
    assertResult(JString("b")) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("misc"), JArrayPath(1))
      extractor.getValue(jPath)
    }
  }
  
  it should "extract from link path" in {
    val extractor = JValueExtractor(jsonVal, linkLamb)
    
    assertResult(JString("example-url")) {
      val jPath = JPath(JObjectPath("mediaSingle"), JObjectPath("sys"), JObjectPath("id"), JPathLink, JObjectPath("file"), JObjectPath("url"))
      extractor.getValue(jPath)
    }
    
    assertResult(JString("velma")) {
      val jPath = JPath(JObjectPath("mediaSingle"), JObjectPath("sys"), JObjectPath("id"), JPathLink, JObjectPath("file"), JObjectPath("creators"), JArrayPath(2))
      extractor.getValue(jPath)
    }
  }
  
  it should "accept a blank path" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(jsonVal) {
      extractor.getValue(JPath())
    }
  }
  
  it should "take default if found, after not finding path" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JString("world")) {
      extractor.getValue(JPath(JObjectPath("hello"), JDefaultValue("world")))
    }
  }
  
  it should "take default and parse to correct JValue, after not finding path" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JString("3")) {
      extractor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("medium"), JDefaultValue("3")))
    }
  }
  
  it should "take default for out of bounds array access" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JString("d")) {
      extractor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("misc"), JArrayPath(3), JDefaultValue("d")))
    }
  }
  
  it should "take default when link cannot be made" in {
    val extractor = JValueExtractor(jsonVal, linkLamb)
    
    assertResult(JString("example-url")) {
      val jPath = JPath(JObjectPath("referenceSingle"), JObjectPath("sys"), JObjectPath("id"), JPathLink, JObjectPath("file"), JObjectPath("url"), JDefaultValue("example-url"))
      extractor.getValue(jPath)
    }
  }
  
  it should "take midway default values if path not found before hand" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JBool(true)) {
      extractor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("gender"), JDefaultValue("false"), JObjectPath("male"), JDefaultValue("false")))
    }
    assertResult(JString("true")) {
      extractor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("gender"), JDefaultValue("false"), JObjectPath("unknown"), JDefaultValue("true")))
    }
    assertResult(JString("true")) {
      extractor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("hender"), JDefaultValue("true"), JObjectPath("female"), JDefaultValue("false")))
    }
  }
  
  it should "return JNothing on missing object field" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("medium"))
      extractor.getValue(jPath)
    }
  }
  
  it should "return JNothing when trying to interpret a primitive as an object" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("boolean"), JObjectPath("isTrue"))
      extractor.getValue(jPath)
    }
  }
  
  it should "return JNothing on missing array index" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("misc"), JArrayPath(5))
      extractor.getValue(jPath)
    }
  }
  
  it should "return JNothing when trying to interpret a primitive as an array" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("boolean"), JArrayPath(0))
      extractor.getValue(jPath)
    }
  }
  
  it should "return JNothing when following a link returns None" in {
    val extractor = JValueExtractor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("referenceSingle"), JObjectPath("sys"), JObjectPath("id"), JPathLink)
      extractor.getValue(jPath)
    }
  }
}