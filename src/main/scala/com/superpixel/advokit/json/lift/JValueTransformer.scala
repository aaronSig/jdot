package com.superpixel.advokit.json.lift

import net.liftweb.json._
import com.superpixel.advokit.json.pathing._

class JValueTransformer(fieldMap: Set[JPathMap], inclusions: Map[String, JValue]) {

  val builder = JValueBuilder(fieldMap.map(_.to))
  def transform(json: JValue, addInclusions: Map[String, JValue] = Map()): JValue = 
    builder.build((getValues(json, addInclusions)))
  
  private def getValues(json: JValue, addInclusions: Map[String, JValue]): Set[(JPath, JValue)] = {
	  val extractor = JValueExtractor(json, (s: String) => addInclusions.get(s).orElse(inclusions.get(s)))

    fieldMap.map { jpm: JPathMap => (jpm.to, extractor.getValue(jpm.from)) }
  }
}

object JValueTransformer {
  
  def apply(fieldMap: Set[JPathMap], inclusions: Map[String, JValue]) = new JValueTransformer(fieldMap, inclusions)
  
}