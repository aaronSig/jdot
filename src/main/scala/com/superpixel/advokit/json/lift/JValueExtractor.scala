package com.superpixel.advokit.json.lift

import com.superpixel.advokit.mapper._
import org.json4s._
import org.json4s.native.JsonMethods._

class JValueExtractor[T](m: Manifest[T], typeHintList: List[Class[_]]) extends JsonContentExtractor[T] {

  implicit val manifest: Manifest[T] = m
  val formats = JValueExtractor.formats(typeHintList)
  
  override def extract(json: String): T = {
    extractFromJValue(parse(json))
  }
  
  def extractFromJValue(json: JValue): T = {
    implicit val localFormats = formats;
//    println("To extract: ")
//    println(m.erasure.getName)
//    println(localFormats.typeHints.hints)
//    println(pretty(render(json)))
    json.extract[T]
  }
  
  override def extractorWithTypeHints(localTypeHints: List[Class[_]]): (String => T) = {
    val f = extractorJValueWithTypeHints(localTypeHints)
    (json: String) => f(parse(json))
  }
  
  def extractorJValueWithTypeHints(localTypeHints: List[Class[_]]): (JValue => T) = {
    implicit val localFormats = localTypeHints match {
      case Nil => formats
      case _ => JValueExtractor.formats(localTypeHints ++ typeHintList)
    }
    (json: JValue) => {
//      println("To extract: ")
//      println(m.erasure.getName)
//      println(localFormats.typeHints.hints)
//      println(pretty(render(json)))
      json.extract[T]
    }
  }
  
}

object JValueExtractor {
  
  val typeHintField = "_t";
  def typeHintFieldForClass(clazz: Class[_]): JField = {
    (typeHintField, JString(clazz.getName))
  }
  
  def forClass[T](targetClass: Class[T], typeHintList: List[Class[_]] = Nil): JValueExtractor[T] = {
    implicit val m: Manifest[T] = Manifest.classType(targetClass)
    apply[T](typeHintList)
  }
  
  def apply[T](typeHintList: List[Class[_]] = Nil)(implicit m: Manifest[T]): JValueExtractor[T] = {
    new JValueExtractor[T](m, typeHintList);
  }
  
  private def formats(tH: List[Class[_]]): Formats = {
    new Formats {
    override val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = FullTypeHints(tH)
    override val typeHintFieldName = JValueExtractor.typeHintField
    override val allowNull = true;
    override val strictOptionParsing = false;
  } + new JavaListSerializer + new JavaOptionalSerializer
  }
  
}