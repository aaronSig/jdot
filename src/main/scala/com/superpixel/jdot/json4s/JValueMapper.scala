package com.superpixel.jdot.json4s

import scala.reflect.Manifest

import org.json4s._
import org.json4s.native.JsonMethods._

import com.superpixel.jdot.pathing._
import com.superpixel.jdot._

class JValueMapper[T](transformer: JDotTransformer, extractor: JDotExtractor[T]) extends JDotMapper[T] {
  
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
  
  def apply[T](transformer: JDotTransformer, extractor: JDotExtractor[T]): JValueMapper[T] = {
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