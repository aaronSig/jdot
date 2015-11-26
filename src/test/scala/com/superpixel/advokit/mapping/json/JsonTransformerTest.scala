package com.superpixel.advokit

import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import scala.io.Source
import net.liftweb.json.JsonAST.JValue

class JsonTransformerTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  override def beforeAll {
    import net.liftweb.json.JsonDSL._;
    import net.liftweb.json.JsonParser;
    
    val jsonLines = Source.fromFile("ExampleJson.json").getLines;
    
    val jsonVal = JsonParser.parse(jsonLines.mkString)
    
  } 
  
  "JsonTransformer transform" should "take json and transform it to new json based on fieldMap" in {
    
    val pathMap = Set(
    )
    
    
  }
  
}