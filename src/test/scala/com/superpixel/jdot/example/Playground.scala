package com.superpixel.jdot.example

import org.scalatest.Matchers
import org.scalatest.FunSpec
import com.superpixel.jdot._

class Playground extends FunSpec with Matchers {

  describe("crazy progress for date") {
    
    val accessor = JDotAccessor("{}")
    
    val date = accessor.getString("(2016-06-12)^date")
    println(date)
    
    val pretty = accessor.getString("(2016-06-12)^date<(pretty_:day)")
    println(pretty)
    
     val str = accessor.getString("(2016-06-12)^date<(pretty_:day)^s<(.2:t)")
    println(str)
    
    val ratio = accessor.getNumber("(2016-06-12)^date<(pretty_:day)^s<(.2:t)^ratio<(!25)")
    println(ratio)
  }

  describe("http scheme adder") {

    val accessorGood = JDotAccessor(
      """{
          "home_page" : "http://www.connemaraheritage.com"
        }
      """.stripMargin)

    val accessorBad = JDotAccessor(
      """{
          "home_page" : "www.connemaraheritage.com"
        }
      """.stripMargin)

    val accessorSecure = JDotAccessor(
      """{
          "home_page" : "https://www.connemaraheritage.com"
        }
      """.stripMargin)

    println(accessorGood.getString("~home_page^s<(.4)=(http)?home_page:|(http://{home_page})"));
    println(accessorBad.getString("~home_page^s<(.4)=(http)?home_page:|(http://{home_page})"));
    println(accessorSecure.getString("~home_page^s<(.4)=(http)?home_page:|(http://{home_page})"));

  }

  describe("string list ops") {

    val json = """{"key":"pockets", "value":["one", "two", "three"]}"""

    val nestTrans = JDotTransformer(Set(
      ("id", "1234"),
      ("value", "_this"),
      ("label", "_this^s<(1u)"),
      ("enables", "~_this=(two)?(true^b):(false^b)"),
      ("is_the_one", "~_this=(three)?(yup):_nothing")
    ))
    val attacher = JDotAttacher(
      Set(("options", "")), Some("value"), Some(nestTrans)
    )

    val transformer = JDotTransformer(Set(), List(attacher))

    val transformed = transformer.transform(json);
    println(transformed)

  }
  
}