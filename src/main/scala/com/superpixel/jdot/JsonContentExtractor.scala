package com.superpixel.jdot

import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.jdot.json4s.JValueExtractor

trait JsonContentExtractor[T] {

  def extract(json: String): T
  
}


object JsonContentExtractor {
  
  def apply[T](extractFn: String => T): JsonContentExtractor[T] = {
    val extractFnJValue = (json: JValue) => extractFn(compact(render(json)))
    JValueExtractor[T](extractFnJValue)
  }
  
  def apply[T](typeHintList: List[Class[_]] = Nil, typeHintFieldName: Option[String] = None)(implicit m: Manifest[T]): JsonContentExtractor[T] = typeHintFieldName match {
    case Some(s) => JValueExtractor[T](typeHintList, s)
    case None => JValueExtractor[T](typeHintList)
  }
  
  def forClass[T](targetClass: Class[T], typeHintList: List[Class[_]] = Nil, typeHintFieldName: Option[String] = None): JsonContentExtractor[T] = typeHintFieldName match {
    case Some(s) => JValueExtractor.forClass(targetClass, typeHintList, s)
    case None => JValueExtractor.forClass(targetClass, typeHintList)
  }
}