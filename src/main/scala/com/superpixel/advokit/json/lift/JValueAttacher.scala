package com.superpixel.advokit.json.lift

import com.superpixel.advokit.mapper._
import com.superpixel.advokit.json.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._


class JValueAttacher(attachmentPairs: Set[JPathPair]) extends JsonContentAttacher {

  val attachBuilder = JValueBuilder(attachmentPairs.map(_.to))
  
  private def _attach(attachJson: Either[String, List[String]], attachToJson: String): String = {
    compact(render(_attachJValue(
        attachJson match {
          case Left(str) => Left(parse(str))
          case Right(ls) => Right(ls.map { j => parse(j) })
        },
        parse(attachToJson))))
  }
  
  private def _attachJValue(attachJson: Either[JValue, List[JValue]], attachToJson: JValue): JValue = {
    val accessor = JValueAccessor(attachJson match {
        case Left(j) => j
        case Right(jList) => JArray(jList)
      })
    val attachee = attachBuilder.build(attachmentPairs.map{ jpm: JPathPair => (jpm.to, accessor.getValue(jpm.from))});
    
    //    println("Attachee:\n " + compact(render(attachee)))
    //    println("AttachTo:\n " + compact(render(attachToJson)))
        
        
    import JValueMerger.{MergeArraysOnIndex, leftMerge}
    //    val a = leftMerge(MergeArraysOnIndex)(attachee, attachToJson)
    //    println("Attached:\n " + compact(render(a)))
    //    a
    leftMerge(MergeArraysOnIndex)(attachee, attachToJson)
  }
  
  override def attach(attachJson: String, attachToJson: String): String = _attach(Left(attachJson), attachToJson)
  override def attachList(attachJsonList: List[String], attachToJson: String): String = _attach(Right(attachJsonList), attachToJson)
  
  def attachJValue(attachJson: JValue, attachToJson: JValue): JValue = _attachJValue(Left(attachJson), attachToJson)
  def attachJValueList(attachJsonList: List[JValue], attachToJson: JValue): JValue = _attachJValue(Right(attachJsonList), attachToJson)
  
}


object JValueAttacher {
  
  def apply(attachmentPairs: Set[JPathPair]): JValueAttacher = new JValueAttacher(attachmentPairs)
  
}