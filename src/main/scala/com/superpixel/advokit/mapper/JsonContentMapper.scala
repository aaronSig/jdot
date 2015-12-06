package com.superpixel.advokit.mapper

import collection.JavaConverters._
import com.superpixel.advokit.json.pathing._

trait JsonContentMapper[T] {
  
  def map(jsonContent: String, additionInclusions: java.util.Map[java.lang.String, java.lang.String]) {
    map(jsonContent, additionInclusions.asScala.toMap)
  }

  def map(jsonContent: String, additionInclusions: scala.collection.immutable.Map[String, String]): T
  
  def map(jsonContent: String): T
  
}