package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._

object JValueMerger {
  
  
  def leftMergeWithArraysOnIndex(leftJson: String, rightJson: String): String = {
    pretty(render(leftMergeWithArraysOnIndex(parse(leftJson), parse(rightJson))))
  }
  
  def leftMergeWithArraysAsValues(leftJson: String, rightJson: String): String = {
    pretty(render(leftMergeWithArraysAsValues(parse(leftJson), parse(rightJson))))
  }
  
  
  type JMerge =  (JValue, JValue) => JValue
	type JArrayMerge = (List[JValue], List[JValue], JMerge) => List[JValue]
  type JObjectMerge = (List[JField], List[JField], JMerge) => List[JField]

  def leftMergeWithArraysOnIndex: (JValue, JValue) => JValue = leftMerge(mergeFields, mergeArraysOnIndex)
  def leftMergeWithArraysAsValues: (JValue, JValue) => JValue = leftMerge(mergeFields, leftMergeArraysAsValues)
  
  private def leftMerge(objectStrat: JObjectMerge, arrayStrat: JArrayMerge): JMerge = {
    def inner(val1: JValue, val2: JValue): JValue = (val1, val2) match {
      case (JObject(xs), JObject(ys)) => JObject(objectStrat(xs, ys, inner))
      case (JArray(xs), JArray(ys)) => JArray(arrayStrat(xs, ys, inner))
      case (JNothing, x) => x
      case (x, _) => x
    }
    inner
  }
  
 
  private def mergeFields(vs1: List[JField], vs2: List[JField], outerMerge: JMerge): List[JField] = {
    def mergeRec(xleft: List[JField], yleft: List[JField]): List[JField] = xleft match {
      case Nil => yleft
      case (xn, xv) :: xs => yleft find (_._1 == xn) match {
        case Some(y @ (yn, yv)) =>
          JField(xn, outerMerge(xv, yv)) :: mergeRec(xs, yleft filterNot (_ == y))
        case None => JField(xn, xv) :: mergeRec(xs, yleft)
      }
    }

    mergeRec(vs1, vs2)
  }

  private def mergeArraysOnIndex(vs1: List[JValue], vs2: List[JValue], outerMerge: JMerge): List[JValue] = {
    def mergeRec(xleft: List[JValue], yleft: List[JValue]): List[JValue] = (xleft, yleft) match {
      case (Nil, ys) => ys
      case (xs, Nil) => xs
      case (x :: xs, y :: ys) => outerMerge(x, y) :: mergeRec(xs, ys)
    }

    mergeRec(vs1, vs2)
  }
  
  private def leftMergeArraysAsValues(vs1: List[JValue], vs2: List[JValue], outerMerge: JMerge): List[JValue] = (vs1, vs2) match {
    case (Nil, ys) => ys
    case (xs, _) => xs
  }
  
}