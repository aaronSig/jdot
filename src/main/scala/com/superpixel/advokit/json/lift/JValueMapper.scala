package com.superpixel.advokit.json.lift

import com.superpixel.advokit.json.pathing.JPathPair
import com.superpixel.advokit.mapper.JsonContentMapper

import net.liftweb.json._

class JValueMapper[T](implicit val m: Manifest[T])(targetClass: Class[T], transformer: JValueTransformer) extends JsonContentMapper[T](targetClass) {

  def map(jsonContent: String, additionalInclusions: Map[String, String]): T = {
    val jValContent = parse(jsonContent)
    
		import JValueMapper._
    val transformed = transformer.transform(jValContent, parseInclusions(additionalInclusions))
    
    implicit val formats = DefaultFormats
    transformed.extract[T]
  }
  
  def map(jsonContent: String): T = map(jsonContent, Map())
  
}

object JValueMapper {

  
  def appy[T](targetClass: Class[T], pathMapping: Set[JPathPair], jsonInclusions: Map[String, String] = Map()): JValueMapper[T] = {
    val transformer = JValueTransformer(pathMapping, parseInclusions(jsonInclusions))
    new JValueMapper(targetClass, transformer)
  } 
  
  private def parseInclusions(inc: Map[String, String]): Map[String, JValue] = {
    inc.map {
      case (key, jsonStr) => (key, parse(jsonStr))
    } 
  }
}