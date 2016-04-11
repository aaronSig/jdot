package com.superpixel.jdot.json4s

import com.superpixel.jdot._
import org.json4s._
import org.json4s.native.JsonMethods._

class JValueExtractor[T](extractFn: JValue => T) extends JdotExtractor[T] {

  
  override def extract(json: String): T = {
    extractFromJValue(parse(json))
  }
  
  def extractFromJValue(json: JValue): T = {
    extractFn(json)
  }
  
}

object JValueExtractor {
  
  val typeHintField = "_t";
  def typeHintFieldForClass(clazz: Class[_]): JField = {
    (typeHintField, JString(clazz.getName))
  }
  
  def forClass[T](targetClass: Class[T], typeHintList: List[Class[_]] = Nil, typeHintFieldName: String = typeHintField): JValueExtractor[T] = {
    implicit val m: Manifest[T] = Manifest.classType(targetClass)
    apply[T](typeHintList, typeHintFieldName)
  }
  
  def apply[T](typeHintList: List[Class[_]] = Nil, typeHintFieldName: String = typeHintField)(implicit m: Manifest[T]): JValueExtractor[T] = {
    
    apply((json: JValue) => {
      implicit val formats = JValueExtractor.formats(typeHintList, typeHintFieldName)
      
      json.extract[T]
    })    
  }
  
  def apply[T](extractFn: JValue => T): JValueExtractor[T] = new JValueExtractor(extractFn)
  
  private def formats(tH: List[Class[_]], tHFieldName: String): Formats = {
    new Formats {
    override val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(tH)
    override val typeHintFieldName = tHFieldName
    override val allowNull = true;
    override val strictOptionParsing = false;
  } + new JavaListSerializer + new JavaOptionalSerializer
  }
  
}