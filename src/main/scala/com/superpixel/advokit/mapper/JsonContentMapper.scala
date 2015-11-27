package com.superpixel.advokit.mapper

import com.superpixel.advokit.json.pathing._

abstract class JsonContentMapper[T](targetClass: Class[T]) {

  def map(jsonContent: String, additionInclusions: Map[String, String]): T
  
  def map(jsonContent: String): T
  
}