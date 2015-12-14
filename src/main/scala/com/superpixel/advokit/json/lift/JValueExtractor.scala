package com.superpixel.advokit.json.lift

import com.superpixel.advokit.mapper._
import org.json4s._
import org.json4s.native.JsonMethods._

class JValueExtractor[T](m: Manifest[T]) extends JsonContentExtractor[T] {

  implicit val manifest: Manifest[T] = m
  implicit val formats = DefaultFormats + new JavaListSerializer
  
  override def extract(json: String): T = {
    extractFromJValue(parse(json))
  }
  
  def extractFromJValue(json: JValue): T = {
    json.extract[T]
  }
  
}

object JValueExtractor {
  
  def forClass[T](targetClass: Class[T]): JValueExtractor[T] = {
    implicit val m: Manifest[T] = Manifest.classType(targetClass)
    apply[T]
  }
  
  def apply[T](implicit m: Manifest[T]): JValueExtractor[T] = {
    new JValueExtractor[T](m);
  }
  
}