package com.superpixel.advokit.json.lift

import scala.reflect.Manifest

import org.json4s._
import org.json4s.native.JsonMethods._

import com.superpixel.advokit.json.pathing.JPathPair
import com.superpixel.advokit.mapper._

class JValueMapper[T](transformer: JsonContentTransformer, extractor: JsonContentExtractor[T]) extends JsonContentMapper[T] {
  
  override def map(jsonContent: String, attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T = {
    val jValContent = parse(jsonContent)
    mapJValue(jValContent, attachments, localMerges, additionalInclusions)
  }

  
  def mapJValue(json: JValue, attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T = (transformer, extractor) match {
    case (trans: JValueTransformer, extrct: JValueExtractor[T]) => {
    	val transformed = trans.transformJValue(json, attachments, localMerges, additionalInclusions)
    	extrct.extractFromJValue(transformed)      
    }
    case (trans: JValueTransformer, _ ) => {
      val transformed = trans.transformJValue(json, attachments, localMerges, additionalInclusions)
      extractor.extract(compact(render(transformed)))
    }
    case _ => {
      val transformed = transformer.transform(compact(render(json)), attachments, localMerges, additionalInclusions)
      extractor.extract(transformed)
    }
  }
  
}


object JValueMapper {

  def withTargetClassAndTransform[T](targetClass: Class[T], pathMapping: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions): JValueMapper[T] = {
    implicit val m: Manifest[T] = Manifest.classType(targetClass)
    val transformer = JValueTransformer(pathMapping, merges, inclusions)
    val extractor = JValueExtractor.forClass(targetClass)
    new JValueMapper[T](transformer, extractor)
  }
  
  def apply[T](transformer: JsonContentTransformer, extractor: JsonContentExtractor[T]): JValueMapper[T] = {
    new JValueMapper[T](transformer, extractor)
  }
  
  def apply[T](transformer: JValueTransformer)(implicit m: Manifest[T]): JValueMapper[T] = {
    val extractor = JValueExtractor[T]()
    new JValueMapper[T](transformer, extractor)
  }
  
  def withTransform[T](pathMapping: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions)(implicit m: Manifest[T]): JValueMapper[T] = {
    val transformer = JValueTransformer(pathMapping, merges, inclusions)
    val extractor = JValueExtractor[T]()
    new JValueMapper[T](transformer, extractor)
  } 
  
  def withExtractAndTransform[T](extractFn: JValue => T, pathMapping: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions)(implicit m: Manifest[T]): JValueMapper[T] = {
    val transformer = JValueTransformer(pathMapping, merges, inclusions)
    val extractor = JValueExtractor(extractFn)
    new JValueMapper[T](transformer, extractor)
  }
}