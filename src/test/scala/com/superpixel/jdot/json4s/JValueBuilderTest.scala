package com.superpixel.jdot.json4s

import scala.math.BigInt.int2bigInt

import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.superpixel.jdot.pathing._

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._



class JValueBuilderTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  "JValueBuilder build" should "be able to build a simple object" in {
    
    val pathVals: Set[(JPath, Any)] = Set(
      ("integer", 3),
      ("string", "hello"),
      ("boolean", true),
      ("double", 2.45)
    )
    
    val builder = JValueBuilder()

    val expected: JValue = 
        ("integer" -> 3) ~
        ("string" -> "hello") ~
        ("boolean" -> true) ~
        ("double" -> 2.45)
      
    assert(expected == parse(builder.build(pathVals)))
    println(builder.build(pathVals))
  }
  
  it should "be able to build a nested object" in {
    
    val pathVals: Set[(JPath, Any)] = Set(
      ("integer", 3),
      ("string", "hello"),
      ("nObj.bool1", true),
      ("nObj.bool2", false),
      ("nObj.nObj2.double1", 2.45),
      ("nObj.nObj2.double2", 3.67)
    )
    
    val builder = JValueBuilder()

    val expected: JValue = 
        ("integer" -> 3) ~
        ("string" -> "hello") ~
        ("nObj" -> 
          ("bool1" -> true) ~
          ("bool2" -> false) ~
          ("nObj2" -> 
            ("double1" -> 2.45) ~
            ("double2" -> 3.67)))
      
    assert(expected == parse(builder.build(pathVals)))
    println(builder.build(pathVals))
  }
  
  "JValueBuilder buildJValue" should "be able to build a simple object" in {
    
    val pathVals: Set[(JPath, JValue)] = Set(
      (JPath(JObjectPath(LiteralKey("integer"))), JInt(3)),
      (JPath(JObjectPath(LiteralKey("string"))), JString("hello")),
      (JPath(JObjectPath(LiteralKey("boolean"))), JBool(true)),
      (JPath(JObjectPath(LiteralKey("double"))), JDouble(2.45))
    )
    
    val builder = JValueBuilder()

    val expected: JValue = 
        ("integer" -> 3) ~
        ("string" -> "hello") ~
        ("boolean" -> true) ~
        ("double" -> 2.45)
      
    assert(expected == builder.buildJValue(pathVals))
    println(pretty(render(builder.buildJValue(pathVals))))
  }
  
  it should "be able to build a nested object" in {
    
    val pathVals: Set[(JPath, JValue)] = Set(
      (JPath(JObjectPath(LiteralKey("integer"))), JInt(3)),
      (JPath(JObjectPath(LiteralKey("string"))), JString("hello")),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("bool1"))), JBool(true)),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("bool2"))), JBool(false)),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("nObj2")), JObjectPath(LiteralKey("double1"))), JDouble(2.45)),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("nObj2")), JObjectPath(LiteralKey("double2"))), JDouble(3.67))
    )
    
    val builder = JValueBuilder()

    val expected: JValue = 
        ("integer" -> 3) ~
        ("string" -> "hello") ~
        ("nObj" -> 
          ("bool1" -> true) ~
          ("bool2" -> false) ~
          ("nObj2" -> 
            ("double1" -> 2.45) ~
            ("double2" -> 3.67)))
      
    assert(expected == builder.buildJValue(pathVals))
    println(pretty(render(builder.buildJValue(pathVals))))
  }
  
  it should "be able to build an array" in {
    
    val pathVals: Set[(JPath, JValue)] = Set(
      (JPath(JArrayPath(LiteralIndex(2))), JString("it's")),
      (JPath(JArrayPath(LiteralIndex(3))), JString("margaret")),
      (JPath(JArrayPath(LiteralIndex(1))), JString("world")),
      (JPath(JArrayPath(LiteralIndex(0))), JString("hello"))
    )
    
    val builder = JValueBuilder()

    val expected: JValue = List("hello", "world", "it's", "margaret")
    
    assert(expected == builder.buildJValue(pathVals))
    println(pretty(render(builder.buildJValue(pathVals))))
  }
  
  it should "be able to build an array with missing indexes, that should minimise when stringified" in {
    
    val pathVals: Set[(JPath, JValue)] = Set(
      (JPath(JArrayPath(LiteralIndex(5))), JString("it's")),
      (JPath(JArrayPath(LiteralIndex(6))), JString("margaret")),
      (JPath(JArrayPath(LiteralIndex(3))), JString("world")),
      (JPath(JArrayPath(LiteralIndex(1))), JString("hello"))
    )
    
    val builder = JValueBuilder()

    val expected: JValue = JArray(List(JNothing, JString("hello"), JNothing, JString("world"), JNothing, JString("it's"), JString("margaret")))
    val returned: JValue = builder.buildJValue(pathVals)
    
    val expectedJsonString = """["hello","world","it's","margaret"]"""
    val jsonString = compact(render(returned))
    
    assert(expected == returned)
    assert(expectedJsonString == jsonString)
    println(pretty(render(returned)))
  }
  
  it should "be able to build mixed object and array json" in {
    val pathVals: Set[(JPath, JValue)] = Set(
      (JPath(JObjectPath(LiteralKey("integer"))), JInt(3)),
      (JPath(JObjectPath(LiteralKey("string"))), JString("hello")),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("bool1"))), JBool(true)),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("bool2"))), JBool(false)),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("list")), JArrayPath(LiteralIndex(0))), JString("Hello")),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("list")), JArrayPath(LiteralIndex(1))), JString("World!")),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("nObj2")), JObjectPath(LiteralKey("double1"))), JDouble(2.45)),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("nObj2")), JObjectPath(LiteralKey("double2"))), JDouble(3.67))
    )
    
    val builder = JValueBuilder()

    val expected: JValue = 
        ("integer" -> 3) ~
        ("string" -> "hello") ~
        ("nObj" -> 
          ("bool1" -> true) ~
          ("bool2" -> false) ~
          ("list" -> List("Hello", "World!")) ~ 
          ("nObj2" -> 
            ("double1" -> 2.45) ~
            ("double2" -> 3.67)))
      
    assert(expected == builder.buildJValue(pathVals))
    println(pretty(render(builder.buildJValue(pathVals))))
    
  }
  
  it should "throw JsonBuildingException when it encounters a link in the path" in {
    val pathVals: Set[(JPath, JValue)] = Set(
      (JPath(JObjectPath(LiteralKey("integer"))), JInt(3)),
      (JPath(JObjectPath(LiteralKey("string"))), JString("hello")),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("bool1"))), JBool(true)),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("bool2"))), JBool(false)),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("nObj2")), JObjectPath(LiteralKey("double1"))), JDouble(2.45)),
      (JPath(JObjectPath(LiteralKey("nObj")), JObjectPath(LiteralKey("nObj2")), JPathLink, JObjectPath(LiteralKey("double2"))), JDouble(3.67))
    )
    
    val builder = JValueBuilder()
    
    intercept[JsonBuildingException] {
      builder.buildJValue(pathVals);
    }

  }
}