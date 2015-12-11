package com.superpixel.advokit.json.lift

import scala.reflect.Manifest

import com.superpixel.advokit.json.pathing.JPathPair
import com.superpixel.advokit.mapper.JsonContentMapper

import org.json4s._
import org.json4s.native.JsonMethods._
import muster._

class JValueMapper[T](m: Manifest[T], transformer: JValueTransformer) extends JsonContentMapper[T] {

  implicit var manifest: Manifest[T] = m
//  implicit val cons: Consumer[T] = consumer
  implicit val formats = DefaultFormats + new JavaListSerializer
  
  override def map(jsonContent: String, additionalInclusions: Map[String, String]): T = {
    val jValContent = parse(jsonContent)
    
		import JValueMapper._
    val transformed = transformer.transform(jValContent, parseInclusions(additionalInclusions))
    
    transformed.extract[T]

  }
  
  override def map(jsonContent: String): T = map(jsonContent, Map())
  
}

object JValueMapper {

  def forTargetClass[T](targetClass: Class[T], pathMapping: Set[JPathPair], jsonInclusions: Map[String, String] = Map()): JValueMapper[T] = {
    implicit val m: Manifest[T] = Manifest.classType(targetClass)
    apply[T](pathMapping, jsonInclusions)
  }
  
  
  def apply[T](pathMapping: Set[JPathPair], jsonInclusions: Map[String, String] = Map())(implicit m: Manifest[T]): JValueMapper[T] = {
    val transformer = JValueTransformer(pathMapping, parseInclusions(jsonInclusions))
    new JValueMapper[T](m, transformer)
  } 
  
  private def parseInclusions(inc: Map[String, String]): Map[String, JValue] = {
    inc.map {
      case (key, jsonStr) => (key, parse(jsonStr))
    } 
  }
}