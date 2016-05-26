package com.superpixel.jdot.example

import org.scalatest.Matchers
import org.scalatest.FunSpec
import com.superpixel.jdot.JDotAccessor

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
  
}