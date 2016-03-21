package com.superpixel.advokit.json.lift

import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

class JValueTransmuterTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  
  "JValueTransmuter b" should "convert bools and numbers appropriately" in {
    
    assertResult(JBool(true)) {
      JValueTransmuter.transmute(JBool(true), "b", None)
    }
    
    assertResult(JBool(true)) {
      JValueTransmuter.transmute(JInt(52), "b", None)
    }
    
    assertResult(JBool(false)) {
      JValueTransmuter.transmute(JDouble(0.0d), "b", None)
    }
  }
  
  it should "convert strings appropriately" in {
    
    assertResult(JBool(true)) {
      JValueTransmuter.transmute(JString("true"), "b", None)
    }
    assertResult(JBool(false)) {
      JValueTransmuter.transmute(JString("FALSE"), "b", None)
    }
    assertResult(JBool(true)) {
      JValueTransmuter.transmute(JString("t"), "b", None)
    }
    assertResult(JBool(false)) {
      JValueTransmuter.transmute(JString("n"), "b", None)
    }
    assertResult(JBool(true)) {
      JValueTransmuter.transmute(JString("Hello World!"), "b", None)
    }
  }
  
  it should "convert objects and arrays on existence" in {
    assertResult(JBool(true)) {
      val obj: JObject = 
        ("one" ->
          ("two" -> 2) ~
          ("three" -> 3)
        )
      JValueTransmuter.transmute(obj, "b", None)
    }
    
    assertResult(JBool(false)) {
      val obj: JObject = JObject(Nil)
      JValueTransmuter.transmute(obj, "b", None)
    }
    
    assertResult(JBool(true)) {
      val arr: JArray = JArray(List(3, 5, 7, 9))
      JValueTransmuter.transmute(arr, "b", None)
    }
    
    assertResult(JBool(false)) {
      val arr: JArray = JArray(Nil)
      JValueTransmuter.transmute(arr, "b", None)
    }
    
    assertResult(JBool(false)) {
      JValueTransmuter.transmute(JNothing, "b", None)
    }
    
    assertResult(JBool(false)) {
      JValueTransmuter.transmute(JNull, "b", None)
    }
  }
  
  it should "take bang/not argument" in {
    assertResult(JBool(false)) {
      JValueTransmuter.transmute(JBool(true), "b", Some("NOT"))
    }
    
    assertResult(JBool(true)) {
      val obj: JObject = JObject(Nil)
      JValueTransmuter.transmute(obj, "b", Some("!"))
    }
    
    assertResult(JBool(true)) {
      JValueTransmuter.transmute(JString("FALSE"), "b", Some("not"))
    }
  }
  
  "JValueTransmuter n" should "convert bools appropriately" in {
    assertResult(JInt(1)) {
      JValueTransmuter.transmute(JBool(true), "n", None)
    }
    
    assertResult(JInt(0)) {
      JValueTransmuter.transmute(JBool(false), "n", None)
    }
  }
  
  it should "leave number values alone" in {
    assertResult(JInt(53)) {
      JValueTransmuter.transmute(JInt(53), "n", None)
    }
    
    assertResult(JDouble(72.3d)) {
      JValueTransmuter.transmute(JDouble(72.3d), "n", None)
    }
    
    val bd: BigDecimal = BigDecimal(4l, 5)
    assertResult(JDecimal(bd)) {
      JValueTransmuter.transmute(JDecimal(bd), "n", None)
    }
  }
  
  it should "convert strings" in {
    assertResult(JInt(53)) {
      JValueTransmuter.transmute(JString("53"), "n", None)
    }
    
    assertResult(JDouble(72.3d)) {
      JValueTransmuter.transmute(JString("72.3"), "n", None)
    }
    
    assertResult(JLong(255)) {
      JValueTransmuter.transmute(JString("0xff"), "n", None)
    }
  }
  
  it should "convert strings with suffixes" in {
    assertResult(JLong(53)) {
      JValueTransmuter.transmute(JString("53l"), "n", None)
    }
    
    assertResult(JDouble(72.3d)) {
      JValueTransmuter.transmute(JString("72.3d"), "n", None)
    }
    
    assertResult(JInt(72)) {
      JValueTransmuter.transmute(JString("72i"), "n", None)
    }
  }
  
  it should "convert strings based on output argument" in {
    assertResult(JDouble(53.0d)) {
      JValueTransmuter.transmute(JString("53"), "n", Some("f"))
    }
    
    assertResult(JInt(72)) {
      JValueTransmuter.transmute(JString("72.3"), "n", Some("i"))
    }
  }
  
  it should "convert strings based on input argument base" in {
    assertResult(JLong(22)) {
      JValueTransmuter.transmute(JString("10110"), "n", Some("2"))
    }
    
    assertResult(JLong(255)) {
      JValueTransmuter.transmute(JString("ff"), "n", Some("16"))
    }
  }
  
  it should "convert strings based on input argument base and output argument" in {
    assertResult(JDouble(22.0d)) {
      JValueTransmuter.transmute(JString("10110"), "n", Some("2:d"))
    }
  }
  
  it should "throw exception on object, array or empty input" in {
    intercept[JsonTransmutingException] {
      val obj: JObject = 
        ("one" ->
          ("two" -> 2) ~
          ("three" -> 3)
        )
      JValueTransmuter.transmute(obj, "n", None)
    }
    
    intercept[JsonTransmutingException] {
      val arr: JArray = JArray(List(2, 4, 6))
      JValueTransmuter.transmute(arr, "n", None)
    }
    
    intercept[JsonTransmutingException] {
      JValueTransmuter.transmute(JNothing, "n", None)
    }
    
    intercept[JsonTransmutingException] {
      JValueTransmuter.transmute(JNull, "n", None)
    }
  }
  
}