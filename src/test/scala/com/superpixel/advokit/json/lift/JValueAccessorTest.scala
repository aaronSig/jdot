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

class JValueAccessorTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

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

    
  
  "JValueAccessor getValue" should "extract from simple path" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JString("Little Yeah")) {
      val jPath = JPath(JObjectPath("symbolSingle"))
      accessor.getValue(jPath)
    }
    assertResult(JBool(true)) {
      val jPath = JPath(JObjectPath("boolean"))
      accessor.getValue(jPath)
    }
  }
  
  it should "extract from object path" in {
    val accessor = JValueAccessor(jsonVal)
    
    assertResult(JBool(false)) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("gender"), JObjectPath("female"))
      accessor.getValue(jPath)
    }
    assertResult(JInt(2)) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("small"))
      accessor.getValue(jPath)
    }
  }
  
  it should "extract from array path" in {
    val accessor = JValueAccessor(jsonVal)

    assertResult(JString("old")) {
      val jPath = JPath(JObjectPath("symbolList"), JArrayPath(3))
      accessor.getValue(jPath)
    }
    assertResult(JString("b")) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("misc"), JArrayPath(1))
      accessor.getValue(jPath)
    }
  }
  
  it should "extract from link path" in {
    val accessor = JValueAccessor(jsonVal, linkLamb)
    
    assertResult(JString("example-url")) {
      val jPath = JPath(JObjectPath("mediaSingle"), JObjectPath("sys"), JObjectPath("id"), JPathLink, JObjectPath("file"), JObjectPath("url"))
      accessor.getValue(jPath)
    }
    
    assertResult(JString("velma")) {
      val jPath = JPath(JObjectPath("mediaSingle"), JObjectPath("sys"), JObjectPath("id"), JPathLink, JObjectPath("file"), JObjectPath("creators"), JArrayPath(2))
      accessor.getValue(jPath)
    }
  }
  
  it should "accept a blank path" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(jsonVal) {
      accessor.getValue(JPath())
    }
  }
  
  it should "take default if found, after not finding path" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JString("world")) {
      accessor.getValue(JPath(JObjectPath("hello"), JDefaultValue("world")))
    }
  }
  
  it should "take default and parse to correct JValue, after not finding path" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JString("3")) {
      accessor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("medium"), JDefaultValue("3")))
    }
  }
  
  it should "take default for out of bounds array access" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JString("d")) {
      accessor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("misc"), JArrayPath(3), JDefaultValue("d")))
    }
  }
  
  it should "take default when link cannot be made" in {
    val accessor = JValueAccessor(jsonVal, linkLamb)
    
    assertResult(JString("example-url")) {
      val jPath = JPath(JObjectPath("referenceSingle"), JObjectPath("sys"), JObjectPath("id"), JPathLink, JObjectPath("file"), JObjectPath("url"), JDefaultValue("example-url"))
      accessor.getValue(jPath)
    }
  }
  
  it should "take midway default values if path not found before hand" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JBool(true)) {
      accessor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("gender"), JDefaultValue("false"), JObjectPath("male"), JDefaultValue("false")))
    }
    assertResult(JString("true")) {
      accessor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("gender"), JDefaultValue("false"), JObjectPath("unknown"), JDefaultValue("true")))
    }
    assertResult(JString("true")) {
      accessor.getValue(JPath(JObjectPath("jsonObj"), JObjectPath("hender"), JDefaultValue("true"), JObjectPath("female"), JDefaultValue("false")))
    }
  }
  
  it should "return JNothing on missing object field" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("medium"))
      accessor.getValue(jPath)
    }
  }
  
  it should "return JNothing when trying to interpret a primitive as an object" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("boolean"), JObjectPath("isTrue"))
      accessor.getValue(jPath)
    }
  }
  
  it should "return JNothing on missing array index" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("jsonObj"), JObjectPath("misc"), JArrayPath(5))
      accessor.getValue(jPath)
    }
  }
  
  it should "return JNothing when trying to interpret a primitive as an array" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("boolean"), JArrayPath(0))
      accessor.getValue(jPath)
    }
  }
  
  it should "return JNothing when following a link returns None" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath("referenceSingle"), JObjectPath("sys"), JObjectPath("id"), JPathLink)
      accessor.getValue(jPath)
    }
  }
  
  it should "format to String Format path spec" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JString("Is female? false. First Misc the Small: a2. Defaulted? defaulted")) {
      val jPath = 
        JPath(JObjectPath("jsonObj"),
            JStringFormat(
                Seq(FormatLiteral("Is female? "), ReplaceHolder, FormatLiteral(". First Misc the Small: "), ReplaceHolder, ReplaceHolder, FormatLiteral(". Defaulted? "), ReplaceHolder),
                Seq(
                    JPath(JObjectPath("gender"), JObjectPath("female")),
                    JPath(JObjectPath("misc"), JArrayPath(0)),
                    JPath(JObjectPath("small")),
                    JPath(JObjectPath("misc"), JArrayPath(5), JDefaultValue("defaulted")))))
      accessor.getValue(jPath)
    }
  }
  
  it should "take true/false path based on condition path" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JInt(5)) {
      val jPath = JPath(JObjectPath("jsonObj"), JConditional(
            JPath(JObjectPath("gender"), JObjectPath("male")),
            None,
            JPath(JObjectPath("big")),
            JPath(JObjectPath("misc"), JArrayPath(2))))
      accessor.getValue(jPath)
    }
    assertResult(JString("c")) {
      val jPath = JPath(JObjectPath("jsonObj"), JConditional(
            JPath(JObjectPath("gender"), JObjectPath("female")),
            None,
            JPath(JObjectPath("big")),
            JPath(JObjectPath("misc"), JArrayPath(2))))
      accessor.getValue(jPath)
    }
  }
  
}