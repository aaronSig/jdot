package com.superpixel.advokit.mapper

import collection.JavaConverters._
import com.superpixel.advokit.json.pathing.JPathPair
import com.superpixel.advokit.json.lift.JValueMapper

class JsonContentMapperBuilder {

  var inclusions: Option[collection.mutable.Map[String, String]] = None
  var pathMappings: Option[collection.mutable.Map[String, String]] = None
  
  def withPathMappings(mapping: java.util.Map[java.lang.String, java.lang.String]): JsonContentMapperBuilder = {
    this.pathMappings = Some(mapping.asScala)
    this
  }
  
  def withInclusions(inclusions: java.util.Map[java.lang.String, java.lang.String]): JsonContentMapperBuilder = {
    this.inclusions = Some(inclusions.asScala)
    this
  }
  
  def build[T](targetClass: Class[T]): JsonContentMapper[T] = {
    
    implicit val m: Manifest[T] = Manifest.classType(targetClass)
    
    val imInclusions: collection.immutable.Map[String, String] = this.inclusions match {
      case None => collection.immutable.Map()
      case Some(inc) => inc.toMap
    }
    val imMapping: collection.immutable.Set[JPathPair] = this.pathMappings match {
      case None => collection.immutable.Set()
      case Some(pM) => {
        val entrySet: collection.immutable.Set[(String, String)] = pM.toSet
        entrySet.map { case (to:String , from:String) => JPathPair.fromStrings(to, from) }
      }
    }
    JValueMapper.forTargetClass(targetClass, imMapping, imInclusions)
  }
}