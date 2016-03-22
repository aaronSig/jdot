package com.superpixel.advokit.json.lift

import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import com.github.nscala_time.time.Imports._

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
  
  "JValueTransmuter s" should "produce json string from values" in {
    
    assertResult(JString("22.05")) {
      JValueTransmuter.transmute(JDouble(22.05d), "s", None)
    }
    
    assertResult(JString("22")) {
      JValueTransmuter.transmute(JInt(22), "s", None)
    }
    
    assertResult(JString("Hello")) {
      JValueTransmuter.transmute(JString("Hello"), "s", None)
    }
    
    assertResult(JString("true")) {
      JValueTransmuter.transmute(JBool(true), "s", None)
    }
    
    assertResult(JString("""{"hello":"world"}""")) {
      JValueTransmuter.transmute(("hello" -> "world"), "s", None)
    }
    
    assertResult(JString("[1,2,3]")) {
      JValueTransmuter.transmute(JArray(List(1, 2, 3)), "s", None)
    }
    
    assertResult(JString("")) {
      JValueTransmuter.transmute(JNothing, "s", None)
    }
    
    assertResult(JString("null")) {
      JValueTransmuter.transmute(JNull, "s", None)
    }
  }
  
  it should "apply simple one term special arguments" in {
    assertResult(JString("123")) {
      JValueTransmuter.transmute(JInt(21232), "s", Some("1.4"))
    }
    
    assertResult(JString("HELLO")) {
      JValueTransmuter.transmute(JString("Hello"), "s", Some("u"))
    }
    
    assertResult(JString("True")) {
      JValueTransmuter.transmute(JBool(true), "s", Some("1u"))
    }
    
    assertResult(JString("hello")) {
      JValueTransmuter.transmute(JString("heLlo"), "s", Some("l"))
    }
  }
  
  it should "apply printf arguments" in {
    assertResult(JString("Hello  ")) {
      JValueTransmuter.transmute(JString("Hello"), "s", Some("-7"))
    }
  }
  
  it should "apply a sequence of terms in argument, in order" in {
    assertResult(JString("BART S")) {
      JValueTransmuter.transmute(JString("bart simpson"), "s", Some("u:.6"))
    }
    
    assertResult(JString("Simpson   ")) {
      JValueTransmuter.transmute(JString("bart simpson"), "s", Some("5.:1u:-10"))
    }
  }
  
  "JValueTransmute f" should "format numbers via printf" in {
    assertResult(JString("2.200000")) {
      JValueTransmuter.transmute(JDouble(2.2d), "f", None)
    }
    
    assertResult(JString("24343.23")) {
      JValueTransmuter.transmute(JDouble(24343.2333d), "f", Some(".2"))
    }
    
    assertResult(JString("3.00")) {
      JValueTransmuter.transmute(JInt(3), "f", Some(".2"))
    }
    
    assertResult(JString("02.2")) {
      JValueTransmuter.transmute(JDouble(2.22d), "f", Some("04.1"))
    }
  }
  
  it should "format numbers via printf from strings" in {
    assertResult(JString("2.200000")) {
      JValueTransmuter.transmute(JString("2.2d"), "f", None)
    }
    
    assertResult(JString("3.00")) {
      JValueTransmuter.transmute(JString("3"), "f", Some(".2"))
    }
    
  } 
  
  "JValueTransmute i" should "format ints via printf" in {    
    assertResult(JString("2")) {
      JValueTransmuter.transmute(JDouble(2.2d), "i", None)
    }
    
    assertResult(JString("2,343,334")) {
      JValueTransmuter.transmute(JInt(2343334), "i", Some(","))
    }
    
    assertResult(JString("003")) {
      JValueTransmuter.transmute(JInt(3), "i", Some("03"))
    }
    
    assertResult(JString("+5")) {
      JValueTransmuter.transmute(JInt(5), "i", Some("+"))
    }

  }
  
  it should "format ints via printf from strings" in {
    assertResult(JString("2")) {
      JValueTransmuter.transmute(JString("2.2d"), "i", None)
    }
    
    assertResult(JString("+003")) {
      JValueTransmuter.transmute(JString("3"), "i", Some("+04"))
    }
    
  }
  
  "JValueTransmute d" should "format ints via printf" in {    
    assertResult(JString("2")) {
      JValueTransmuter.transmute(JDouble(2.2d), "d", None)
    }
    
    assertResult(JString("2,343,334")) {
      JValueTransmuter.transmute(JInt(2343334), "d", Some(","))
    }
    
    assertResult(JString("003")) {
      JValueTransmuter.transmute(JInt(3), "d", Some("03"))
    }
    
    assertResult(JString("+5")) {
      JValueTransmuter.transmute(JInt(5), "d", Some("+"))
    }

  }
  
  it should "format ints via printf from strings" in {
    assertResult(JString("2")) {
      JValueTransmuter.transmute(JString("2.2d"), "d", None)
    }
    
    assertResult(JString("+003")) {
      JValueTransmuter.transmute(JString("3"), "d", Some("+04"))
    }
    
  }
 
  "JValueTransmute %" should "take a double and convert to percentage" in {
    assertResult(JString("45%")) {
      JValueTransmuter.transmute(JDouble(0.45d), "%", None)
    }
    
    assertResult(JString("45.65%")) {
      JValueTransmuter.transmute(JDouble(0.4565d), "%", None)
    }
  }
  
  it should "take a string and convert to percentage" in {
    assertResult(JString("45%")) {
      JValueTransmuter.transmute(JString("0.45d"), "%", None)
    }
    
    assertResult(JString("45.65%")) {
      JValueTransmuter.transmute(JString("0.4565d"), "%", None)
    }
    
    assertResult(JString("45.65%")) {
      JValueTransmuter.transmute(JString("0.456522d"), "%", None)
    }
  }
  
  it should "take a double and convert to percentage with args" in {
    assertResult(JString("55%")) {
      JValueTransmuter.transmute(JDouble(0.45d), "%", Some("!"))
    }
    
    assertResult(JString("50%")) {
      JValueTransmuter.transmute(JInt(25), "%", Some("50"))
    }
    
    assertResult(JString("75%")) {
      JValueTransmuter.transmute(JInt(25), "%", Some("!100"))
    }
    assertResult(JString("90%")) {
      JValueTransmuter.transmute(JInt(1), "%", Some("!10"))
    }
  }
  
  "JValueTransmute date" should "date format an ISO date string" in {
    
    assertResult(JString("2016-02-28T12:31:17.721Z")) {
      JValueTransmuter.transmute(JString("2016-02-28T12:31:17.721Z"), "date", None)
    }
    
    assertResult(JString("2016:02:28")) {
      JValueTransmuter.transmute(JString("2016-02-28T12:31:17.721Z"), "date", Some("yyyy:MM:dd"))
    }
    
    assertResult(JString("February 28 2016")) {
      JValueTransmuter.transmute(JString("2016-02-28T12:31:17.721Z"), "date", Some("MMMM dd yyyy"))
    }
  }
  
  it should "date format an epoch" in {
    
    assertResult(JString("2016-02-28T00:00:00.000Z")) {
      JValueTransmuter.transmute(JLong(1456617600l), "date", None)
    }
    
    assertResult(JString("2016:02:28")) {
      JValueTransmuter.transmute(JLong(1456617600l), "date", Some("yyyy:MM:dd"))
    }
    
    assertResult(JString("February 28 2016")) {
      JValueTransmuter.transmute(JLong(1456617600l), "date", Some("MMMM dd yyyy"))
    }
  }
  
  it should "date format now string" in {
    
    assertResult(JString(DateTime.now.toString("yyyyDDD'T'HHmmssZ"))) {
      JValueTransmuter.transmute(JString("now"), "date", Some("yyyyDDD'T'HHmmssZ"))
    }
    
    assertResult(JString(DateTime.now.toString("yyyyDDD'T'HHmmss"))) {
      JValueTransmuter.transmute(JString("NOW"), "date", Some("yyyyDDD'T'HHmmss"))
    }

  }
  
  "JValueTransmute ord" should "produce a suffix on any int" in {
    
    assertResult(JString("1st")) {
      JValueTransmuter.transmute(JInt(1), "ord", None)
    }
    
    assertResult(JString("0th")) {
      JValueTransmuter.transmute(JInt(0), "ord", None)
    }
    
    assertResult(JString("154th")) {
      JValueTransmuter.transmute(JInt(154), "ord", None)
    }
    
    assertResult(JString("14802nd")) {
      JValueTransmuter.transmute(JInt(14802), "ord", None)
    }
  } 
  
  "JValueTransmute ord" should "produce a full ordinal word on argument (up to 12)" in {
    
    assertResult(JString("First")) {
      JValueTransmuter.transmute(JInt(1), "ord", Some("full"))
    }
    
    assertResult(JString("Zeroth")) {
      JValueTransmuter.transmute(JInt(0), "ord", Some("full"))
    }
    
    assertResult(JString("Third")) {
      JValueTransmuter.transmute(JInt(3), "ord", Some("full"))
    }
    
    assertResult(JString("Twelth")) {
      JValueTransmuter.transmute(JInt(12), "ord", Some("full"))
    }
    
    assertResult(JString("154th")) {
      JValueTransmuter.transmute(JInt(154), "ord", Some("full"))
    }
    
  } 
}
  