package com.superpixel.jdot

import com.superpixel.jdot.pathing.{JPathPair, JPath}
import com.superpixel.jdot.json4s.JValueAttacher
import org.json4s.native.JsonMethods._

/***
  *
  *
  */
trait JDotAttacher {

  def attach(contextJson: String, attachToJson: String): String
  
  def attachList(contextJsonList: List[String], attachToJson: String): String
  
}

object JDotAttacher {
  val originalContextKey = "_root"

  def apply(attachmentPairs: Set[JPathPair],
            contextPath: Option[String] = None,
            transformer: Option[JDotTransformer] = None,
            nestedAttachers: List[JDotAttacher] = Nil): JDotAttacher =
    JValueAttacher(attachmentPairs, contextPath.map(JPath.fromString(_)), transformer, nestedAttachers)

  def withAdditionalJson(attachmentPairs: Set[JPathPair],
                         additionalContextJson: String,
                         transformer: Option[JDotTransformer] = None,
                         nestedAttachers: List[JDotAttacher] = Nil): JValueAttacher =
     JValueAttacher.withAdditionalJson(attachmentPairs, parse(additionalContextJson), transformer, nestedAttachers)

  def withAdditionalJsonList(attachmentPairs: Set[JPathPair],
                             additionalContextJsonList: List[String],
                             transformer: Option[JDotTransformer] = None,
                             nestedAttachers: List[JDotAttacher] = Nil): JValueAttacher =
     JValueAttacher.withAdditionalJsonList(attachmentPairs, additionalContextJsonList.map(parse(_)), transformer, nestedAttachers)

  def applyAttachers(context: String, attachTo: String, attachers: List[JDotAttacher]): String = {
    compact(render(JValueAttacher.applyAttachers(parse(context), parse(attachTo), attachers)))
  }
}