package com.superpixel.jdot.json4s

import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import scala.io.Source
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import com.superpixel.jdot.pathing._

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
      val jPath = JPath(JObjectPath(LiteralKey("symbolSingle")))
      accessor.getValue(jPath)
    }
    assertResult(JBool(true)) {
      val jPath = JPath(JObjectPath(LiteralKey("boolean")))
      accessor.getValue(jPath)
    }
  }
  
  it should "extract from object path" in {
    val accessor = JValueAccessor(jsonVal)
    
    assertResult(JBool(false)) {
      val jPath = JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("gender")), JObjectPath(LiteralKey("female")))
      accessor.getValue(jPath)
    }
    assertResult(JInt(2)) {
      val jPath = JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("small")))
      accessor.getValue(jPath)
    }
  }

  it should "extract from object path from value" in {
    val accessor = JValueAccessor(jsonVal)
        
        assertResult(JString("Fred")) {
      val jPath = JPath(JObjectPath(LiteralKey("innerRefObj")), JObjectPath(KeyFromPath(
          JPath(JObjectPath(LiteralKey("innerRefKey"))) 
          )), JObjectPath(LiteralKey("name")))
          
          accessor.getValue(jPath)
    }
    
    assertResult(JBool(true)) {
      val jPath = JPath(JObjectPath(LiteralKey("jsonObj")), 
          JObjectPath(LiteralKey("gender")), 
          JObjectPath(KeyFromPath(
              JPath(JObjectPath(LiteralKey("symbolList")), JArrayPath(LiteralIndex(4))))))
              accessor.getValue(jPath)
    }
  }
  
  it should "extract from array path" in {
    val accessor = JValueAccessor(jsonVal)

    assertResult(JString("old")) {
      val jPath = JPath(JObjectPath(LiteralKey("symbolList")), JArrayPath(LiteralIndex(3)))
      accessor.getValue(jPath)
    }
    assertResult(JString("b")) {
      val jPath = JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("misc")), JArrayPath(LiteralIndex(1)))
      accessor.getValue(jPath)
    }
  }
  
  it should "extract from array path from value" in {
    val accessor = JValueAccessor(jsonVal)

    assertResult(JString("young")) {
      val jPath = JPath(JObjectPath(LiteralKey("symbolList")), JArrayPath(IndexFromPath(JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("small"))))))
      accessor.getValue(jPath)
    }
  }
  
  it should "extract a jpathvalue" in {
    val accessor = JValueAccessor(jsonVal)
    
    assertResult(JString("Hello World!")) {
      val jPath = JPath(JPathValue("Hello World!", None))
      accessor.getValue(jPath)
    }
    
    assertResult(JDouble(3.14)) {
      val jPath = JPath(JPathValue("3.14", Some(JTransmute("n", None))))
      accessor.getValue(jPath)
    }
  }
  
  it should "extract from link path" in {
    val accessor = JValueAccessor(jsonVal, linkLamb)
    
    assertResult(JString("example-url")) {
      val jPath = JPath(JObjectPath(LiteralKey("mediaSingle")), JObjectPath(LiteralKey("sys")), JObjectPath(LiteralKey("id")), JPathLink, JObjectPath(LiteralKey("file")), JObjectPath(LiteralKey("url")))
      accessor.getValue(jPath)
    }
    
    assertResult(JString("velma")) {
      val jPath = JPath(JObjectPath(LiteralKey("mediaSingle")), JObjectPath(LiteralKey("sys")), JObjectPath(LiteralKey("id")), JPathLink, JObjectPath(LiteralKey("file")), JObjectPath(LiteralKey("creators")), JArrayPath(LiteralIndex(2)))
      accessor.getValue(jPath)
    }
  }
  
  it should "date format" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JString("2015:12:25")) {
      val jPath = JPath(JObjectPath(LiteralKey("date")), JTransmute("date", Some(LiteralArgument("yyyy:MM:dd"))))
      accessor.getValue(jPath)
    }
  }
  
  it should "interpret a nested transmute argument" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JString("2015-12-25")) {
      val jPath = JPath(JObjectPath(LiteralKey("date")), JTransmute("date", Some(NestedArgument(JPath(JObjectPath(LiteralKey("dateFormat")))))))
      accessor.getValue(jPath)
    }
    
    assertResult(JString("£15")) {
      val jPath = JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("price")), JTransmute("cur", Some(NestedArgument(JPath(
        JStringFormat(
              Seq(FormatLiteral("0"), ReplaceHolder),
              Seq(JPath(JObjectPath(LiteralKey("currency")))))    
      )))))
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
      accessor.getValue(JPath(JObjectPath(LiteralKey("hello")), JDefaultValue("world", None)))
    }
  }
  
  it should "take default and parse to correct JValue, after not finding path" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JInt(3)) {
      accessor.getValue(JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("medium")), JDefaultValue("3", Some(JTransmute("n", None)))))
    }
  }
  
  it should "take default, after not finding path, and transmute to integer" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JInt(3)) {
      accessor.getValue(JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("medium")), JDefaultValue("3", None), JTransmute("n", None)))
    }
  }
  
  it should "take default for out of bounds array access" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JString("d")) {
      accessor.getValue(JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("misc")), JArrayPath(LiteralIndex(3)), JDefaultValue("d", None)))
    }
  }
  
  it should "take default when link cannot be made" in {
    val accessor = JValueAccessor(jsonVal, linkLamb)
    
    assertResult(JString("example-url")) {
      val jPath = JPath(JObjectPath(LiteralKey("referenceSingle")), JObjectPath(LiteralKey("sys")), JObjectPath(LiteralKey("id")), JPathLink, JObjectPath(LiteralKey("file")), JObjectPath(LiteralKey("url")), JDefaultValue("example-url", None))
      accessor.getValue(jPath)
    }
  }
  
  it should "take midway default values if path not found before hand" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JBool(true)) {
      accessor.getValue(JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("gender")), JDefaultValue("false", None), JObjectPath(LiteralKey("male")), JDefaultValue("false", None)))
    }
    assertResult(JString("true")) {
      accessor.getValue(JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("gender")), JDefaultValue("false", None), JObjectPath(LiteralKey("unknown")), JDefaultValue("true", None)))
    }
    assertResult(JString("true")) {
      accessor.getValue(JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("hender")), JDefaultValue("true", None), JObjectPath(LiteralKey("female")), JDefaultValue("false", None)))
    }
    assertResult(JBool(false)) {
      accessor.getValue(JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("hender")), JDefaultValue("true", None), JObjectPath(LiteralKey("female")), JDefaultValue("false", None), JTransmute("b", Some(LiteralArgument("!")))))
    }
  }
  
  it should "return JNothing on missing object field" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("medium")))
      accessor.getValue(jPath)
    }
  }
  
  it should "return JNothing when trying to interpret a primitive as an object" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath(LiteralKey("boolean")), JObjectPath(LiteralKey("isTrue")))
      accessor.getValue(jPath)
    }
  }
  
  it should "return JNothing on missing array index" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath(LiteralKey("jsonObj")), JObjectPath(LiteralKey("misc")), JArrayPath(LiteralIndex(5)))
      accessor.getValue(jPath)
    }
  }
  
  it should "return JNothing when trying to interpret a primitive as an array" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath(LiteralKey("boolean")), JArrayPath(LiteralIndex(0)))
      accessor.getValue(jPath)
    }
  }
  
  it should "return JNothing when following a link returns None" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JNothing) {
      val jPath = JPath(JObjectPath(LiteralKey("referenceSingle")), JObjectPath(LiteralKey("sys")), JObjectPath(LiteralKey("id")), JPathLink)
      accessor.getValue(jPath)
    }
  }
  
  it should "format to String Format path spec" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JString("Is female? false. First Misc the Small: a2. Defaulted? defaulted")) {
      val jPath = 
        JPath(JObjectPath(LiteralKey("jsonObj")),
            JStringFormat(
                Seq(FormatLiteral("Is female? "), ReplaceHolder, FormatLiteral(". First Misc the Small: "), ReplaceHolder, ReplaceHolder, FormatLiteral(". Defaulted? "), ReplaceHolder),
                Seq(
                    JPath(JObjectPath(LiteralKey("gender")), JObjectPath(LiteralKey("female"))),
                    JPath(JObjectPath(LiteralKey("misc")), JArrayPath(LiteralIndex(0))),
                    JPath(JObjectPath(LiteralKey("small"))),
                    JPath(JObjectPath(LiteralKey("misc")), JArrayPath(LiteralIndex(5)), JDefaultValue("defaulted", None)))))
      accessor.getValue(jPath)
    }
  }
  
  it should "take true/false path based on condition path" in {
    val accessor = JValueAccessor(jsonVal)
    assertResult(JInt(5)) {
      val jPath = JPath(JObjectPath(LiteralKey("jsonObj")), JConditional(
            JPath(JObjectPath(LiteralKey("gender")), JObjectPath(LiteralKey("male"))),
            None,
            JPath(JObjectPath(LiteralKey("big"))),
            JPath(JObjectPath(LiteralKey("misc")), JArrayPath(LiteralIndex(2)))))
      accessor.getValue(jPath)
    }
    assertResult(JString("c")) {
      val jPath = JPath(JObjectPath(LiteralKey("jsonObj")), JConditional(
            JPath(JObjectPath(LiteralKey("gender")), JObjectPath(LiteralKey("female"))),
            None,
            JPath(JObjectPath(LiteralKey("big"))),
            JPath(JObjectPath(LiteralKey("misc")), JArrayPath(LiteralIndex(2)))))
      accessor.getValue(jPath)
    }
  }
  
}