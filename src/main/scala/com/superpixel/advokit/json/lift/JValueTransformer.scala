package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.pathing._

class JValueTransformer(fieldMap: Set[JPathPair], inclusions: Map[String, JValue]) {

  val builder = JValueBuilder(fieldMap.map(_.to))
  def transform(json: JValue, addInclusions: Map[String, JValue] = Map()): JValue = 
    builder.build((getValues(json, addInclusions)))
  
  private def getValues(json: JValue, addInclusions: Map[String, JValue]): Set[(JPath, JValue)] = {
	  val extractor = JValueExtractor(json, (s: String) => addInclusions.get(s).orElse(inclusions.get(s)))

    fieldMap.map { jpm: JPathPair => (jpm.to, extractor.getValue(jpm.from)) }
  }
}

object JValueTransformer {
  
  def apply(fieldMap: Set[JPathPair], inclusions: Map[String, JValue] = Map()) = new JValueTransformer(fieldMap, inclusions)
  
}