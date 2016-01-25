package com.superpixel.advokit.json.lift

import scala.reflect.Manifest

import com.superpixel.advokit.json.pathing.JPathPair
import com.superpixel.advokit.mapper._

import org.json4s._
import org.json4s.native.JsonMethods._

class JValueMapper[T](transformer: JValueTransformer, extractor: JValueExtractor[T]) extends JsonContentMapper[T] {
  
  override def map(jsonContent: String, attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T = {
    val jValContent = parse(jsonContent)
    mapJValue(jValContent, attachments, localMerges, additionalInclusions)
  }

  
  def mapJValue(json: JValue, attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T = {
    val transformed = transformer.transformJValue(json, attachments, localMerges, additionalInclusions)
    extractor.extractFromJValue(transformed)
  }
  
}


object JValueMapper {

  def forTargetClass[T](targetClass: Class[T], pathMapping: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions): JValueMapper[T] = {
    implicit val m: Manifest[T] = Manifest.classType(targetClass)
    apply[T](pathMapping, merges, inclusions)
  }
  
  
  def apply[T](pathMapping: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions)(implicit m: Manifest[T]): JValueMapper[T] = {
    val transformer = JValueTransformer(pathMapping, merges, inclusions)
    val extractor = JValueExtractor()
    new JValueMapper[T](transformer, extractor)
  } 
}